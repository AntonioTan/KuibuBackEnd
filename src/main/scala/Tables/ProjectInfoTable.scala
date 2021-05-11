package Tables

import ActorModels.UserWebRequestBehavior.WebReplyGetCompleteProjectInfoMessage
import Globals.GlobalUtils.convertDateTimeToWebString
import Globals.{GlobalDBs, GlobalRules, GlobalUtils}
import Plugins.CommonUtils.StringUtils
import Plugins.MSUtils.CustomColumnTypes._
import Plugins.MSUtils.ServiceUtils
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try

case class ProjectInfoRow(projectID: String, projectName: String, createUserID: String, description: String, startDate: DateTime, taskIDList: List[String], userIDList: List[String], sessionIDList: List[String])
case class ProjectBasicInfo(projectID: String, projectName: String, createUserID: String, createUserName: String, description: String, startDate: String, userMap: Map[String, String])
case class ProjectCompleteInfo(projectID: String, projectName: String, createUserID: String, createUserName: String, description: String, startDate: String, userMap: Map[String, String], sessionMap: Map[String, String], taskMap: Map[String, String])
case class GanttData(projectID: String, projectName: String, startDate: String, allMemberMap: Map[String, String],taskList: List[GanttTask])


class ProjectInfoTable(tag: Tag) extends Table[ProjectInfoRow](tag, GlobalDBs.kuibu_schema, _tableName = "project_info") {
  def projectID: Rep[String] = column[String]("project_id", O.PrimaryKey)

  def projectName: Rep[String] = column[String]("project_name")

  def createUserID: Rep[String] = column[String]("create_user_id")

  def description: Rep[String] = column[String]("description")

  def startDate: Rep[DateTime] = column[DateTime]("start_date")

  def taskIDList: Rep[List[String]] = column[List[String]]("task_id_list")

  def userIDList: Rep[List[String]] = column[List[String]]("user_id_list")

  def sessionIDList: Rep[List[String]] = column[List[String]]("session_id_list")

  override def * : ProvenShape[ProjectInfoRow] = (projectID, projectName, createUserID, description, startDate, taskIDList, userIDList, sessionIDList).mapTo[ProjectInfoRow]

}

object ProjectInfoTable {
  val projectInfoTable: TableQuery[ProjectInfoTable] = TableQuery[ProjectInfoTable]

  def generateNewID(): String = {
    var newID = StringUtils.randomString(GlobalRules.projectIDLength)
    while (IDExists(newID).get) newID = StringUtils.randomString(GlobalRules.projectIDLength)
    newID
  }

  def IDExists(projectID: String): Try[Boolean] = Try {
    ServiceUtils.exec(projectInfoTable.filter(_.projectID === projectID).exists.result)
  }

  def addProjectWithID(projectID: String, projectName: String, createUserID: String, description: String, userIDList: List[String], startDate: DateTime = DateTime.now()): Try[Unit] = Try {
    ServiceUtils.exec(projectInfoTable += ProjectInfoRow(
      projectID = projectID, projectName = projectName, createUserID = createUserID, description = description, startDate = startDate, taskIDList = List.empty[String], userIDList = userIDList, sessionIDList = List.empty[String]
    ))
    for(userID <- userIDList) {
      UserAccountTable.addProjectID(userID, projectID)
    }
  }

  def addProject(projectName: String, createUserID: String, description: String, userIDList: List[String]): Try[String] = Try{
    val newProjectID = generateNewID()
    val startDate: DateTime = DateTime.now()
    UserAccountTable.addProjectID(userID = createUserID, projectID = newProjectID)
    val addedUserIDList = userIDList :+ createUserID
    for(userID <- addedUserIDList) {
      UserAccountTable.addProjectID(userID, projectID = newProjectID)
    }
    ServiceUtils.exec(projectInfoTable += ProjectInfoRow(
      projectID = newProjectID,
      projectName = projectName,
      createUserID = createUserID,
      startDate = startDate,
      description = description,
      userIDList = addedUserIDList,
      taskIDList = List.empty[String],
      sessionIDList= List.empty[String]
    ))
    newProjectID
  }

  def addTask(projectID: String, taskID: String) : Try[Unit]= Try {
    var taskIDList: List[String] = ServiceUtils.exec(projectInfoTable.filter(_.projectID===projectID).map(_.taskIDList).result.head)
    if(!taskIDList.contains(taskID)) {
      taskIDList = taskIDList :+ taskID
      ServiceUtils.exec(projectInfoTable.filter(_.projectID===projectID).map(_.taskIDList).update(taskIDList))
    }
  }

  def addSession(projectID: String, sessionID: String): Try[Unit] = Try {
    var sessionIDList: List[String] = ServiceUtils.exec(projectInfoTable.filter(_.projectID===projectID).map(_.sessionIDList).result.head)
    if(!sessionIDList.contains(sessionID)) {
      sessionIDList = sessionIDList :+ sessionID
      ServiceUtils.exec(projectInfoTable.filter(_.projectID === projectID).map(_.sessionIDList).update(sessionIDList))
    }
  }


  def getBasicProjectInfo(projectID: String): Try[ProjectBasicInfo] = Try {
    val project: ProjectInfoRow = ServiceUtils.exec(projectInfoTable.filter(task => task.projectID===projectID).result.head)
    val createUserName: String = UserAccountTable.getBasicUserInfo(project.createUserID).get.userName
    val startDate: String = GlobalUtils.convertDateTimeToWebString(project.startDate)
    var userMap: Map[String, String] = Map.empty[String, String]
    for(userID <- project.userIDList) {
      userMap += (userID -> UserAccountTable.getBasicUserInfo(userID).get.userName)
    }
    ProjectBasicInfo(projectID = projectID, projectName = project.projectName, createUserID = project.createUserID, createUserName = createUserName, description = project.description, startDate = startDate, userMap = userMap)
  }

  // 需要过滤出根任务
  def getTasksInfo(projectID: String): Try[Map[String, String]] = Try {
    val project: ProjectInfoRow = ServiceUtils.exec(projectInfoTable.filter(_.projectID===projectID).result.head)
    var taskMap: Map[String, String] = Map.empty[String, String]
    for(taskID <- project.taskIDList) {
      if(TaskInfoTable.checkIfRoot(taskID).get) {
        taskMap += (taskID -> TaskInfoTable.getTaskName(taskID).get)
      }
    }
    taskMap
  }


  def getCompleteProjectInfo(projectID: String): Try[ProjectCompleteInfo] = Try {
    val project: ProjectInfoRow = ServiceUtils.exec(projectInfoTable.filter(_.projectID===projectID).result.head)
    val createUserName: String = UserAccountTable.getBasicUserInfo(project.createUserID).get.userName
    val startDate: String = GlobalUtils.convertDateTimeToWebString(project.startDate)
    var userMap: Map[String, String] = Map.empty[String, String]
    for(userID <- project.userIDList) {
      userMap += (userID -> UserAccountTable.getBasicUserInfo(userID).get.userName)
    }
    var sessionMap: Map[String, String] = Map.empty[String, String]
    for(sessionID <- project.sessionIDList) {
      sessionMap += (sessionID -> ChatSessionInfoTable.getSessionName(sessionID).get)
    }
    var taskMap: Map[String, String] = Map.empty[String, String]
    for(taskID <- project.taskIDList) {
      taskMap += (taskID -> TaskInfoTable.getTaskName(taskID).get)
    }
    ProjectCompleteInfo(projectID = project.projectID, projectName = project.projectName, createUserID = project.createUserID, createUserName = createUserName,
      startDate = startDate, description = project.description,
      userMap = userMap, sessionMap = sessionMap, taskMap = taskMap)
  }

  def getUserIncludedSessionIDList(projectID: String, userID: String): Try[List[String]] = Try{
    var sessionIDList: List[String] = ServiceUtils.exec(projectInfoTable.filter(_.projectID === projectID).map(_.sessionIDList).result.head)
    sessionIDList.filter(sessionID => ChatSessionInfoTable.whetherIncludeUser(sessionID, userID).get)
  }

  def getProjectTaskIDList(projectID: String): Try[List[String]] = Try {
    ServiceUtils.exec(projectInfoTable.filter(_.projectID === projectID).map(_.taskIDList).result.head)
  }

  def getGanttData(projectID: String): Try[GanttData] = Try {
    val projectInfo: ProjectInfoRow = ServiceUtils.exec(projectInfoTable.filter(_.projectID === projectID).result.head)
    val allMemberMap: Map[String, String] = UserAccountTable.getUserNamesByIDs(projectInfo.userIDList).get
    val ganttTaskList: List[GanttTask] = TaskInfoTable.getGanttTaskList(projectInfo.taskIDList).get
    GanttData(
      projectID = projectInfo.projectID,
      projectName = projectInfo.projectName,
      startDate = convertDateTimeToWebString(projectInfo.startDate),
      allMemberMap = allMemberMap,
      taskList = ganttTaskList
    )
  }


}

