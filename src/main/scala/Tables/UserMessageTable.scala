package Tables

import Globals.GlobalDBs
import Impl.DisplayPortalMessage
import Impl.Messages.TokenMessage
import Plugins.CommonUtils.IOUtils
import Plugins.MSUtils.CustomColumnTypes._
import Plugins.MSUtils.ServiceUtils
import org.joda.time.DateTime
import org.json4s._
import org.json4s.native.JsonMethods._
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.util.Try

case class UserMessageRow(content : DisplayPortalMessage, time : DateTime, userID:String,
                          returnMessage:String, succesful:Boolean, userMessagePK:Long=0L)
class UserMessageTable(tag:Tag) extends Table[UserMessageRow](tag, GlobalDBs.display_schema, _tableName = "user_message"){
  def userMessagePK:Rep[Long]=column[Long]("user_message_pk", O.PrimaryKey, O.AutoInc,  O.SqlType("SERIAL"))
  def userID:Rep[String]=column[String]("user_id")
  def content : Rep[DisplayPortalMessage] = column[DisplayPortalMessage] ("content")
  def time : Rep[DateTime] = column[DateTime] ("time")
  def returnMessage:Rep[String]=column[String]("return_message")
  def successful:Rep[Boolean]=column[Boolean]("successful")
  def * : ProvenShape[UserMessageRow] = (content, time, userID,returnMessage, successful, userMessagePK).mapTo[UserMessageRow]
}

object UserMessageTable {
  val userMessageTable: TableQuery[UserMessageTable] = TableQuery[UserMessageTable]

  /** 在表格里面加入一个message */
  def addMessage(m:DisplayPortalMessage, userID:String, returnMessage:String, successful:Boolean): Try[Unit] = Try{
    m.eraseInformation()
    /** remove token，因为token没啥用 */
    m match {
      case _: TokenMessage =>
        val st: String = compact(render(parse(IOUtils.serialize(m).get) merge JObject(("token", JString("")))))
        val m2 = IOUtils.deserialize[DisplayPortalMessage](st).get
        ServiceUtils.exec(userMessageTable += UserMessageRow(m2, DateTime.now, userID, returnMessage, successful))
      case _ => ServiceUtils.exec(userMessageTable += UserMessageRow(m, DateTime.now, userID, returnMessage, successful))
    }
  }

  /** 记一下这个用户今天登陆了多少次，太多了就要报错了 */
  def countUserIDLoginRecord(userID:String):Try[Int]=Try{
    val yesterday=DateTime.now().minusDays(1)
    ServiceUtils.exec(userMessageTable.filter(m=> m.userID===userID &&m.time>yesterday).length.result)
  }
}
