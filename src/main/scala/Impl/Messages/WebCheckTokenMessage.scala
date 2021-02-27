package Impl.WebAccountMessages

import Exceptions.UserNotExistedException
import Globals.GlobalVariables
import Impl.DisplayPortalMessage
import Plugins.CommonUtils.CommonTypes.ReplyMessage
import Plugins.CommonUtils.IOUtils
import Plugins.MSUtils.API
import Plugins.UserAccountAPI.LoginMessages._
import Utils.LocalUtils

import scala.util.Try


/**
 * [API]
 * [描述] 检查Token有效性
 * [返回值] token String
 */
case class WebCheckTokenMessage(userToken: String) extends DisplayPortalMessage {
  override def reaction(): Try[ReplyMessage] = Try{
    val userID = GlobalVariables.tokenUserMap.getOrElse(userToken, throw UserNotExistedException())
    ReplyMessage(0, "ok")
  }
}
