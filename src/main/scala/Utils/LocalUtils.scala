package Utils
import Globals.{GlobalRules, GlobalVariables}
import Plugins.CommonUtils.CommonTypes.ReplyMessage
import Plugins.CommonUtils.StringUtils
import Plugins.MSUtils.MailSender
object LocalUtils {
  /** 处理异常，发送异常邮件 */
  def handleException(e: Throwable): Unit = MailSender.emailWarning(e)

  def updateReplyMessageToken(message:ReplyMessage):ReplyMessage={
    if (message.info.contains(",")){
      ReplyMessage(message.status,
        message.info.split(",").map{st=>
          LocalUtils.updateLocalToken(st.split("-").head)+ st.substring(st.indexOf("-"))
        }.reduce(_+","+_))
    } else
      ReplyMessage(message.status, LocalUtils.updateLocalToken(message.info))
  }
  def updateLocalToken(userToken:String):String={
    var newToken = StringUtils.randomString(GlobalRules.tokenLength)
    while (GlobalVariables.tokenUserMap.contains(newToken)) newToken = StringUtils.randomString(GlobalRules.tokenLength)
    GlobalVariables.tokenUserMap=GlobalVariables.tokenUserMap++Map(newToken-> userToken)
    newToken
  }
}
