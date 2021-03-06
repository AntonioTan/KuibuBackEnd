package Tables

import Globals.{GlobalDBs, GlobalRules}
import Plugins.CommonUtils.StringUtils
import Plugins.MSUtils.CustomColumnTypes._
import Plugins.MSUtils.ServiceUtils
import geny.Generator.from
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try


case class UserBasicInfo(userName: String, sessionIDList: List[String], friendIDList: List[String], projectIDList: List[String])

case class UserAccountRow(userID: String, userName: String, passWord: String, registerTime: DateTime, sessionIDList: List[String], friendIDList: List[String], projectIDList: List[String])

class UserAccountTable(tag: Tag) extends Table[UserAccountRow](tag, GlobalDBs.kuibu_schema, _tableName = "user_account") {
  def userID: Rep[String] = column[String]("user_id", O.PrimaryKey)

  def userName: Rep[String] = column[String]("user_name")

  def passWord: Rep[String] = column[String]("pass_word")

  def registerTime: Rep[DateTime] = column[DateTime]("register_time")

  def sessionIDList: Rep[List[String]] = column[List[String]]("session_list")

  def friendIDList: Rep[List[String]] = column[List[String]]("friend_id_list")

  def projectIDList: Rep[List[String]] = column[List[String]]("project_id_list")

  override def * : ProvenShape[UserAccountRow] = (userID, userName, passWord, registerTime, sessionIDList, friendIDList, projectIDList).mapTo[UserAccountRow]
}

object UserAccountTable {
  val userAccountTable: TableQuery[UserAccountTable] = TableQuery[UserAccountTable]

  def generateNewID(): String = {
    var newID = StringUtils.randomString(GlobalRules.userIDLength)
    while (IDExists(newID).get) newID = StringUtils.randomString(GlobalRules.userIDLength)
    newID
  }

  def addUser(userName: String, userPassword: String): Try[Unit] = Try{
    val now = DateTime.now()
    val newSessionList: List[String] = List.empty[String]
    val newFriendIDList: List[String] = List.empty[String]
    val newProjectIDList: List[String] = List.empty[String]
    val newID = generateNewID()
    ServiceUtils.exec(userAccountTable += UserAccountRow(newID, userName, userPassword, now, sessionIDList = newSessionList, friendIDList = newFriendIDList, projectIDList = newProjectIDList))
  }

  // ????????????user
  def addUserWithUserID(userID: String, userName: String, userPassword: String): Try[Unit] = Try {
    val now = DateTime.now()
    val newSessionList: List[String] = List.empty[String]
    val newFriendIDList: List[String] = List.empty[String]
    val newProjectIDList: List[String] = List.empty[String]
    ServiceUtils.exec(userAccountTable += UserAccountRow(userID, userName, userPassword, now, sessionIDList = newSessionList, friendIDList = newFriendIDList, projectIDList = newProjectIDList))
  }

  def addUserFriend(userID: String, friendID: String): Try[String] = Try {
     if(IDExists(userID).get&&IDExists(friendID).get) {
       val userFriendList = ServiceUtils.exec(userAccountTable.filter(_.userID===userID).map(_.friendIDList).result.headOption)
       if(userFriendList.get.contains(friendID)) {
         "??????????????????"
       } else {
         val newUserFriendList: List[String] = userFriendList.get :+ friendID
         ServiceUtils.exec(userAccountTable.filter(_.userID === userID).map(_.friendIDList).update(newUserFriendList))
         "?????????????????????"
       }
     } else {
       "??????ID?????????ID??????"
     }
  }

  def getBasicUserInfo(userID: String): Try[UserBasicInfo] = Try {
    val user: UserAccountRow = ServiceUtils.exec(userAccountTable.filter(_.userID===userID).result.head)
    var friendMap: Map[String, String] = Map.empty[String, String]
    UserBasicInfo(user.userName, user.sessionIDList, user.friendIDList, user.projectIDList)
  }

  def getUserNamesByIDs(userIDs: List[String]): Try[Map[String, String]] = Try {
    var memberMap = Map.empty[String, String]
    for(userID <- userIDs) {
      memberMap = memberMap ++ Map(userID -> getNameByID(userID).get)
    }
    memberMap
  }

  def getNameByID(userID: String): Try[String] = Try {
    ServiceUtils.exec(userAccountTable.filter(_.userID===userID).map(_.userName).result.head)
  }

  def checkLogin(userID: String, passWord: String): Try[Boolean] = Try {
    ServiceUtils.exec(userAccountTable.filter(user => user.userID===userID&&user.passWord===passWord).exists.result)
  }

  // ??????????????????id
  def addSessionID(userID: String, sessionID: String): Try[String] = Try {
    if(!IDExists(userID).get) {
      "??????ID?????????"
    } else {
      val pastSessionIDList: List[String] = ServiceUtils.exec(userAccountTable.filter(_.userID===userID).map(_.sessionIDList).result.head)
      if(pastSessionIDList.contains(sessionID)) {
        "?????????????????????"
      } else {
        val newSessionIDList: List[String] = pastSessionIDList :+ sessionID
        ServiceUtils.exec(userAccountTable.filter(_.userID===userID).map(_.sessionIDList).update(newSessionIDList))
        "????????????????????????"
      }
    }
  }


  def addProjectID(userID: String, projectID: String): Try[String] = Try {
    if(!IDExists(userID).get) {
      "???????????????"
    } else {
      val pastProjectIDList: List[String] = ServiceUtils.exec(userAccountTable.filter(_.userID===userID).map(_.projectIDList).result.head)
        if(pastProjectIDList.contains(projectID)) {
          "?????????????????????"
        } else {
          val newProjectIDList: List[String] = pastProjectIDList :+ projectID
          ServiceUtils.exec(userAccountTable.filter(_.userID===userID).map(_.projectIDList).update(newProjectIDList))
          "?????????????????????"
        }
    }
  }

  def IDExists(userID: String): Try[Boolean] = Try {
    ServiceUtils.exec(userAccountTable.filter(_.userID === userID).exists.result)
  }

}

