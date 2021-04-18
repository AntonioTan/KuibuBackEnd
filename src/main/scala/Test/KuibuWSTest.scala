package Test

import ActorModels.ChatSystemBehavior.ChatSystemAskingMessage
import ActorModels.SystemBehavior.{SystemCondition, SystemStart}
import ActorModels.UserBehavior.{Structure, UserCommand, UserTestMessage}
import ActorModels.UserSystemBehavior.{UserAddedMessage, UserFlowResponseMessage}
import ActorModels.UserWebGuardianBehavior.UserWebRequestGenerateMessage
import ActorModels.UserWebRequestBehavior.{UserWebCommand, UserWebLoginCommand, UserWebMessage}
import ActorModels.{ChatSystemBehavior, SystemBehavior, UserSystemBehavior, UserWebGuardianBehavior}
import Globals.GlobalVariables
import Globals.GlobalVariables.{userSystem, userWebGuardian}
import Impl.Messages.WebAccountMessages.WebLoginMessage
import KuibuProcess.LocalTestPath
import Plugins.CommonUtils.CommonTypes.UserPath
import Plugins.CommonUtils.Hub.ServiceCenter.{portMap, treeObjectServiceCode, userAccountServiceCode}
import Plugins.CommonUtils.IOUtils
import Plugins.MSUtils.AkkaBase.AkkaUtils.system
import Utils.DBUtils
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives.{complete, concat, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl._
import akka.stream.typed.scaladsl.ActorFlow
import akka.util.Timeout
import akka.{NotUsed, actor}
import org.joda.time.DateTime

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.StdIn
import scala.util.{Failure, Success}

object KuibuWSTest {
  def main(args: Array[String]): Unit = {

    UserPath.chosenPath=LocalTestPath()
    // 测试阶段 之后删除
    DBUtils.dropKuibuDatabase()
    DBUtils.initKuibuDatabase()
    Thread.sleep(10000)
    println(IOUtils.serialize(WebLoginMessage("hh", List.apply[String]("jj"))).get)
    println(IOUtils.deserialize[UserCommand]("{\"type\":\"UserChatMessage\",\"content\":\"Hello\"}"))
    import org.joda.time.format.DateTimeFormat
    import org.joda.time.format.DateTimeFormatter
    val formatter = DateTimeFormat.forPattern("yyyy/MM/dd")
    val dt = formatter.parseDateTime("2021/04/22")
    println(IOUtils.serialize(UserTestMessage(Structure(List.apply("Hello")), dt)).get)
    system ! SystemCondition("System Start")
    system ! SystemStart()

    Thread.sleep(1000)


    val route: Route = concat(
      pathPrefix("KuibuProcess") {
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
      pathPrefix("web") {
        concat(
          get {
            import akka.actor.typed.scaladsl.AskPattern._
            implicit val pathUserWebSystem: ActorSystem[SystemBehavior.SystemCommand] = system
            implicit val ec: ExecutionContext = system.executionContext
            implicit val timeout: Timeout = Timeout(3.seconds)
            // 先用userWebGuardian生成一个可以handle webrequest的actor
            val userWebRequestActor: Future[UserWebGuardianBehavior.UserWebRequestGenerateResponse] = userWebGuardian.askWithStatus(ref => UserWebRequestGenerateMessage(ref))
            onComplete(userWebRequestActor) {
              case Success(UserWebGuardianBehavior.UserWebRequestGenerateResponse(newRequestActor)) =>
                onComplete(newRequestActor.askWithStatus(ref => UserWebMessage(UserWebLoginCommand("Hello"), ref))) {
                  case Success(value) =>
                    complete(value.toString)
                  case Failure(exception) => complete(InternalServerError, "Failed to create feedback!")
                }
              case Failure(exception) => complete(InternalServerError, "Failed to create feedback!")
            }
          },
          post {
            entity(as[String]) {
              bytes: String => {
                import akka.actor.typed.scaladsl.AskPattern._
                implicit val pathUserWebSystem: ActorSystem[SystemBehavior.SystemCommand] = system
                implicit val ec: ExecutionContext = system.executionContext
                implicit val timeout: Timeout = Timeout(3.seconds)
                val message: UserWebCommand = IOUtils.deserialize[UserWebCommand](bytes).get
                val userWebRequestActor: Future[UserWebGuardianBehavior.UserWebRequestGenerateResponse] = userWebGuardian.askWithStatus(ref => UserWebRequestGenerateMessage(ref))
                onComplete(userWebRequestActor) {
                  case Success(UserWebGuardianBehavior.UserWebRequestGenerateResponse(newRequestActor)) =>
                    onComplete(newRequestActor.askWithStatus(ref => UserWebMessage(message, ref))) {
                      case Success(value) =>
                        complete(IOUtils.serialize(value))
                      case Failure(exception) => complete(InternalServerError, exception.getMessage)
                    }
                  case Failure(exception) => complete(InternalServerError, "Failed to create feedback!")
                }
              }
            }
          }
        )
      },
      pathPrefix("ws") {
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
