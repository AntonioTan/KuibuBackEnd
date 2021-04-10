package ActorModels

import ActorModels.UserBehavior.{ UserChatConvertMessage, UserChatMessage,  UserCommand, UserNotifierMessage, UserPushCompleteMessage, UserPushFailMessage, UserWsCompleteMessage, UserWsFailMessage, UserWsPushMessage, onUserPushFail}
import Plugins.CommonUtils.IOUtils
import akka.NotUsed
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.client.RequestBuilding.WithTransformation
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.pattern.StatusReply
import akka.stream.scaladsl.GraphDSL.Implicits.port2flow
import akka.stream.{FlowShape, Outlet, OverflowStrategy, SourceShape}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.typed.scaladsl.{ActorFlow, ActorSink, ActorSource}

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration.DurationInt

object UserSystemBehavior {

  sealed trait UserSystemCommand

  case class UserAddedMessage(userID: String, sender: ActorRef[StatusReply[UserFlowResponseMessage]]) extends UserSystemCommand

  case class UserFlowResponseMessage(userFlow: Flow[Message, Message, ActorRef[UserCommand]]) extends UserSystemCommand

  var userMap: TrieMap[String, ActorRef[UserCommand]] = TrieMap.empty[String, ActorRef[UserCommand]]
//  var userFlowMap: TrieMap[String, Flow[]]

  def apply(): Behavior[UserSystemCommand] = {
    Behaviors.setup {
      context => {
        Behaviors.receiveMessage {
          case UserAddedMessage(userID: String, sender: ActorRef[StatusReply[UserFlowResponseMessage]]) =>
            val newUser: ActorRef[UserCommand] = context.spawn(UserBehavior(), userID)
            val source: Source[UserCommand, ActorRef[UserCommand]] = ActorSource.actorRef[UserCommand](completionMatcher = {
              case UserWsCompleteMessage =>
            }, failureMatcher = {
              case UserWsFailMessage(ex) => ex
            }, bufferSize = 8, overflowStrategy = OverflowStrategy.fail)
            val userFlow: Flow[Message, Message, ActorRef[UserCommand]] = Flow.fromGraph(GraphDSL.create(source){
              implicit builder =>
                (pushSource: SourceShape[UserCommand]) =>
                  import GraphDSL.Implicits._

                  implicit val timeout: akka.util.Timeout = 1.second

                  val flowFromWs: FlowShape[Message, UserChatMessage] = builder.add(
                    Flow[Message].map{
                      case TextMessage.Strict(text: String) =>
                        UserChatMessage(text)
                      case BinaryMessage.Strict(text) => UserChatMessage("")
                    }.buffer(1024 * 1024, OverflowStrategy.fail)
                  )
                  val flowToUser: FlowShape[UserChatMessage, Message] = builder.add(ActorFlow.ask(newUser)(makeMessage = (el: UserChatMessage, replyTo: ActorRef[Message])=>UserChatConvertMessage(el, replyTo)))

                  val connectedWs: Flow[ActorRef[UserCommand], UserNotifierMessage, NotUsed] = Flow[ActorRef[UserCommand]].map((actor: ActorRef[UserCommand]) => UserNotifierMessage(actor, userID))

                  val pushActorSink = ActorSink.actorRef[UserCommand](ref = newUser, onCompleteMessage = UserPushCompleteMessage, onFailureMessage = onUserPushFail )

                  val pushToUser: FlowShape[UserCommand, UserChatMessage] = builder.add(Flow[UserCommand].collect{
                    case UserWsPushMessage(content: String) => UserChatMessage(content)
                  })

                  val mergeToUser = builder.add(Merge[UserChatMessage](2))

                  flowFromWs ~> mergeToUser.in(0)
                  pushSource ~> pushToUser ~> mergeToUser.in(1)
                  mergeToUser ~> flowToUser
                  // 添加推送用actor
                  builder.materializedValue ~> connectedWs ~> pushActorSink
                FlowShape(flowFromWs.in, flowToUser.out)
            })
            sender ! StatusReply.success(UserFlowResponseMessage(userFlow))
            Behaviors.same
        }
      }
    }
  }

}
