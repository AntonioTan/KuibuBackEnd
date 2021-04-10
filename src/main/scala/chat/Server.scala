package chat

import java.time.LocalTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME

import ActorModels.{ChatSystemBehavior, SystemBehavior, UserBehavior, UserSystemBehavior, UserWebGuardianBehavior, UserWebRequestBehavior, UserWebSystem}
import ActorModels.ChatSystemBehavior.{ChatSystemAskingMessage, ChatSystemChatMessage, ChatSystemChatRoomAdded, ChatSystemCommand, ChatSystemComplete, ChatSystemFail, ChatSystemGetChatRoom, ChatSystemInit, ChatSystemProtocolMessage}
import ActorModels.SystemBehavior.{SystemCondition, SystemStart}
import ActorModels.UserBehavior.{UserCommand, UserWsPushMessage}
import ActorModels.UserSystemBehavior.{UserAddedMessage, UserFlowResponseMessage}
import ActorModels.UserWebGuardianBehavior.UserWebRequestGenerateMessage
import ActorModels.UserWebRequestBehavior.{UserWebCommand, UserWebLoginCommand}
import Globals.GlobalVariables
import Globals.GlobalVariables.{userSystem, userWebGuardian}
import Impl.ChatPortalMessage
import Impl.Messages.WebAccountMessages.WebLoginMessage
import Plugins.CommonUtils.IOUtils
import Plugins.MSUtils.AkkaBase.AkkaUtils
import Plugins.MSUtils.AkkaBase.AkkaUtils.system
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.{Done, NotUsed, actor}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Props, SpawnProtocol, SupervisorStrategy}
import akka.actor.typed.scaladsl.adapter.{ClassicActorRefOps, PropsAdapter, TypedActorSystemOps}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives.{complete, concat, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.stream.{FlowShape, Materializer, OverflowStrategy}
import akka.stream.scaladsl.MergeHub.source
import akka.stream.scaladsl._
import akka.stream.typed.scaladsl.{ActorFlow, ActorSink, ActorSource}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.io.StdIn
import scala.util.{Failure, Success, Try}

object Server {
  def main(args: Array[String]): Unit = {

//    println(GlobalVariables.chatSystem.hashCode())

//    val userWebSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(UserWebSystem(), "UserWebSystem")
    println(IOUtils.serialize(WebLoginMessage("hh", List.apply[String]("jj"))).get)
    println(IOUtils.deserialize[UserCommand]("{\"type\":\"UserChatMessage\",\"content\":\"Hello\"}"))

    system ! SystemCondition("System Start")
    system ! SystemStart()

    //    }
    Thread.sleep(1000)

//    import akka.actor.typed.scaladsl.AskPattern._
//    implicit val pathUserWebSystem: ActorSystem[SystemBehavior.SystemCommand] = system
//    implicit val ec: ExecutionContext = system.executionContext
//    implicit val timeout: Timeout = Timeout(1.seconds)
//    val addedResponse: Future[UserSystemBehavior.UserFlowResponseMessage] = userSystem.askWithStatus(ref => UserAddedMessage(userID = "001", ref))
//
//    addedResponse.onComplete((a: Try[UserFlowResponseMessage]) =>{
//
//      println(a.get.userFlow)
//      Source.single(TextMessage("Hello")).via(
//        a.get.userFlow).to(Sink.foreach(println(_)))
//    })
//    Source(List(1, 2, 3)).map(_ + 1).async.map(_ * 2).to(Sink.foreach(println(_)))

    val route: Route = concat(
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
                  val chatRoomFlow = ChatSystemBehavior.getChatRoomFlow(receiverID)
                  handleWebSocketMessages(Flow[Message].via(chatSystemAddedFlow).via(chatRoomFlow.keepAlive(20.seconds, ()=>TextMessage("Hello"))
                    //                  chatSystemAddedFlow.via(ChatSystemActor.getChatRoomFlow(receiverID))
                  ))
                }
              }
            }
          },
        )

      },
        pathPrefix("events") {
        get {
          import akka.actor.typed.scaladsl.AskPattern._
          implicit val pathUserWebSystem: ActorSystem[SystemBehavior.SystemCommand] = system
          implicit val ec: ExecutionContext = system.executionContext
          implicit val timeout: Timeout = Timeout(3.seconds)
          // 先用userWebGuardian生成一个可以handle webrequest的actor
          val userWebRequestActor: Future[UserWebGuardianBehavior.UserWebRequestGenerateResponse] = userWebGuardian.askWithStatus(ref => UserWebRequestGenerateMessage(ref))
          onComplete(userWebRequestActor) {
            case Success(UserWebGuardianBehavior.UserWebRequestGenerateResponse(newRequestActor)) =>
              onComplete(newRequestActor.askWithStatus(ref => UserWebLoginCommand("Hello", ref))) {
                case Success(value) =>
                  complete(value.toString)
                case Failure(exception) => complete(InternalServerError, "Failed to create feedback!")
              }
            case Failure(exception) => complete(InternalServerError, "Failed to create feedback!")
          }
        }
      },
      pathPrefix("test") {
        get {
          parameters(
            "userID"
          ) {
            (userID: String) => {
              import akka.actor.typed.scaladsl.AskPattern._
              implicit val pathUserWebSystem: ActorSystem[SystemBehavior.SystemCommand] = system
              implicit val ec: ExecutionContext = system.executionContext
              implicit val timeout: Timeout = Timeout(1.seconds)
              val addedResponse: Future[UserSystemBehavior.UserFlowResponseMessage] = userSystem.askWithStatus(ref => UserAddedMessage(userID = userID, ref))
              onComplete(addedResponse) {
                case Success(UserFlowResponseMessage(userFlow)) =>
                  handleWebSocketMessages(userFlow)
                case Failure(exception) =>
                  complete(InternalServerError, "Failed to connect to Server!")
              }

            }
          }
        }
      }
    )

    implicit val materializer: Materializer = Materializer(system)
    implicit val sys2: actor.ActorSystem = system.toClassic
    val binding = Await.result(Http().bindAndHandle(route, "127.0.0.1", 8080), 3.seconds)


    // the rest of the sample code will go here
    println("Started server at 127.0.0.1:8080, press enter to kill server")
    StdIn.readLine()
    system.terminate()
  }
}
