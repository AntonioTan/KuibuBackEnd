package Impl.Messages.WebAccountMessages

import Exceptions.UserNotExistedException
import Globals.GlobalVariables
import Impl.Messages.{TokenMessage}
import Plugins.CommonUtils.CommonTypes.ReplyMessage
import Plugins.CommonUtils.Hub.ServiceType
import Plugins.MSUtils.API
import Plugins.UserAccountAPI.AkkaAccountMessage
import Plugins.UserAccountAPI.GetInfoMessages.{GetEmailMessage, GetNationalIDMessage, GetPhoneMessage, GetRealNameMessage}

import scala.util.Try

/**
 * [API]
 * [描述]获取当前用户信息
 * [返回值] token String
 */
case class WebGetUserInfoMessage(override val userToken: String, infoType: String) extends TokenMessage(userToken ) {
  override def reaction(): Try[ReplyMessage] = Try {
    val userID = GlobalVariables.tokenUserMap.getOrElse(userToken, throw UserNotExistedException())

    val replyMessage: ReplyMessage = infoType match {
      case "cellphone" => ReplyMessage(0, API.request[GetPhoneMessage](userID).get)
      case "nationalID" => ReplyMessage(0, API.request[GetNationalIDMessage](userID).get)
      case "realName" => ReplyMessage(0, API.request[GetRealNameMessage](userID).get)
      case "email" => ReplyMessage(0, API.request[GetEmailMessage](userID).get)
      case "avatar" => ReplyMessage(0, "http://www.qianfang.space:3000/images/logo.png")
    }
    println(replyMessage)
    replyMessage
  }
}
