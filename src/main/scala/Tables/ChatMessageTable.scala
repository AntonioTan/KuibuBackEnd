package Tables
import Globals.{GlobalDBs, GlobalRules}
import Plugins.CommonUtils.StringUtils
import Plugins.MSUtils.CustomColumnTypes._
import Plugins.MSUtils.ServiceUtils
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try

case class ChatMessageRow(chatMessageID: String, sessionID: String, senderID: String, message: String, sendDate: DateTime)
case class ChatMessage(chatMessageID: String, sessionID: String, senderID: String, senderName: String, message: String, sendDate: String)
case class ChatWsMessage(sessionID: String, senderID: String, message: String, sendDate: String)

class ChatMessageTable(tag: Tag) extends Table[ChatMessageRow](tag, GlobalDBs.kuibu_schema, _tableName = "chat_message") {

  def chatMessageID: Rep[String] = column[String]("chat_message_id", O.PrimaryKey)

  def sessionID: Rep[String] = column[String]("session_id")

  def senderID: Rep[String] = column[String]("sender_id")

  def message: Rep[String] = column[String]("message")

  def sendDate: Rep[DateTime] = column[DateTime]("send_date")

  def * : ProvenShape[ChatMessageRow] = (chatMessageID, sessionID, senderID, message, sendDate).mapTo[ChatMessageRow]

}

object ChatMessageTable {
  val chatMessageTable: TableQuery[ChatMessageTable] = TableQuery[ChatMessageTable]

  def generateNewID(): String = {
    var newID = StringUtils.randomString(GlobalRules.chatMessageLength)
    while (IDExists(newID).get) newID = StringUtils.randomString(GlobalRules.sessionIDLength)
    newID
  }

  def addChatMessageWithID(chatMessageID: String, sessionID: String, senderID: String, message: String): Try[Unit] = Try {
    val sendDate = DateTime.now()
    ServiceUtils.exec(chatMessageTable += ChatMessageRow(chatMessageID = chatMessageID, sessionID = sessionID, senderID = senderID, message = message, sendDate = sendDate))
  }

  def addChatMessage(sessionID: String, senderID: String, message: String): Try[Unit] = Try {
    val chatMessageID = generateNewID()
    val sendDate = DateTime.now()
    ServiceUtils.exec(chatMessageTable += ChatMessageRow(chatMessageID = chatMessageID, sessionID = sessionID, senderID = senderID, message = message, sendDate = sendDate))
  }

  def addChatFromWs(chatMessage: ChatWsMessage): Try[Int] = Try{
    val chatMessageID: String = generateNewID()
    ServiceUtils.exec(chatMessageTable += ChatMessageRow(
      chatMessageID = chatMessageID,
      sessionID = chatMessage.sessionID,
      senderID = chatMessage.senderID,
      message = chatMessage.message,
      sendDate = new DateTime(chatMessage.sendDate),
    ))
  }

  def getMessageByCount(sessionID: String, num: Int): Try[List[ChatMessageRow]] = Try {
    ServiceUtils.exec(chatMessageTable.filter(_.sessionID === sessionID).sortBy(_.sendDate.desc).take(num).result).toList.reverse
  }

  def IDExists(chatMessageID: String): Try[Boolean] = Try {
    ServiceUtils.exec(chatMessageTable.filter(_.chatMessageID === chatMessageID).exists.result)
  }



}


