package Impl.Messages

import Exceptions.UserNotExistedException
import Globals.GlobalVariables
import Impl.DisplayPortalMessage
import Plugins.CommonUtils.CommonTypes.ReplyMessage
import Plugins.CommonUtils.IOUtils
import Plugins.MSUtils.API
import Plugins.UserAccountAPI.GetInfoMessages.GetRealNameMessage
import Plugins.UserAccountAPI.LoginMessages._
import Utils.LocalUtils

import scala.util.Try


/**
 * [API]
 * [描述]获取当前用户信息
 * [返回值] token String
 */
case class WebGetCurrentUserMessage() extends TokenMessage{
  override def reaction(): Try[ReplyMessage] = Try{
    val userID = GlobalVariables.tokenUserMap.getOrElse(userToken, throw UserNotExistedException())
    val realName = API.request[GetRealNameMessage](userID).get
    ReplyMessage(0, "{\"realName\": \"" + realName + "\"}")
  }
}
