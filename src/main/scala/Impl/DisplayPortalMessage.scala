package Impl


import Globals.{GlobalIOs, GlobalStrings, GlobalVariables}
import Impl.Messages.{BookImageMessage, GetAvailableBookCutQuestionPageListMessage}
import Impl.WebAccountMessages.{WebCheckTokenMessage, WebGetCurrentUserMessage, WebLoginMessage}
import Message.fromObject
import Plugins.CommonUtils.CommonExceptions.{ConnectionFailedException, ExceptionWithMessage}
import Plugins.CommonUtils.CommonTypes.{JacksonSerializable, ReplyMessage}
import Plugins.CommonUtils.Hub.{ServiceCenter, ServiceType}
import Plugins.CommonUtils.IOUtils
import Plugins.Encryption.Combine
import Tables.UserMessageTable
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future, Promise}
import scala.util.{Failure, Success, Try}
import Utils.LocalUtils
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.unmarshalling.Unmarshal

import scala.swing.Dialog


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[BookImageMessage], name = "BookImageMessage"),
    new JsonSubTypes.Type(value = classOf[GetAvailableBookCutQuestionPageListMessage], name = "GetAvailableBookCutQuestionPageListMessage"),
    new JsonSubTypes.Type(value = classOf[WebLoginMessage], name = "WebLoginMessage"),
    new JsonSubTypes.Type(value = classOf[WebGetCurrentUserMessage], name = "WebGetCurrentUserMessage"),
    new JsonSubTypes.Type(value = classOf[WebCheckTokenMessage], name = "WebCheckTokenMessage"),
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

  /** 这是一般情况下的fulfill promise */
  def fulfillPromise(promise:Promise[HttpResponse]):Unit={
    reaction() match {
      /** 直接成功了，得到了一个replyMessage */
      case Success(message)=>
        addMessage(returnMessage="成功", successful = true)
        promise.success(fromObject(success = true, message))
      case Failure(e:Throwable)=>
        /** 根据failure的具体情况，得到不同的replay message */
        promise.success(LocalUtils.treatFailure(this, e))
    }
  }

  /** 这里定义了一个promise，用于自动分一个线程去做fulfill promise的事情 */
  def processResult() : Try[HttpResponse] = Try{
    val promise = Promise[HttpResponse]()
    val result = promise.future
    fulfillPromise(promise)
    Await.result(result, Duration.create(10, scala.concurrent.duration.SECONDS))
  }


}





