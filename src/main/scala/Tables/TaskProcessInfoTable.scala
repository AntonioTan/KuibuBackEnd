package Tables

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

  def addTaskProcessInfo(taskID: String, editUserID: String, content: String, editDate: DateTime=DateTime.now()): Try[Int] = Try {
    val taskProcessInfoID: String = generateNewID()
    ServiceUtils.exec(taskProcessInfoTable += TaskProcessInfoRow(
      taskProcessInfoID = taskProcessInfoID,
      editUserID = editUserID,
      content = content,
      editDate = editDate
    ))
    TaskProcessMapTable.addTaskProcessMap(taskID = taskID, taskProcessInfoID = taskProcessInfoID).get
  }

  def IDExists(taskProcessInfoID: String): Try[Boolean] = Try {
    ServiceUtils.exec(taskProcessInfoTable.filter(_.taskProcessInfoID=== taskProcessInfoID).exists.result)
  }

}
