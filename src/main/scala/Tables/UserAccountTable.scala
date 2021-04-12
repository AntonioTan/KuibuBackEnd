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

case class UserAccountRow(userID: String, userName: String, passWord: String, registerTime: DateTime)

class UserAccountTable(tag:Tag) extends Table[UserAccountRow] (tag,GlobalDBs.kuibu_schema, _tableName = "user_account"){
  def userID:Rep[String]=column[String]("user_id", O.PrimaryKey)
  def userName: Rep[String]=column[String]("user_name")
  def passWord: Rep[String]=column[String]("pass_word")
  def registerTime:Rep[DateTime]=column[DateTime]("register_time")

  def * : ProvenShape[UserAccountRow] = (userID, userName, passWord, registerTime).mapTo[UserAccountRow]
}
object UserAccountTable {
  val userAccountTable: TableQuery[UserAccountTable] = TableQuery[UserAccountTable]

  def generateNewID() : String = {
    var newID=StringUtils.randomString(GlobalRules.userIDLength)
    while (IDExists(newID).get) newID=StringUtils.randomString(GlobalRules.userIDLength)
    newID
  }
  def addUser(userID: String, userName: String, userPassword: String): Try[Unit] = Try{
    val now = DateTime.now()
    ServiceUtils.exec(userAccountTable += UserAccountRow(userID, userName, userPassword, now))
  }
  def IDExists(userID:String):Try[Boolean]=Try{
    ServiceUtils.exec(userAccountTable.filter(_.userID===userID).exists.result)
  }

}

