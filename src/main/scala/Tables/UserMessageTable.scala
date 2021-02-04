package Tables

import Globals.{GlobalDBs, GlobalTimes}
import Impl.DisplayPortalMessage
import Impl.Messages.{CreditUploadMessage, JudgeMessage, TokenMessage}
import Plugins.CommonUtils.IOUtils
import Plugins.MSUtils.CustomColumnTypes._
import Plugins.MSUtils.ServiceUtils
import org.joda.time.{DateTime, Period}
import org.json4s._
import org.json4s.native.JsonMethods._
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

case class UserMessageRow(content : DisplayPortalMessage, time : DateTime, userID:String,
                          returnMessage:String, succesful:Boolean, userMessagePK:Long=0L)
class UserMessageTable(tag:Tag) extends Table[UserMessageRow](tag, GlobalDBs.cloud_sourcing_user_schema, _tableName = "user_message"){
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

  /** TODO: 下面几个方法感觉是需要弄到另一个表格里面去。等到数据库拆分的时候可以这么做 */
  def addJudger():Try[Unit]= addMessage(JudgeMessage(), "admin", "成功", successful = true)
  def addCredit():Try[Unit]= addMessage(CreditUploadMessage(),"admin", "成功", successful = true)
  def getLastJudgeTime: Option[DateTime] =
    ServiceUtils.exec(userMessageTable.filter(_.content===JudgeMessage()).map(_.time).result.headOption)
  def getLastUploadTime: Option[DateTime] =
    ServiceUtils.exec(userMessageTable.filter(_.content===CreditUploadMessage()).map(_.time).result.headOption)
  def updateUploadTime(): DBIO[Int] =
    userMessageTable.filter(_.content===CreditUploadMessage()).map(m => m.time).update(DateTime.now())
  def updateJudgeTime(): DBIO[Int] =
    userMessageTable.filter(_.content===JudgeMessage()).map(m => m.time).update(DateTime.now())


  /** 算一下有哪些用户在这段时间有登陆记录 */
  def getToDelete(startDate: DateTime, endDate: DateTime): Array[String] = {
    val result = userMessageTable.filter(m => m.time >= startDate && m.time <= endDate). map(_.userID).distinct.result
    ServiceUtils.exec(result).filter(s=>s.nonEmpty && s!="admin").toArray
  }

  /** 算一下某个用户有哪些登陆记录 */
  def getActivitiesWithLimit(userID: String, startDate: DateTime): Array[DateTime] = {
    val result = userMessageTable.filter(m => m.userID=== userID && m.time >= startDate).sortBy(_.time.asc).map(_.time).result.map(_.toArray)
    ServiceUtils.exec(result)
  }


  /************************** 计算工作时间用 **************************/
  def getWorkTime(userID: String, days:Int): Period = {
    val timeThreshold = DateTime.now.minusDays(days)
    calculateWorkTime(getActivitiesWithLimit(userID, timeThreshold))
  }

  /** 每一个DateTime是用户的每一次活动，所得工作时间为每个DateTime前后5分钟(Globals.workTimeInterval)取并 */
  def calculateWorkTime(activities: Array[DateTime]): Period = {
    val interval = GlobalTimes.workTimeMinutesInterval

    val start = DateTime.now()
    var end = start

    /** 如果队列非空的话，加上头尾各10分钟 */
    if (activities.nonEmpty)
      end = end.plusMinutes(interval*2)

    /** 逐个算区间，如果区间有重合，按照重合部分计算，否则按2*interval计算 */
    for (i <- 0 until activities.length - 1)
      if (activities(i).plusMinutes(interval * 2).isBefore(activities(i + 1)))
        end = end.plusMinutes(interval * 2)
      else
        end = end.plus(new Period(activities(i), activities(i + 1)))

    new Period(start, end)
  }

}
