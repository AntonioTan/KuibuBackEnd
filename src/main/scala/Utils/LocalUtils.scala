package Utils
import java.io.File

import Impl.DisplayPortalMessage
import _root_.Message.fromObject
import Plugins.CommonUtils.CommonExceptions.{ExceptionWithCode, MessageException}
import Plugins.CommonUtils.CommonTypes.ReplyMessage
import Plugins.CommonUtils.StringUtils
import Plugins.EngineOperationAPI.AkkaEngineOperationMessages.{AkkaEngineOperationMessage, UpdateTreeObjectMessage}
import Plugins.EngineOperationAPI.EngineObjectGlobals.IDMap
import Plugins.EngineOperationAPI.ObjectClass.Medicine
import Plugins.EngineShared.EnumTypes.MedicineName
import Plugins.EngineShared.PropertyItem.{BasicItem, ClaimItem, MedicineItem}
import Plugins.EngineShared.StringObject.CandidateObjectTrait
import akka.http.scaladsl.model.HttpResponse

import scala.swing.Dialog

object LocalUtils {
  /** 根据failure的不同情况，得到不同的replyMessage，并且都记录下来 */
  def treatFailure(message:DisplayPortalMessage, e:Throwable):HttpResponse={
    e match {
      case exception:MessageException=>
        message.addMessage(exception.message, successful = false)
        fromObject(success = true, ReplyMessage(-1, exception.message))
      case exception:ExceptionWithCode=>
        message.addMessage(exception.code, successful = false)
        fromObject(success = true, ReplyMessage(-2, exception.getMessage))
      case exception: Throwable=>
        message.addMessage(StringUtils.exceptionToString(exception), successful = false)
        fromObject(success = true, StringUtils.exceptionToReplyCode(exception))
    }
  }

  /** 处理异常，主要就是显示在本地 */
  def handleException(e: Throwable): Unit = {
    e.printStackTrace()
  }
  /** 强制删除文件 */
  def deleteFile(fileName: String): Boolean = {
    val file = new File(fileName)
    if (file.exists())
      try {
        var deleteResult: Boolean = false
        var tryCount = 0
        while (!deleteResult && tryCount < 10) {
          System.gc()
          deleteResult = file.delete()
          tryCount += 1
        }
        if (deleteResult)
          return true
        else Dialog.showMessage(null, "错误：删除文件失败！")
      } catch {
        case e: Exception =>
          handleException(e)
      }
    false
  }

  /** 打印basic item，转换成字符串 */
  def printCandidateObjectTrait(item:CandidateObjectTrait):String={
    item match {
      case item:ClaimItem=>
        (if (item.negative) "【无】" else "")+IDMap(item.id.v).showName+
          (if (item.appendDetails()!="") ","+item.appendDetails() else "")
      case item:MedicineItem=>
        var st:String = (if (item.negative) "【减】" else "")+ IDMap(item.id.v).showName
        if (IDMap(item.id.v).asInstanceOf[Medicine].medicineType==MedicineName && item.amount.isDefined)
          st+= " "+ item.amount.get
        if (item.cookTime.isDefined)
          st+=","+item.cookTime.get
        st
      case item:BasicItem=>
        IDMap(item.id.v).showName
      case _=>
        item.toString
    }
  }

  /** 发送一个新的operation到engine */
  def send(message: AkkaEngineOperationMessage): Option[String]= {
    /** 如果是实习状态，是不能发送更新操作的 */
    None
  }

  def updateActions():Unit={
  }
  def timeOutAction():Unit={
  }
}
