package Tables

import Globals.GlobalUtils.convertDateTimeToWebString
import Globals.{GlobalDBs, GlobalRules}
import Impl.ChatPortalMessage
import Impl.Messages.TokenMessage
import Plugins.CommonUtils.{IOUtils, StringUtils}
import Plugins.MSUtils.CustomColumnTypes._
import Plugins.MSUtils.ServiceUtils
import org.joda.time.DateTime
import org.json4s._
import org.json4s.native.JsonMethods._
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try

case class TaskInfoRow(taskID: String, projectID: String, taskName: String, status: Boolean, startDate: DateTime, endDate: DateTime, description: String, parentID: String, childrenIDList: List[String], leaderIDList: List[String], userIDList: List[String])
case class TaskCompleteInfo(taskID: String, projectID: String, taskName: String, status: Boolean, startDate: String, endDate: String, description: String, parentID: String, parentName: String, childrenMap: Map[String, TaskStatusInfo], leaderMap: Map[String, String], userMap: Map[String, String])
case class TaskStatusInfo(taskName: String, status: Boolean)

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

  override def * : ProvenShape[TaskInfoRow] = (taskID, projectID, taskName, status, startDate, endDate, description, parentID, childrenIDList, userIDList).mapTo[TaskInfoRow]
}

object TaskInfoTable {
  val taskInfoTable: TableQuery[TaskInfoTable] = TableQuery[TaskInfoTable]

  def generateNewID(): String = {
    var newID = StringUtils.randomString(GlobalRules.taskIDLength)
    while (IDExists(newID).get) newID = StringUtils.randomString(GlobalRules.taskIDLength)
    newID
  }

  def addTask(taskName: String, projectID: String, status: Boolean=false, startDate: DateTime, endDate: DateTime, description: String, parentID: String="", childrenIDList: List[String], leaderIDList: List[String] = List.empty[String], userIDList: List[String]): Try[Int] = Try{
    ServiceUtils.exec(taskInfoTable += TaskInfoRow(
      taskID = generateNewID(),
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
  }

  def addTaskWithID(taskID: String, taskName: String, status: Boolean=false, projectID: String, startDate: DateTime, endDate: DateTime, description: String, parentID: String="", childrenIDList: List[String], leaderIDList: List[String] = List.empty[String] , userIDList: List[String]): Try[Int] = Try{
    ServiceUtils.exec(taskInfoTable += TaskInfoRow(
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
      userIDList = userIDList
    ))

  }

  def getTaskNamesByIDs(taskIDs: List[String]): Try[Map[String, String]] = Try {
    var taskMap: Map[String, String] = Map.empty[String, String]
    for(taskID <- taskIDs) {
      taskMap += (taskID -> getTaskName(taskID).get)
    }
    taskMap
  }

  def getTaskCompleteInfo(taskID: String): Try[TaskCompleteInfo] = Try { val taskInfoRow: TaskInfoRow = ServiceUtils.exec(taskInfoTable.filter(_.taskID === taskID).result.head)
    val userMap: Map[String, String] = UserAccountTable.getUserNamesByIDs(taskInfoRow.userIDList).get
    val leaderMap: Map[String, String] = UserAccountTable.getUserNamesByIDs(taskInfoRow.leaderIDList).get
    val childrenMap: Map[String, String] = getTaskNamesByIDs(taskInfoRow.childrenIDList).get

    TaskCompleteInfo(taskID = taskInfoRow.taskID, projectID = taskInfoRow.projectID,
      taskName = taskInfoRow.taskName, status = taskInfoRow.status, startDate = convertDateTimeToWebString(taskInfoRow.startDate), endDate = convertDateTimeToWebString(taskInfoRow.endDate),
      description = taskInfoRow.description, parentID = taskInfoRow.parentID, parentName = getTaskName(taskInfoRow.parentID).get, childrenMap = childrenMap,
      leaderMap = leaderMap, userMap = userMap)
  }

  def IDExists(taskID: String): Try[Boolean] = Try {
    ServiceUtils.exec(taskInfoTable.filter(_.taskID === taskID).exists.result)
  }

  def getTaskName(taskID: String): Try[String] = Try {
    ServiceUtils.exec(taskInfoTable.filter(_.taskID === taskID).map(_.taskName).result.head)
  }
}
