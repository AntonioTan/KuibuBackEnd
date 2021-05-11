package Tables

import Globals.GlobalUtils.convertDateTimeToWebString
import Globals.{GlobalDBs, GlobalRules}
import Plugins.CommonUtils.StringUtils
import Plugins.MSUtils.CustomColumnTypes.jodaDateTimeType
import Plugins.MSUtils.ServiceUtils
import Tables.TaskToDoInfoTable.IDExists
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try

case class TaskProcessInfoRow(taskProcessInfoID: String, editUserID: String, content: String, editDate: DateTime)
case class TaskWebProcessInfo(taskProcessInfoID: String, editUserID: String, content: String, editDate: String)


class TaskProcessInfoTable(tag: Tag) extends Table[TaskProcessInfoRow](tag, GlobalDBs.kuibu_schema, _tableName = "task_process_info") {

  def taskProcessInfoID: Rep[String] = column[String]("task_process_id", O.PrimaryKey)

  def editUserID: Rep[String] = column[String]("edit_user_id")

  def content: Rep[String] = column[String]("content")

  def editDate: Rep[DateTime] = column[DateTime]("edit_date")

  override def * : ProvenShape[TaskProcessInfoRow] = (taskProcessInfoID, editUserID, content, editDate).mapTo[TaskProcessInfoRow]

}

object TaskProcessInfoTable {
  val taskProcessInfoTable: TableQuery[TaskProcessInfoTable] =  TableQuery[TaskProcessInfoTable]


  def generateNewID(): String = {
    var newID = StringUtils.randomString(GlobalRules.taskProcessInfoIDLength)
    while (IDExists(newID).get) newID = StringUtils.randomString(GlobalRules.taskProcessInfoIDLength)
    newID
  }

  def addTaskProcessInfoWithID(taskID: String, taskProcessInfoID: String, editUserID: String, content: String, editDate: DateTime=DateTime.now()): Try[Int] = Try {
    ServiceUtils.exec(taskProcessInfoTable += TaskProcessInfoRow(
      taskProcessInfoID = taskProcessInfoID,
      editUserID = editUserID,
      content = content,
      editDate = editDate
    ))
    TaskProcessMapTable.addTaskProcessMap(taskID = taskID, taskProcessInfoID = taskProcessInfoID).get
  }

  def addTaskProcessInfo(taskID: String, editUserID: String, content: String, editDate: DateTime=DateTime.now()): Try[String] = Try {
    val taskProcessInfoID: String = generateNewID()
    ServiceUtils.exec(taskProcessInfoTable += TaskProcessInfoRow(
      taskProcessInfoID = taskProcessInfoID,
      editUserID = editUserID,
      content = content,
      editDate = editDate
    ))
    TaskProcessMapTable.addTaskProcessMap(taskID = taskID, taskProcessInfoID = taskProcessInfoID).get
    taskProcessInfoID
  }

  def getTaskProcessInfoMap(taskID: String): Try[Map[String, TaskWebProcessInfo]] = Try {
    val taskProcessInfoIDList: List[String] =  TaskProcessMapTable.getTaskProcessInfoIDList(taskID).get
    var taskWebProcessInfoMap: Map[String,TaskWebProcessInfo] = Map.empty[String, TaskWebProcessInfo]
    for(taskProcessInfoID <- taskProcessInfoIDList) {
      val taskProcessInfo: TaskProcessInfoRow = ServiceUtils.exec(taskProcessInfoTable.filter(_.taskProcessInfoID===taskProcessInfoID).result.head)
      taskWebProcessInfoMap = taskWebProcessInfoMap.updated(taskProcessInfo.editUserID, TaskWebProcessInfo(
        taskProcessInfoID = taskProcessInfoID,
        editUserID = taskProcessInfo.editUserID,
        content = taskProcessInfo.content,
        editDate =  convertDateTimeToWebString(taskProcessInfo.editDate)
      )
      )
    }
    taskWebProcessInfoMap
  }

  def updateProcessInfo(taskID: String, editUserID: String, content: String): Try[TaskWebProcessInfo] = Try {
    val taskProcessInfoIDList = TaskProcessMapTable.getTaskProcessInfoIDList(taskID).get
    val editDate: String = convertDateTimeToWebString(DateTime.now())
    for(taskProcessInfoID <- taskProcessInfoIDList) {
      if(ServiceUtils.exec(taskProcessInfoTable.filter(taskProcessInfo =>
        taskProcessInfo.taskProcessInfoID === taskProcessInfoID && taskProcessInfo.editUserID===editUserID ).exists.result)) {
        ServiceUtils.exec(taskProcessInfoTable.filter(taskProcessInfo =>
          taskProcessInfo.taskProcessInfoID === taskProcessInfoID && taskProcessInfo.editUserID===editUserID ).map(_.editDate).update(DateTime.now()))
        ServiceUtils.exec(taskProcessInfoTable.filter(taskProcessInfo =>
          taskProcessInfo.taskProcessInfoID === taskProcessInfoID && taskProcessInfo.editUserID===editUserID ).map(_.content).update(content))
        val newTaskProcessInfo =  ServiceUtils.exec(taskProcessInfoTable.filter(taskProcessInfo =>
          taskProcessInfo.taskProcessInfoID === taskProcessInfoID && taskProcessInfo.editUserID===editUserID ).result.head)
        return convertToWeb(newTaskProcessInfo)
      }
    }
    val newProcessInfoID = TaskProcessInfoTable.addTaskProcessInfo(taskID, editUserID, content).get
    val newProcessInfo = ServiceUtils.exec(taskProcessInfoTable.filter(_.taskProcessInfoID===newProcessInfoID).result.head)
    convertToWeb(newProcessInfo).get
  }

  def convertToWeb(taskProcessInfo:TaskProcessInfoRow): Try[TaskWebProcessInfo] = Try{
    TaskWebProcessInfo(
      taskProcessInfoID = taskProcessInfo.taskProcessInfoID,
      editUserID = taskProcessInfo.editUserID,
      content = taskProcessInfo.content,
      editDate =  convertDateTimeToWebString(taskProcessInfo.editDate)
    )
  }


  def IDExists(taskProcessInfoID: String): Try[Boolean] = Try {
    ServiceUtils.exec(taskProcessInfoTable.filter(_.taskProcessInfoID=== taskProcessInfoID).exists.result)
  }

}
