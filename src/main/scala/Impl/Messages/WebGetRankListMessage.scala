package Impl.Messages

import Exceptions.UserNotExistedException
import Globals.{GlobalStrings, GlobalVariables}
import Plugins.CommonUtils.CommonExceptions.ConnectionFailedException
import Plugins.CommonUtils.CommonTypes.{JacksonSerializable, ReplyMessage}
import Plugins.CommonUtils.IOUtils
import Plugins.Encryption.Combine
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.Try

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[GetRankListMessage], name = "GetRankListMessage"),
  ))
abstract class Message() extends JacksonSerializable
case class GetRankListMessage(days: Int, orderType: Int, userToken: String) extends Message

case class WebGetRankListMessage(days: Int, orderType: Int, override val userToken: String) extends TokenMessage(userToken){
  /** 定义需要采取的action */
  override def reaction(): Try[ReplyMessage] = Try {
    implicit val system: ActorSystem = GlobalVariables.requestAkka
    implicit val ec: ExecutionContextExecutor = system.dispatcher
    val userID = GlobalVariables.tokenUserMap.getOrElse(userToken, throw UserNotExistedException())
    val result: Future[HttpResponse] = Http().singleRequest(HttpRequest(method=HttpMethods.POST,uri="http://localhost:6070/users",
      entity= Combine.encrypt(IOUtils.serialize(GetRankListMessage(days, orderType, userID)).get, GlobalStrings.serverPublic)))
    val returnMessage=Await.result(result, Duration.create(180, scala.concurrent.duration.SECONDS))
    if (returnMessage.status != StatusCodes.OK)
      throw ConnectionFailedException()
    val waitedOutcome=Await.result(Unmarshal(returnMessage).to[String], Duration.Inf)
    IOUtils.deserialize[ReplyMessage](Combine.decrypt(waitedOutcome, GlobalStrings.clientPrivate)).get
  }
}
