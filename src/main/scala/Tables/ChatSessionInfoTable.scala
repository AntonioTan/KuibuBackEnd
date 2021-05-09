package Tables

import Globals.GlobalUtils.{convertDateTimeToWebString, convertDateTimeToWebTimeString}
import Globals.{GlobalDBs, GlobalRules}
import Plugins.CommonUtils.StringUtils
import Plugins.MSUtils.CustomColumnTypes._
import Plugins.MSUtils.ServiceUtils
import Tables.UserAccountTable.{getUserNamesByIDs, userAccountTable}
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try

case class ChatSessionInfoRow(sessionID: String, sessionName: String, projectID: String, taskID: String, startDate: DateTime, userIDList: List[String])
case class ChatSessionInfo(sessionID: String, sessionName: String, userMap: Map[String, String], taskID: String, chatMessageList: List[ChatMessage])


class ChatSessionInfoTable(tag: Tag) extends Table[ChatSessionInfoRow](tag, GlobalDBs.kuibu_schema, _tableName = "chat_session_info") {

  def sessionID: Rep[String] = column[String]("session_id", O.PrimaryKey)

  def sessionName: Rep[String] = column[String]("session_name")

  def projectID: Rep[String] = column[String]("project_id")

  def taskID: Rep[String] = column[String]("task_id")

  def startDate: Rep[DateTime] = column[DateTime]("start_date")

  def userIDList: Rep[List[String]] = column[List[String]]("user_id_list")

  def * : ProvenShape[ChatSessionInfoRow] = (sessionID, sessionName, projectID, taskID, startDate, userIDList).mapTo[ChatSessionInfoRow]
}

object ChatSessionInfoTable {
  val chatSessionInfoTable: TableQuery[ChatSessionInfoTable] = TableQuery[ChatSessionInfoTable]

  def generateNewID(): String = {
    var newID = StringUtils.randomString(GlobalRules.sessionIDLength)
    while (IDExists(newID).get) newID = StringUtils.randomString(GlobalRules.sessionIDLength)
    newID
  }

  def addChatSessionInfo(projectID: String, sessionName: String, userIDList: List[String], taskID: String=""): Try[Unit] = Try {
    val newID = generateNewID()
    val now = DateTime.now()
    ServiceUtils.exec(chatSessionInfoTable += ChatSessionInfoRow(sessionID = newID, sessionName = sessionName, projectID = projectID, taskID = taskID, startDate = now, userIDList = userIDList))
    ProjectInfoTable.addSession(projectID, newID)
  }

  def addChatSessionInfoWithID(projectID: String, sessionID: String, sessionName: String, userIDList: List[String], taskID: String=""): Try[Unit] = Try {
    val now = DateTime.now()
    ServiceUtils.exec(chatSessionInfoTable += ChatSessionInfoRow(sessionID = sessionID, sessionName = sessionName, projectID = projectID, taskID = taskID, startDate = now, userIDList = userIDList))
    for(userID <- userIDList) {
      UserAccountTable.addSessionID(userID, sessionID)
    }
    ProjectInfoTable.addSession(projectID, sessionID)
  }

  def getSessionName(sessionID: String): Try[String] = Try{
    ServiceUtils.exec(chatSessionInfoTable.filter(_.sessionID===sessionID).map(_.sessionName).result.head)
  }

  def getSessionDateForWeb(sessionID: String): Try[String] = Try{
    convertDateTimeToWebString(ServiceUtils.exec(chatSessionInfoTable.filter(_.sessionID===sessionID).map(_.startDate).result.head))
  }

  def getSessionDateMapByTaskID(taskID: String): Try[Map[String, String]] = Try {
    var sessionList: List[ChatSessionInfoRow] =  ServiceUtils.exec(chatSessionInfoTable.filter(_.taskID===taskID).sortBy(_.startDate).result).toList
    var sessionDateMap: Map[String, String] = Map.empty[String, String]
    for(session <- sessionList) {
      sessionDateMap = sessionDateMap ++ Map(session.sessionID -> convertDateTimeToWebString(session.startDate))
    }
    sessionDateMap
  }

  def getSessionInfo(sessionID: String): Try[ChatSessionInfo] = Try {
    val session: ChatSessionInfoRow = ServiceUtils.exec(chatSessionInfoTable.filter(_.sessionID === sessionID).result.head)
    val chatMessageRowList: List[ChatMessageRow] = ChatMessageTable.getMessageByCount(sessionID, GlobalRules.initialChatMessageNum).get
    val userMap: Map[String, String] = getUserNamesByIDs(session.userIDList).get
    var chatMessageList: List[ChatMessage] = List.empty[ChatMessage]
    for(chatMessageRow <- chatMessageRowList) {
      chatMessageList = chatMessageList :+ ChatMessage(
        chatMessageID = chatMessageRow.chatMessageID,
        senderID = chatMessageRow.senderID,
        senderName = UserAccountTable.getNameByID(chatMessageRow.senderID).get,
        sessionID = chatMessageRow.sessionID,
        sendDate = convertDateTimeToWebTimeString(chatMessageRow.sendDate),
        message = chatMessageRow.message
      )
    }
    ChatSessionInfo(sessionID = sessionID, sessionName = session.sessionName, userMap = userMap, taskID = session.taskID, chatMessageList = chatMessageList)

  }


  def whetherIncludeUser(sessionID: String, userID: String): Try[Boolean] = Try {
    val userIDList: List[String] = ServiceUtils.exec(chatSessionInfoTable.filter(_.sessionID === sessionID).map(_.userIDList).result.head)
    userIDList.contains(userID)
  }

  def IDExists(sessionID: String): Try[Boolean] = Try {
    ServiceUtils.exec(chatSessionInfoTable.filter(_.sessionID === sessionID).exists.result)
  }

}
