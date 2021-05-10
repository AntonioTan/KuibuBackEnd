package Tables

import Globals.GlobalDBs
import Plugins.MSUtils.ServiceUtils
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try

case class TaskToDoMapRow(taskID: String, taskToDoID: String)

class TaskToDoMapTable(tag: Tag) extends Table[TaskToDoMapRow](tag, GlobalDBs.kuibu_schema, _tableName = "task_todo_map") {
  def taskToDoMapID: Rep[Long] = column[Long]("task_todo_map_id", O.PrimaryKey, O.AutoInc, O.SqlType("SERIAL"))

  def taskID: Rep[String] = column[String]("task_id")

  def taskToDoID: Rep[String] = column[String]("task_todo_id")

  override def * : ProvenShape[TaskToDoMapRow] = (taskID, taskToDoID).mapTo[TaskToDoMapRow]
}

object TaskToDoMapTable {
  val taskToDoMapTable: TableQuery[TaskToDoMapTable] = TableQuery[TaskToDoMapTable]

  def addTaskToDoMap(taskID: String, taskToDoID: String): Try[Int] = Try {
    ServiceUtils.exec(taskToDoMapTable += TaskToDoMapRow(
      taskID = taskID,
      taskToDoID = taskToDoID
    ))
  }

  def getTaskToDoIDList(taskID: String): Try[List[String]] = Try {
    ServiceUtils.exec(taskToDoMapTable.filter(_.taskID === taskID).map(_.taskToDoID).result).toList
  }

}
