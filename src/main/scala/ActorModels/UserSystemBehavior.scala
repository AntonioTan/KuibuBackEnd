package ActorModels

import ActorModels.UserBehavior.{Complete, Fail, UserChatMessage, UserChatProtocol, UserCommand, UserNotifierMessage, UserPushCompleteMessage, UserPushFailMessage, UserWsCompleteMessage, UserWsFailMessage, onUserPushFail}
import Plugins.CommonUtils.IOUtils
import akka.NotUsed
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.client.RequestBuilding.WithTransformation
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.GraphDSL.Implicits.port2flow
import akka.stream.{FlowShape, OverflowStrategy, SourceShape}
import akka.stream.scaladsl.{Flow, GraphDSL, Sink, Source}
import akka.stream.typed.scaladsl.{ActorFlow, ActorSink, ActorSource}

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration.DurationInt

object UserSystemBehavior {

  sealed trait UserSystemCommand

  case class UserAddedMessage(userID: String) extends UserSystemCommand

  var userMap: TrieMap[String, ActorRef[UserCommand]] = TrieMap.empty[String, ActorRef[UserCommand]]
//  var userFlowMap: TrieMap[String, Flow[]]

  def apply(): Behavior[UserSystemCommand] = {

    Behaviors.setup {
      context => {
        Behaviors.receiveMessage {
          case UserAddedMessage(userID: String) =>
            val newUser: ActorRef[UserCommand] = context.spawn(UserBehavior(), userID)
            val source: Source[UserChatProtocol, ActorRef[UserChatProtocol]] = ActorSource.actorRef[UserChatProtocol](completionMatcher = {
              case Complete =>
            }, failureMatcher = {
              case Fail(ex) => ex
            }, bufferSize = 8, overflowStrategy = OverflowStrategy.fail)
            Flow.fromGraph(GraphDSL.create(source){
              implicit builder =>
                (pushSource: SourceShape[UserChatProtocol]) =>

                  implicit val timeout: akka.util.Timeout = 1.second

                  val flowFromWs: FlowShape[Message, UserChatMessage] = builder.add(
                    Flow[Message].map{
                      case TextMessage.Strict(text: String) => UserChatMessage(text)
                      case BinaryMessage.Strict(text) => UserChatMessage("")
                    }
                  )
                  val flowToUser = builder.add(ActorFlow.ask(newUser)(makeMessage = (el: UserChatMessage, replyTo: ActorRef[Message])=>el))
                  val connectedWs: Flow[ActorRef[UserChatProtocol], UserNotifierMessage, NotUsed] = Flow[ActorRef[UserChatProtocol]].map((actor: ActorRef[UserChatProtocol]) => UserNotifierMessage(actor))
                  val pushActorSink = ActorSink.actorRef[UserCommand](ref = newUser, onCompleteMessage = UserPushCompleteMessage, onFailureMessage = onUserPushFail )

                  builder.materializedValue ~> connectedWs ~> pushActorSink
                FlowShape(flowFromWs.in, flowFromWs.out)
            })

            Behaviors.same

        }
      }
    }
  }

}
