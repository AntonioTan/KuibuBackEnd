package Impl.Messages.WebAccountMessages

import Exceptions.UserNotExistedException
import Globals.GlobalVariables
import Impl.Messages.TokenMessage
import Plugins.CommonUtils.CommonTypes.ReplyMessage

import scala.util.Try

/**
 * [API]
 * [描述] 检查Token有效性
 * [返回值] token String
 */
case class WebCheckTokenMessage(override val userToken: String) extends TokenMessage(userToken) {
  override def reaction(): Try[ReplyMessage] = Try {
    GlobalVariables.tokenUserMap.getOrElse(userToken, throw UserNotExistedException())
    ReplyMessage(0, "ok")
  }
}
