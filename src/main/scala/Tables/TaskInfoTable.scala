package Tables

import Globals.GlobalUtils.convertDateTimeToWebString
import Globals.{GlobalDBs, GlobalRules}
import Impl.ChatPortalMessage
import Impl.Messages.TokenMessage
import Plugins.CommonUtils.{IOUtils, StringUtils}
import Plugins.MSUtils.CustomColumnTypes._
import Plugins.MSUtils.ServiceUtils
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.json4s._
import org.json4s.native.JsonMethods._
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try

case class TaskInfoRow(taskID: String, projectID: String, taskName: String, status: Boolean, startDate: DateTime, endDate: DateTime, description: String, parentID: String, childrenIDList: List[String], leaderIDList: List[String], userIDList: List[String])
case class TaskCompleteInfo(taskID: String, projectID: String, taskName: String, status: Boolean, startDate: String, endDate: String, description: String, parentID: String, parentName: String, childrenMap: Map[String, TaskStatusInfo], leaderMap: Map[String, String], userMap: Map[String, String], sessionDateMap: Map[String, String])
case class TaskStatusInfo(taskName: String, status: Boolean)
case class TaskNewFromWeb(taskName: String, projectID: String, parentID: String, startDate: String, endDate: String, description: String, leaderIDList: List[String], userIDList: List[String])
case class TaskAddResult(outcome: Boolean, reason: String, taskID: String="", taskName: String="")
case class MyTask(taskID: String, taskName: String, description: String, startDate: String, endDate: String, leader: String, members: String)
case class SyncTask(taskID: String, taskName: String, leaderName: String, leaderIDList: List[String], startDate: String, endDate: String, description: String, toDoList: List[TaskWebToDoInfo], processInfoMap: Map[String, TaskWebProcessInfo], allMemberMap: Map[String, String])

class TaskInfoTable(tag: Tag) extends Table[TaskInfoRow](tag, GlobalDBs.kuibu_schema, _tableName = "task_info") {
  def taskID: Rep[String] = column[String]("task_id", O.PrimaryKey)

  def projectID: Rep[String] = column[String]("project_id")

  def taskName: Rep[String] = column[String]("task_name")

  def status: Rep[Boolean] = column[Boolean]("status")

  def startDate: Rep[DateTime] = column[DateTime]("start_date")

  def endDate: Rep[DateTime] = column[DateTime]("end_date")

  def description: Rep[String] = column[String]("description")

  def parentID: Rep[String] = column[String]("parent_id")

  def childrenIDList: Rep[List[String]] = column[List[String]]("children_id_list")

  def leaderIDList: Rep[List[String]] = column[List[String]]("leader_id_list")

  def userIDList: Rep[List[String]] = column[List[String]]("user_id_list")

  override def * : ProvenShape[TaskInfoRow] = (taskID, projectID, taskName, status, startDate, endDate, description, parentID, childrenIDList, leaderIDList, userIDList).mapTo[TaskInfoRow]

}

object TaskInfoTable {
  val taskInfoTable: TableQuery[TaskInfoTable] = TableQuery[TaskInfoTable]

  def generateNewID(): String = {
    var newID = StringUtils.randomString(GlobalRules.taskIDLength)
    while (IDExists(newID).get) newID = StringUtils.randomString(GlobalRules.taskIDLength)
    newID
  }

  def addTask(taskName: String, projectID: String, status: Boolean=false, startDate: DateTime, endDate: DateTime, description: String, parentID: String="", childrenIDList: List[String]=List.empty[String], leaderIDList: List[String] = List.empty[String], userIDList: List[String]): Try[String] = Try{
    val newTaskID: String = generateNewID()
    ServiceUtils.exec(taskInfoTable += TaskInfoRow(
      taskID = newTaskID,
      taskName = taskName,
      projectID = projectID,
      status = status,
      startDate = startDate,
      endDate = endDate,
      description = description,
      parentID = parentID,
      childrenIDList = childrenIDList,
      leaderIDList = leaderIDList,
      userIDList = userIDList,
    ))
    if(IDExists(parentID).get) addChildTask(parentID, newTaskID)
    ProjectInfoTable.addTask(projectID = projectID, taskID = newTaskID)
    newTaskID
  }


  def addTaskFromWeb(taskNewFromWeb: TaskNewFromWeb): Try[TaskAddResult] = Try {
    val dateTimeFormatter = DateTimeFormat.forPattern("yyyy/MM/dd")
    if(taskNewFromWeb.taskName==""||taskNewFromWeb.description==""||taskNewFromWeb.startDate==""||taskNewFromWeb.endDate==""||taskNewFromWeb.userIDList.isEmpty) {
      TaskAddResult(outcome = false, reason = "新任务信息不完整")
    } else {
      val newTaskID = addTask(
        taskName = taskNewFromWeb.taskName,
        projectID = taskNewFromWeb.projectID,
        startDate = DateTime.parse(taskNewFromWeb.startDate, dateTimeFormatter),
        endDate = DateTime.parse(taskNewFromWeb.endDate, dateTimeFormatter),
        description = taskNewFromWeb.description,
        parentID =  taskNewFromWeb.parentID,
        leaderIDList = taskNewFromWeb.leaderIDList,
        userIDList = taskNewFromWeb.userIDList
      ).get
      if(taskNewFromWeb.parentID!="") TaskInfoTable.addChildTask(taskNewFromWeb.parentID, newTaskID)
      TaskAddResult(outcome = true, reason="成功添加新任务", taskID = newTaskID, taskName = taskNewFromWeb.taskName)
    }
  }

  def addChildTask(taskID: String, childTaskID: String): Try[Int] = Try {
    var targetTaskChildren = ServiceUtils.exec(taskInfoTable.filter(_.taskID===taskID).map(_.childrenIDList).result.head)
    if(!targetTaskChildren.contains(childTaskID)) targetTaskChildren = targetTaskChildren :+ childTaskID
    ServiceUtils.exec(taskInfoTable.filter(_.taskID===taskID).map(_.childrenIDList).update(targetTaskChildren))
  }

  def addTaskWithID(taskID: String, taskName: String, status: Boolean=false, projectID: String, startDate: DateTime, endDate: DateTime, description: String, parentID: String="", childrenIDList: List[String], leaderIDList: List[String] = List.empty[String] , userIDList: List[String]): Try[Int] = Try{
    val rst: Int = ServiceUtils.exec(taskInfoTable += TaskInfoRow(
      taskID = taskID,
      taskName = taskName,
      status = status,
      projectID = projectID,
      startDate = startDate,
      endDate = endDate,
      description = description,
      parentID = parentID,
      childrenIDList = childrenIDList,
      leaderIDList = leaderIDList,
      userIDList = userIDList,
    ))
    if(IDExists(parentID).get) addChildTask(parentID, taskID)
    ProjectInfoTable.addTask(projectID = projectID, taskID = taskID)
    rst

  }

  def getTaskNamesByIDs(taskIDs: List[String]): Try[Map[String, String]] = Try {
    var taskMap: Map[String, String] = Map.empty[String, String]
    for(taskID <- taskIDs) {
      taskMap += (taskID -> getTaskName(taskID).get)
    }
    taskMap
  }

  def getTaskStatusesByIDs(taskIDs: List[String]): Try[Map[String, TaskStatusInfo]] = Try {
    var taskMap: Map[String, TaskStatusInfo] = Map.empty[String, TaskStatusInfo]
    for(taskID <- taskIDs) {
      taskMap += (taskID -> TaskStatusInfo(getTaskName(taskID).get, getTaskStatus(taskID).get))
    }
    taskMap

  }

  def getTaskCompleteInfo(taskID: String): Try[TaskCompleteInfo] = Try {
    val taskInfoRow: TaskInfoRow = ServiceUtils.exec(taskInfoTable.filter(_.taskID === taskID).result.head)
    val userMap: Map[String, String] = UserAccountTable.getUserNamesByIDs(taskInfoRow.userIDList).get
    val leaderMap: Map[String, String] = UserAccountTable.getUserNamesByIDs(taskInfoRow.leaderIDList).get
    val childrenMap: Map[String, TaskStatusInfo] = getTaskStatusesByIDs(taskInfoRow.childrenIDList).get
    val sessionDateMap: Map[String, String] = ChatSessionInfoTable.getSessionDateMapByTaskID(taskInfoRow.taskID).get
    TaskCompleteInfo(taskID = taskInfoRow.taskID, projectID = taskInfoRow.projectID,
      taskName = taskInfoRow.taskName, status = taskInfoRow.status, startDate = convertDateTimeToWebString(taskInfoRow.startDate), endDate = convertDateTimeToWebString(taskInfoRow.endDate),
      description = taskInfoRow.description, parentID = taskInfoRow.parentID, parentName = getTaskName(taskInfoRow.parentID).get, childrenMap = childrenMap,
      leaderMap = leaderMap, userMap = userMap, sessionDateMap = sessionDateMap)
  }


  def IDExists(taskID: String): Try[Boolean] = Try {
    ServiceUtils.exec(taskInfoTable.filter(_.taskID === taskID).exists.result)
  }

  def getTaskName(taskID: String): Try[String] = Try {
    if(IDExists(taskID).get) {
      ServiceUtils.exec(taskInfoTable.filter(_.taskID === taskID).map(_.taskName).result.head)
    } else {
      ""
    }
  }

  def getTaskStatus(taskID: String): Try[Boolean] = Try {
    ServiceUtils.exec(taskInfoTable.filter(_.taskID === taskID).map(_.status).result.head)
  }

  def checkIfRoot(taskID: String): Try[Boolean] = Try {
    ServiceUtils.exec(taskInfoTable.filter(_.taskID === taskID).result.head).parentID.length==0
  }

  def getMyTaskList(projectID: String, userID: String): Try[List[MyTask]] = Try {
    val projectTaskList: List[String] = ProjectInfoTable.getProjectTaskIDList(projectID).get
    var myTaskList: List[MyTask] = List.empty[MyTask]
    for(taskID <- projectTaskList) {
      val taskInfo: TaskInfoRow = ServiceUtils.exec(taskInfoTable.filter(_.taskID === taskID).result.head)
      if(taskInfo.leaderIDList.contains(userID) || taskInfo.userIDList.contains(userID)) {
        myTaskList = myTaskList :+ MyTask(
          taskID = taskInfo.taskID,
          taskName = taskInfo.taskName,
          description = taskInfo.description,
          startDate = convertDateTimeToWebString(taskInfo.startDate),
          endDate = convertDateTimeToWebString(taskInfo.endDate),
          leader = UserAccountTable.getUserNamesByIDs(taskInfo.leaderIDList).get.values.toList.mkString(", "),
          members = UserAccountTable.getUserNamesByIDs(taskInfo.userIDList).get.values.toList.mkString(", ")
        )
      }
    }
    myTaskList
  }

  def getMyTaskIDList(projectID: String, userID: String): Try[List[String]] = Try {
    val projectTaskIDList: List[String] = ProjectInfoTable.getProjectTaskIDList(projectID).get
    var myTaskIDList: List[String] = List.empty[String]
    for(projectTaskID <- projectTaskIDList) {
      val taskInfo: TaskInfoRow = ServiceUtils.exec(taskInfoTable.filter(taskInfo => taskInfo.taskID === projectTaskID).result.head)
      if(taskInfo.leaderIDList.contains(userID) || taskInfo.userIDList.contains(userID)) {
        myTaskIDList = myTaskIDList :+ taskInfo.taskID
      }
    }
    myTaskIDList
  }

  def getSyncTaskInfo(taskID: String): Try[SyncTask] = Try {
    val taskInfo: TaskInfoRow = ServiceUtils.exec(taskInfoTable.filter(_.taskID === taskID).result.head)
    val taskToDoInfoList: List[TaskWebToDoInfo] = TaskToDoInfoTable.getTaskToDoInfoList(taskID).get
    val taskProcessInfoMap: Map[String, TaskWebProcessInfo] = TaskProcessInfoTable.getTaskProcessInfoMap(taskID).get
    val allMemberIDList: List[String] = taskInfo.leaderIDList ++ taskInfo.userIDList
    SyncTask(
      taskID=taskInfo.taskID,
      taskName=taskInfo.taskName,
      leaderName= UserAccountTable.getUserNamesByIDs(taskInfo.leaderIDList).get.values.toList.mkString(", "),
      leaderIDList = taskInfo.leaderIDList,
      startDate = convertDateTimeToWebString(taskInfo.startDate),
      endDate = convertDateTimeToWebString(taskInfo.endDate),
      description = taskInfo.description,
      toDoList = taskToDoInfoList,
      processInfoMap = taskProcessInfoMap,
      allMemberMap = UserAccountTable.getUserNamesByIDs(allMemberIDList).get
    )

  }


}
