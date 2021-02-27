package Impl.Messages

import Exceptions.FrequentLoginException
import Globals.{GlobalIOs, GlobalRules}
import Impl.DisplayPortalMessage
import Plugins.MSUtils.{API, MailSender}
import Plugins.UserAccountAPI.GetInfoMessages.GetUserIDByTokenMessage
import Tables.UserMessageTable
import Utils.LocalUtils
import akka.http.scaladsl.model.HttpResponse
import com.fasterxml.jackson.annotation.JsonIgnore

import scala.util.{Failure, Success, Try}

abstract class TokenMessage(val userToken: String = GlobalIOs.userToken) extends DisplayPortalMessage {
  @JsonIgnore
  var userID:String=""
  override def processResult(): Try[HttpResponse] = Try{
    API.request[GetUserIDByTokenMessage](userToken) match {
      case Success(value)=>
        userID=value
        if (UserMessageTable.countUserIDLoginRecord(userID).get>GlobalRules.maximumDailyRequest)
          MailSender.emailWarning(throw FrequentLoginException())
        super.processResult().get
      case Failure(e:Throwable)=>
        LocalUtils.treatFailure(this, e)
    }
  }
  override def addMessage(returnMessage:String, successful:Boolean): Try[Unit] =UserMessageTable.addMessage(this,userID, returnMessage, successful)

}
