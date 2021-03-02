package Impl.Messages.WebAccountMessages

import Exceptions.UserNotExistedException
import Globals.GlobalVariables
import Impl.Messages.TokenMessage
import Plugins.CommonUtils.CommonTypes.ReplyMessage
import Plugins.CommonUtils.Hub.ServiceType
import Plugins.MSUtils.API
import Plugins.UserAccountAPI.GetInfoMessages.GetRealNameMessage

import scala.util.Try

/**
 * [API]
 * [描述]获取当前用户信息
 * [返回值] token String
 */
case class WebGetCurrentUserMessage(override val userToken: String) extends TokenMessage {
  override def reaction(): Try[ReplyMessage] = Try {
    val userID = GlobalVariables.tokenUserMap.getOrElse(userToken, throw UserNotExistedException())
    val realName = API.request[GetRealNameMessage](userID).get
    ReplyMessage(0, "{\"realName\": \"" + realName + "\"}")
  }
}
