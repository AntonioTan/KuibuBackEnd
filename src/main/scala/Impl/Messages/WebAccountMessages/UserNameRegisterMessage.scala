package Impl.WebAccountMessages

import Exceptions.PasswordNotMatchedException
import Impl.DisplayPortalMessage
import Plugins.CommonUtils.CommonTypes.ReplyMessage
import Plugins.MSUtils.API
import Plugins.UserAccountAPI.RegisterMessages.UserNameRegisterMessage
import Utils.LocalUtils

import scala.util.Try

/**
 * [API]
 * [描述] 用户注册
 * @param userName String 用户名
 * @param password String 密码
 * [返回值] 用户登录的token String
 */
case class UsernameRegisterMessage(userName: String, password: String, confirm: String) extends DisplayPortalMessage {
  override def reaction(): Try[ReplyMessage] = Try{
    if (password != confirm) throw PasswordNotMatchedException()
//    ReplyMessage(0, API.request[UserNameRegisterMessage](userName, password).get)
    ReplyMessage(-1, "Disabled Mannually")
  }
}
