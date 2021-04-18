package Tables

//case class UserAccountRow(userID: String, password: String)
//
//class UserAccountTable(tag: Tag) extends Table[UserAccountTable](tag, GlobalDBs.kuibu_schema, _tableName = "user_account") {
//  def userID: Rep[Long] = column[Long]("user_id", O.PrimaryKey, O.AutoInc,)
//
//}

import Exceptions.{UserIDNotExistException, UserInvalidException}
import Globals.{GlobalDBs, GlobalRules}
import Plugins.CommonUtils.StringUtils
import Plugins.MSUtils.CustomColumnTypes._
import Plugins.MSUtils.ServiceUtils
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try

case class UserAccountRow(userID: String, userName: String, passWord: String, registerTime: DateTime, sessionList: List[String], friendIDList: List[String])

class UserAccountTable(tag: Tag) extends Table[UserAccountRow](tag, GlobalDBs.kuibu_schema, _tableName = "user_account") {
  def userID: Rep[String] = column[String]("user_id", O.PrimaryKey)

  def userName: Rep[String] = column[String]("user_name")

  def passWord: Rep[String] = column[String]("pass_word")

  def registerTime: Rep[DateTime] = column[DateTime]("register_time")

  def sessionList: Rep[List[String]] = column[List[String]]("session_list")

  def friendIDList: Rep[List[String]] = column[List[String]]("friend_id_list")

  override def * : ProvenShape[UserAccountRow] = (userID, userName, passWord, registerTime, sessionList, friendIDList).mapTo[UserAccountRow]
}

object UserAccountTable {
  val userAccountTable: TableQuery[UserAccountTable] = TableQuery[UserAccountTable]

  def generateNewID(): String = {
    var newID = StringUtils.randomString(GlobalRules.userIDLength)
    while (IDExists(newID).get) newID = StringUtils.randomString(GlobalRules.userIDLength)
    newID
  }

  // 添加新的user
  def addUser(userID: String, userName: String, userPassword: String): Try[Unit] = Try {
    val now = DateTime.now()
    val newSessionList: List[String] = List.empty[String]
    val newFriendIDList: List[String] = List.empty[String]
    ServiceUtils.exec(userAccountTable += UserAccountRow(userID, userName, userPassword, now, sessionList = newSessionList, friendIDList = newFriendIDList))
  }

  def checkLogin(userID: String, passWord: String): Try[Boolean] = Try {
    ServiceUtils.exec(userAccountTable.filter(user => user.userID===userID&&user.passWord===passWord).exists.result)
  }

  // 添加新的对话id
  def addSessionID(userID: String, sessionID: String): Try[Unit] = Try {
    val pastSessionIDList: List[String] = ServiceUtils.exec(userAccountTable.filter(_.userID===userID).map(_.sessionList).result.head)
    val newSessionIDList: List[String] = pastSessionIDList :+ sessionID
    ServiceUtils.exec(userAccountTable.filter(_.userID===userID).map(_.sessionList).update(newSessionIDList))
  }

  def IDExists(userID: String): Try[Boolean] = Try {
    ServiceUtils.exec(userAccountTable.filter(_.userID === userID).exists.result)
  }

}

