package Tables

import Globals.GlobalDBs
import Plugins.MSUtils.ServiceUtils
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try

case class TaskProcessMapRow(taskID: String, taskProcessInfoID: String)

class TaskProcessMapTable(tag: Tag) extends Table[TaskProcessMapRow](tag, GlobalDBs.kuibu_schema, _tableName = "task_process_map") {
  def taskProcessMapID: Rep[Long] = column[Long]("task_process_map_id", O.PrimaryKey, O.AutoInc, O.SqlType("SERIAL"))

  def taskID: Rep[String] = column[String]("task_id")

  def taskProcessInfoID: Rep[String] = column[String]("task_process_info_id")

  override def * : ProvenShape[TaskProcessMapRow] = (taskID, taskProcessInfoID).mapTo[TaskProcessMapRow]
}

object TaskProcessMapTable {
  val taskProcessMapTable: TableQuery[TaskProcessMapTable] = TableQuery[TaskProcessMapTable]

  def addTaskProcessMap(taskID: String, taskProcessInfoID: String): Try[Int] = Try {
    ServiceUtils.exec(taskProcessMapTable += TaskProcessMapRow(
      taskID = taskID, taskProcessInfoID = taskProcessInfoID
    ))
  }

  def getTaskProcessInfoID(taskID: String): Try[List[String]] = Try {
    ServiceUtils.exec(taskProcessMapTable.filter(_.taskID === taskID).map(_.taskProcessInfoID).result).toList
  }


}
