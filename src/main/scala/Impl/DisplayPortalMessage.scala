package Impl


import java.lang.annotation.Annotation

import Impl.Messages.WebAccountMessages.{WebCheckTokenMessage, WebGetUserInfoMessage, WebLoginMessage}
import Impl.Messages._
import Impl.WebAccountMessages.{CellphoneVerificationCodeMessage, UsernameRegisterMessage}
import Plugins.CommonUtils.CommonTypes.{JacksonSerializable, ReplyMessage}
import Plugins.CommonUtils.Hub.{ServiceCenter, ServiceType}
import Plugins.CommonUtils.IOUtils
import Tables.UserMessageTable
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}

import scala.util.Try


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[BookImageMessage], name = "BookImageMessage"),
    new JsonSubTypes.Type(value = classOf[GetAvailableBookCutQuestionPageListMessage], name = "GetAvailableBookCutQuestionPageListMessage"),
    new JsonSubTypes.Type(value = classOf[WebLoginMessage], name = "WebLoginMessage"),
    new JsonSubTypes.Type(value = classOf[CellphoneVerificationCodeMessage], name = "CellphoneVerificationCodeMessage"),
    new JsonSubTypes.Type(value = classOf[UsernameRegisterMessage], name = "UsernameRegisterMessage"),
    new JsonSubTypes.Type(value = classOf[WebGetUserInfoMessage], name = "WebGetUserInfoMessage"),
    new JsonSubTypes.Type(value = classOf[WebCheckTokenMessage], name = "WebCheckTokenMessage"),
    new JsonSubTypes.Type(value = classOf[WebGetRankListMessage], name = "WebGetRankListMessage"),
    new JsonSubTypes.Type(value = classOf[WebDecryptionMessage], name = "WebDecryptionMessage"),
  ))
abstract class DisplayPortalMessage(val sender: ServiceType = ServiceCenter.serviceType) extends JacksonSerializable{

  /** 擦除敏感信息 */
  def eraseInformation(): Unit = {}

  /** 定义需要采取的action */
  def reaction(): Try[ReplyMessage]

  /** 将自己写入到messageTable里面去 */
  def addMessage(returnMessage:String, successful:Boolean):Try[Unit]= UserMessageTable.addMessage(this,"", returnMessage, successful)

  /** 把某个结果序列化成为message，然后返回。写在这里方便调用 */
  def serializeToReply(target:Object):Try[ReplyMessage]=Try{
    ReplyMessage(0, IOUtils.serialize(target).get)
  }

  /** 这里定义了一个promise，用于自动分一个线程去做fulfill promise的事情 */
  def processResult() : Try[ReplyMessage] = reaction()
}





