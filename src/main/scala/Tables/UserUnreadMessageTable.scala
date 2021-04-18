package Tables

import Globals.{GlobalDBs, GlobalRules}
import Plugins.CommonUtils.StringUtils
import Plugins.MSUtils.CustomColumnTypes._
import Plugins.MSUtils.ServiceUtils
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try

case class UserUnreadMessageRow(receiverID: String, senderID: String, message: String, messageID: Long=0L)

class UserUnreadMessageTable(tag: Tag) extends Table[UserUnreadMessageRow](tag, GlobalDBs.kuibu_schema, _tableName = "user_unread_message") {
  def messageID:Rep[Long] = column[Long]("message_id", O.PrimaryKey, O.AutoInc, O.SqlType("SERIAL"))
  def receiverID: Rep[String] = column[String]("receiver_id")
  def senderID: Rep[String] = column[String]("sender_id")
  def message: Rep[String] = column[String]("message")
  def * : ProvenShape[UserUnreadMessageRow] = (receiverID, senderID, message, messageID).mapTo[UserUnreadMessageRow]
}

object UserUnreadMessageTable {
  val userUnreadMessageTable: TableQuery[UserUnreadMessageTable] = TableQuery[UserUnreadMessageTable]
  def addMessage(receiverID: String, senderID: String, message: String): Try[Unit] = Try{
  ServiceUtils.exec(userUnreadMessageTable += UserUnreadMessageRow(receiverID = receiverID, senderID = senderID, message = message))
  }
}

