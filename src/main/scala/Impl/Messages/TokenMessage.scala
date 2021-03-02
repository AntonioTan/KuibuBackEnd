package Impl.Messages

import Exceptions.{FrequentLoginException, UserNotExistedException}
import Globals.{GlobalIOs, GlobalRules, GlobalVariables}
import Impl.DisplayPortalMessage
import Plugins.CommonUtils.CommonTypes.ReplyMessage
import Plugins.MSUtils.{API, MailSender}
import Plugins.UserAccountAPI.GetInfoMessages.GetUserIDByTokenMessage
import Tables.UserMessageTable
import com.fasterxml.jackson.annotation.JsonIgnore

import scala.util.Try

abstract class TokenMessage(val userToken: String ) extends DisplayPortalMessage {
  @JsonIgnore
  var userID:String=""
  override def processResult(): Try[ReplyMessage] = Try{
    userID=GlobalVariables.tokenUserMap.getOrElse(userToken, throw UserNotExistedException())
    if (UserMessageTable.countUserIDLoginRecord(userID).get>GlobalRules.maximumDailyRequest)
      MailSender.emailWarning(throw FrequentLoginException())
    reaction().get
  }
  override def addMessage(returnMessage:String, successful:Boolean): Try[Unit] =UserMessageTable.addMessage(this,userID, returnMessage, successful)

}
