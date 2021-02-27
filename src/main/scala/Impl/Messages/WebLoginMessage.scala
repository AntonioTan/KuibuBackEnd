package Impl.WebAccountMessages

import Impl.DisplayPortalMessage
import Plugins.CommonUtils.CommonTypes.ReplyMessage
import Plugins.CommonUtils.IOUtils
import Plugins.MSUtils.API
import Plugins.UserAccountAPI.LoginMessages._
import Utils.LocalUtils

import scala.util.Try


/**
 * [API]
 * [描述] 登录账户
 * @param loginType String 登录类型
 * @param infoList  List[String] 登录需要的信息 邮箱和电话登录需要额外添加验证码
 * [返回值] token String
 */
case class WebLoginMessage(loginType: String, infoList: List[String]) extends DisplayPortalMessage {
  override def reaction(): Try[ReplyMessage] = Try{
    val replyMessage: ReplyMessage = loginType match {
      case "email" => ReplyMessage(0, API.request[EmailLoginMessage](infoList.head, infoList.last).get)
      case "cellphone" => ReplyMessage(0, API.request[CellphoneLoginMessage](infoList.head, infoList.last).get)
      case "nationalID" => ReplyMessage(0, API.request[NationalIDLoginMessage](infoList.head).get)
      case "userName" => ReplyMessage(0, API.request[UserNameLoginMessage](infoList.head, infoList.last).get)
    }
    LocalUtils.updateReplyMessageToken(replyMessage)
  }
}
