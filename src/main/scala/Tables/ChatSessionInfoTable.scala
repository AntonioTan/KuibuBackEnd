package Tables

import Globals.{GlobalDBs, GlobalRules}
import Plugins.CommonUtils.StringUtils
import Plugins.MSUtils.CustomColumnTypes._
import Plugins.MSUtils.ServiceUtils
import Tables.UserAccountTable.userAccountTable
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try

case class ChatSessionInfoRow(sessionID: String, sessionName: String, startDate: DateTime, userIDList: List[String])

class ChatSessionInfoTable(tag: Tag) extends Table[ChatSessionInfoRow](tag, GlobalDBs.kuibu_schema, _tableName = "chat_session_info") {
  def sessionID: Rep[String] = column[String]("session_id", O.PrimaryKey)

  def sessionName: Rep[String] = column[String]("session_name")

  def startDate: Rep[DateTime] = column[DateTime]("start_date")

  def userIDList: Rep[List[String]] = column[List[String]]("user_id_list")

  def * : ProvenShape[ChatSessionInfoRow] = (sessionID, sessionName, startDate, userIDList).mapTo[ChatSessionInfoRow]
}

object ChatSessionInfoTable {
  val chatSessionInfoTable: TableQuery[ChatSessionInfoTable] = TableQuery[ChatSessionInfoTable]

  def generateNewID(): String = {
    var newID = StringUtils.randomString(GlobalRules.sessionIDLength)
    while (IDExists(newID).get) newID = StringUtils.randomString(GlobalRules.sessionIDLength)
    newID
  }

  def addChatSessionInfo(sessionName: String, userIDList: List[String]): Try[Unit] = Try {
    val newID = generateNewID()
    val now = DateTime.now()
    ServiceUtils.exec(chatSessionInfoTable += ChatSessionInfoRow(sessionID = newID, sessionName = sessionName, startDate = now, userIDList = userIDList))
  }

  def addChatSessionInfoWithID(sessionID: String, sessionName: String, userIDList: List[String]): Try[Unit] = Try {
    val now = DateTime.now()
    ServiceUtils.exec(chatSessionInfoTable += ChatSessionInfoRow(sessionID = sessionID, sessionName = sessionName, startDate = now, userIDList = userIDList))
    for(userID <- userIDList) {
      UserAccountTable.addSessionID(userID, sessionID)
    }
  }

  def getSessionName(sessionID: String): Try[String] = Try{
    ServiceUtils.exec(chatSessionInfoTable.filter(_.sessionID===sessionID).map(_.sessionName).result.head)
  }

  def IDExists(sessionID: String): Try[Boolean] = Try {
    ServiceUtils.exec(chatSessionInfoTable.filter(_.sessionID === sessionID).exists.result)
  }

}
