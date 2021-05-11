package Tables

import Globals.GlobalUtils.convertDateTimeToWebString
import Globals.{GlobalDBs, GlobalRules}
import Plugins.CommonUtils.StringUtils
import Plugins.MSUtils.CustomColumnTypes.jodaDateTimeType
import Plugins.MSUtils.ServiceUtils
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try


case class TaskToDoInfoRow(taskToDoID: String, status: String, finishUserID: Option[String]=null, createUserID: String, content: String, startDate: DateTime, endDate: Option[DateTime]=null)

case class TaskWebToDoInfo(taskToDoID: String, status: String, createUserID: String, finishUserID: String, content: String, startDate: String, endDate: String)

class TaskToDoInfoTable(tag: Tag) extends Table[TaskToDoInfoRow](tag, GlobalDBs.kuibu_schema, _tableName = "task_todo_info") {

  def taskToDoID: Rep[String] = column[String]("task_todo_id", O.PrimaryKey)

  def status: Rep[String] = column[String]("status")

  def finishUserID: Rep[Option[String]] = column[Option[String]]("finish_user_id")

  def createUserID: Rep[String] = column[String]("create_user_id")

  def content: Rep[String] = column[String]("content")

  def startDate: Rep[DateTime] = column[DateTime]("start_date")

  def endDate: Rep[Option[DateTime]] = column[Option[DateTime]]("end_date")

  override def * : ProvenShape[TaskToDoInfoRow] = (taskToDoID, status, finishUserID, createUserID, content, startDate, endDate).mapTo[TaskToDoInfoRow]
}

object TaskToDoInfoTable {
  val taskToDoInfoTable: TableQuery[TaskToDoInfoTable] = TableQuery[TaskToDoInfoTable]

  def generateNewID(): String = {
    var newID = StringUtils.randomString(GlobalRules.taskToDoIDLength)
    while (IDExists(newID).get) newID = StringUtils.randomString(GlobalRules.taskToDoIDLength)
    newID
  }

  def addTaskToDoWithID(taskID: String, taskToDoID: String, status: String, createUserID: String, content: String, startDate: DateTime=DateTime.now(), finishUserID: Option[String]=null, endDate: Option[DateTime]=null): Try[Int] = Try {
    ServiceUtils.exec(taskToDoInfoTable += TaskToDoInfoRow(
      taskToDoID = taskToDoID,
      status = status,
      finishUserID = finishUserID,
      createUserID = createUserID,
      content = content,
      startDate = startDate,
      endDate = endDate,
    ))
    TaskToDoMapTable.addTaskToDoMap(taskID, taskToDoID).get
  }

  def addTaskToDo(taskID: String, finishUserID: Option[String]=null, createUserID: String, content: String, startDate: DateTime=DateTime.now()): Try[String] = Try {
    val taskToDoID = generateNewID()
    ServiceUtils.exec(taskToDoInfoTable += TaskToDoInfoRow(
      taskToDoID = taskToDoID,
      status = "0",
      finishUserID = finishUserID,
      createUserID = createUserID,
      content = content,
      startDate = startDate,
    ))
    TaskToDoMapTable.addTaskToDoMap(taskID, taskToDoID).get
    taskToDoID
  }

  def getTaskToDoInfoList(taskID: String): Try[List[TaskWebToDoInfo]] = Try {
//    case class TaskWebToDoInfo(taskToDoID: String, status: String, createUserID: String, finishUserID: String, content: String, startDate: String, endDate: String)
    var taskToDoInfoList: List[TaskWebToDoInfo] = List.empty[TaskWebToDoInfo]
    val taskToDoInfoIDList: List[String] = TaskToDoMapTable.getTaskToDoIDList(taskID).get
    for(taskToDoInfoID <- taskToDoInfoIDList) {
      val taskToDoInfo = ServiceUtils.exec(taskToDoInfoTable.filter(_.taskToDoID === taskToDoInfoID).result.head)
      val endDate: String = if(taskToDoInfo.endDate.getOrElse("") != "") convertDateTimeToWebString(taskToDoInfo.endDate.get) else ""
      taskToDoInfoList = taskToDoInfoList :+ TaskWebToDoInfo(
        taskToDoID = taskToDoInfo.taskToDoID,
        status = taskToDoInfo.status,
        createUserID = taskToDoInfo.createUserID,
        finishUserID = taskToDoInfo.finishUserID.getOrElse(""),
        content = taskToDoInfo.content,
        startDate = convertDateTimeToWebString(taskToDoInfo.startDate),
        endDate = endDate
      )
    }
    taskToDoInfoList
  }

  def updateTaskToDoJudgeStatus(taskToDoID: String, outcome: Boolean): Try[Int] = Try {
    if(!outcome) {
      ServiceUtils.exec(taskToDoInfoTable.filter(_.taskToDoID === taskToDoID).map(_.finishUserID).update(null))
      ServiceUtils.exec(taskToDoInfoTable.filter(_.taskToDoID === taskToDoID).map(_.endDate).update(null))
    }
    ServiceUtils.exec(taskToDoInfoTable.filter(_.taskToDoID === taskToDoID).map(_.status).update(if(outcome)"2" else "1"))
  }

  def updateTaskToDoStatus(taskToDoID: String, status: String, finishUserID: String): Try[String] = Try {
    val endDate = DateTime.now()
    ServiceUtils.exec(taskToDoInfoTable.filter(_.taskToDoID === taskToDoID).map(_.finishUserID).update(Option(finishUserID)))
    ServiceUtils.exec(taskToDoInfoTable.filter(_.taskToDoID === taskToDoID).map(_.endDate).update(Option(endDate)))
    ServiceUtils.exec(taskToDoInfoTable.filter(_.taskToDoID === taskToDoID).map(_.status).update(status))
    convertDateTimeToWebString(endDate)
  }

  def addTaskToDoFromWeb(taskID: String, newToDo: TaskWebToDoInfo): Try[TaskWebToDoInfo] = Try {
    val newTaskToDoID = TaskToDoInfoTable.addTaskToDo(taskID = taskID, finishUserID = null, createUserID = newToDo.createUserID, content = newToDo.content).get
    convertToWeb(ServiceUtils.exec(taskToDoInfoTable.filter(_.taskToDoID === newTaskToDoID).result.head)).get
  }

  def convertToWeb(taskToDoInfo: TaskToDoInfoRow): Try[TaskWebToDoInfo] = Try {
    val endDate: String = if(taskToDoInfo.endDate.getOrElse("") != "") convertDateTimeToWebString(taskToDoInfo.endDate.get) else ""
    TaskWebToDoInfo(
      taskToDoID = taskToDoInfo.taskToDoID,
      status = taskToDoInfo.status,
      createUserID = taskToDoInfo.createUserID,
      finishUserID = taskToDoInfo.finishUserID.getOrElse(""),
      content = taskToDoInfo.content,
      startDate = convertDateTimeToWebString(taskToDoInfo.startDate),
      endDate = endDate
    )
  }

  def IDExists(taskToDoID: String): Try[Boolean] = Try {
    ServiceUtils.exec(taskToDoInfoTable.filter(_.taskToDoID === taskToDoID).exists.result)
  }

}
