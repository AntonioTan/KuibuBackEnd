package Impl.WebAccountMessages

import Impl.ChatPortalMessage
import Plugins.CommonUtils.CommonTypes.ReplyMessage
import Plugins.MSUtils.API
import Plugins.UserAccountAPI.VerificationMessages.CellphoneVerificationMessage

import scala.util.Try

/**
 * [API]
 * [描述] 发送验证码
 * @param cellphone String 用户的手机号码
 * [返回值] "发送验证码成功！" String
 **/
case class CellphoneVerificationCodeMessage(cellphone: String) extends ChatPortalMessage {
  override def reaction(): Try[ReplyMessage] = Try {
//    ReplyMessage(0, API.request[CellphoneVerificationMessage](cellphone).get)
    ReplyMessage(-1, "Disabled Mannually")
  }
}
