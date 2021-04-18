package Tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}
import Globals.{GlobalDBs, GlobalRules}
import Plugins.CommonUtils.StringUtils
import Plugins.MSUtils.ServiceUtils

import scala.util.Try

case class UserChatContentRow(sessionID: String, userID: String, content: String)

class UserChatContentTable(tag: Tag) extends Table[UserChatContentRow](tag, GlobalDBs.kuibu_schema, _tableName="user_chat_session"){
  def sessionID:Rep[String] = column[String]("session_id", O.PrimaryKey)
  def userID: Rep[String] = column[String]("user_id")
  def content: Rep[String] = column[String]("content")
  def * : ProvenShape[UserChatContentRow] = (sessionID, userID, content).mapTo[UserChatContentRow]
}

object UserChatContentTable {
  val userChatContentTable: TableQuery[UserChatContentTable] = TableQuery[UserChatContentTable]

  def generateNewID(): String = {
    var newID = StringUtils.randomString(GlobalRules.sessionIDLength)
    while(IDExists(newID).get) {
      newID = StringUtils.randomString(GlobalRules.sessionIDLength)
    }
    newID
  }

  def addChatContent(sessionID: String, userID: String, content: String): Try[Unit] = Try{
    ServiceUtils.exec(userChatContentTable += UserChatContentRow(sessionID, userID, content))

  }

  def IDExists(sessionID: String): Try[Boolean] = Try{
    ServiceUtils.exec(userChatContentTable.filter(_.sessionID===sessionID).exists.result)
  }

}
