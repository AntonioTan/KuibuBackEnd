package Tables

import Globals.GlobalDBs
import Plugins.MSUtils.ServiceUtils
import Utils.DBUtils
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try

case class SuperUserRow(userID:String)
/** 超级用户表格 */
class SuperUserTable(tag: Tag) extends Table[SuperUserRow](tag, GlobalDBs.display_schema, _tableName = "super_user") {
  def userID: Rep[String] = column[String]("user_id", O.PrimaryKey)

  override def * : ProvenShape[SuperUserRow] = userID.mapTo[SuperUserRow]
}

object SuperUserTable {
  val superUserTable: TableQuery[SuperUserTable] = TableQuery[SuperUserTable]

  def addSuperuser(userID: String): Try[Unit] = Try(
    if (!checkSuperuser(userID).get)
      ServiceUtils.exec(superUserTable += SuperUserRow(userID))
  )

  def checkSuperuser(userID: String): Try[Boolean] =Try{
    ServiceUtils.exec(superUserTable.filter(_.userID=== userID).exists.result)
  }
}