package chat

import java.time.LocalTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME

import ActorModels.{ChatSystemActor, SystemBehavior, UserWebRequestBehavior, UserWebSystem}
import ActorModels.ChatSystemActor.{ChatSystemAskingMessage, ChatSystemChatMessage, ChatSystemChatRoomAdded, ChatSystemCommand, ChatSystemComplete, ChatSystemFail, ChatSystemGetChatRoom, ChatSystemInit, ChatSystemProtocolMessage}
import ActorModels.UserWebRequestBehavior.{UserWebCommand, UserWebLoginCommand}
import Globals.GlobalVariables
import Impl.Messages.WebAccountMessages.WebLoginMessage
import Plugins.CommonUtils.IOUtils
import Plugins.MSUtils.AkkaBase.AkkaUtils
import Plugins.MSUtils.AkkaBase.AkkaUtils.system
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.{NotUsed, actor}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Props, SpawnProtocol, SupervisorStrategy}
import akka.actor.typed.scaladsl.adapter.{ClassicActorRefOps, PropsAdapter, TypedActorSystemOps}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives.{complete, concat, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.MergeHub.source
import akka.stream.scaladsl._
import akka.stream.typed.scaladsl.{ActorFlow, ActorSink, ActorSource}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.io.StdIn
import scala.util.{Failure, Success}


object Server {
  def main(args: Array[String]): Unit = {

//    println(GlobalVariables.chatSystem.hashCode())

    implicit val userWebSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(UserWebSystem(), "UserWebSystem")
    println(IOUtils.serialize(WebLoginMessage("hh", List.apply[String]("jj"))).get)
    println(IOUtils.deserialize[WebLoginMessage]("{\"type\":\"WebLoginMessage\",\"loginType\":\"hh\",\"infoList\":[\"jj\"],\"sender\":\"000010\"}"))

    Thread.sleep(1000)
    system ! ChatSystemChatRoomAdded("002")
    Thread.sleep(1000)
    system ! ChatSystemChatRoomAdded("003")
    //    }

    val route: Route = {
      concat(
        pathPrefix("chat") {
          concat(
            pathPrefix("msg") {
              get {
                parameters(
                  "senderID", "receiverID"
                ) {
                  (senderID, receiverID) => {
                    println(senderID)
                    implicit val timeout: akka.util.Timeout = 1.second
                    val chatSystemAddedFlow: Flow[Message, Message, NotUsed] = {
                      ActorFlow.ask(GlobalVariables.chatSystem)(
                        makeMessage = (el, replyTo: ActorRef[Message]) => ChatSystemAskingMessage(el, receiverID, replyTo))
                    }
                    val chatRoomFlow = ChatSystemActor.getChatRoomFlow(receiverID)
                    handleWebSocketMessages(Flow[Message].via(chatSystemAddedFlow).via(chatRoomFlow.keepAlive(20.seconds, ()=>TextMessage("Hello"))
                      //                  chatSystemAddedFlow.via(ChatSystemActor.getChatRoomFlow(receiverID))
                    ))
                  }
                }
              }
            },
//            pathPrefix("create") {
//              get {
//                parameters("userID") {
//
//                }
//              }
//            }
          )

        },
          pathPrefix("events") {
          get {
            import akka.actor.typed.scaladsl.AskPattern._
            implicit val ec: ExecutionContext = system.executionContext
            implicit val timeout: Timeout = Timeout(3.seconds)
            val userWebRequestActor: Future[ActorRef[UserWebCommand]] = userWebSystem.ask(SpawnProtocol.Spawn(UserWebRequestBehavior(), name = "", props=Props.empty, _))
            onComplete(userWebRequestActor) {
              case Success(requestActor) =>
                onComplete(requestActor.askWithStatus(ref => UserWebLoginCommand("Hello", ref))) {
                  case Success(value) =>
                    complete(value.toString)
                  case Failure(exception) => complete(InternalServerError, "Failed to create feedback!")
                }
              case Failure(exception) => complete(InternalServerError, "Failed to create feedback!")
            }
          }
        }
      )
    }

    implicit val materializer: Materializer = Materializer(system)
    implicit val sys2: actor.ActorSystem = system.toClassic
    val binding = Await.result(Http().bindAndHandle(route, "127.0.0.1", 8080), 3.seconds)


    // the rest of the sample code will go here
    println("Started server at 127.0.0.1:8080, press enter to kill server")
    StdIn.readLine()
    system.terminate()
  }
}
