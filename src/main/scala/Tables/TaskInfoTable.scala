package Tables

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

case class TaskInfoRow(taskID: String, projectID: String, taskName: String, startDate: DateTime, endDate: DateTime, description: String, childrenIDList: List[String])

class TaskInfoTable(tag: Tag) extends Table[TaskInfoRow](tag, GlobalDBs.kuibu_schema, _tableName = "task_info") {
  def taskID: Rep[String] = column[String]("task_id", O.PrimaryKey)

  def projectID: Rep[String] = column[String]("project_id")

  def taskName: Rep[String] = column[String]("task_name")

  def startDate: Rep[DateTime] = column[DateTime]("start_date")

  def endDate: Rep[DateTime] = column[DateTime]("end_date")

  def description: Rep[String] = column[String]("description")

  def childrenIDList: Rep[List[String]] = column[List[String]]("children_id_list")

  override def * : ProvenShape[TaskInfoRow] = (taskID, projectID, taskName, startDate, endDate, description, childrenIDList).mapTo[TaskInfoRow]
}

object TaskInfoTable {
  val taskInfoTable: TableQuery[TaskInfoTable] = TableQuery[TaskInfoTable]

  def generateNewID(): String = {
    var newID = StringUtils.randomString(GlobalRules.taskIDLength)
    while (IDExists(newID).get) newID = StringUtils.randomString(GlobalRules.taskIDLength)
    newID
  }

  def IDExists(taskID: String): Try[Boolean] = Try {
    ServiceUtils.exec(taskInfoTable.filter(_.taskID === taskID).exists.result)
  }

  def getTaskName(taskID: String): Try[String] = Try {
    ServiceUtils.exec(taskInfoTable.filter(_.taskID === taskID).map(_.taskName).result.head)
  }
}
