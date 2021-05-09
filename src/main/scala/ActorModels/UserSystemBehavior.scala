package ActorModels

import ActorModels.UserBehavior.{UserChatMessage, UserCommand, UserNotifierMessage, UserPushCompleteMessage, UserPushFailMessage, UserWsCompleteMessage, UserWsConvertMessage, UserWsFailMessage, UserWsInitializeMessage, UserWsInviteProjectMessage, UserWsPushChatMessage, UserWsPushMessage, onUserPushFail}
import ActorModels.UserSystemBehavior.{UserAddedMessage, UserFlowResponseMessage, UserInitializeResponseMessage, UserSystemCommand, UserSystemInitializeMessage, userFlowMap, userMap}
import Plugins.CommonUtils.CommonTypes.JacksonSerializable
import Plugins.CommonUtils.IOUtils
import Plugins.MSUtils.AkkaBase.AkkaUtils.system
import Tables.ProjectInfoTable
import akka.NotUsed
import akka.actor.TypedActor.self
import akka.actor.typed.pubsub.Topic
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, ChildFailed, Signal, Terminated}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.http.scaladsl.client.RequestBuilding.WithTransformation
import akka.http.scaladsl.model.ws.TextMessage.Strict
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.pattern.StatusReply
import akka.stream.scaladsl.GraphDSL.Implicits.port2flow
import akka.stream.{FlowShape, Materializer, Outlet, OverflowStrategy, SourceShape, UniformFanInShape}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.typed.scaladsl.{ActorFlow, ActorSink, ActorSource}
import akka.util.Timeout

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.Try


object UserSystemBehavior {

  sealed trait UserSystemCommand

  case class UserAddedMessage(userID: String, sender: ActorRef[StatusReply[UserFlowResponseMessage]]) extends UserSystemCommand

  case class UserSystemInitializeMessage(userID: String, projectID: String, sender: ActorRef[StatusReply[UserInitializeResponseMessage]]) extends UserSystemCommand

  case class UserFlowResponseMessage(userFlow: Flow[Message, Message, Any]) extends UserSystemCommand

  case class UserInitializeResponseMessage(outcome: Boolean) extends UserSystemCommand

  var userMap: TrieMap[String, ActorRef[UserCommand]] = TrieMap.empty[String, ActorRef[UserCommand]]
  var userFlowMap: TrieMap[String, Flow[Message, Message, Any]] = TrieMap.empty[String, Flow[Message, Message, Any]]

  def apply(): Behavior[UserSystemCommand] = {
    Behaviors.setup {
      context =>
        new UserSystemBehavior(context)
    }
  }

}

class UserSystemBehavior(context: ActorContext[UserSystemCommand]) extends AbstractBehavior[UserSystemCommand](context) {
  override def onMessage(msg: UserSystemCommand): Behavior[UserSystemCommand] =
    msg match {
      case UserAddedMessage(userID: String, sender: ActorRef[StatusReply[UserFlowResponseMessage]]) =>
        if (userMap.contains(userID)) {
          sender ! StatusReply.success(UserFlowResponseMessage(userFlowMap(userID)))
        } else {
          val newUser: ActorRef[UserCommand] = context.spawn(UserBehavior(), userID)
          context.watch(newUser)

          val convertFlow: Flow[Message, UserCommand, NotUsed] = Flow[Message].map{
            case TextMessage.Strict(text) =>
              println("received ws message", text)
              IOUtils.deserialize[UserCommand](text).get
          }
          val sin: Sink[UserCommand, Any] = ActorSink.actorRef[UserCommand](ref = newUser, onCompleteMessage = UserPushCompleteMessage, onFailureMessage = onUserPushFail)
          val incomingMessages: Sink[UserCommand, Any] = sin

          implicit val materializer: Materializer = Materializer(system)
          val actorAndSource: (ActorRef[UserCommand], Source[UserCommand, NotUsed]) = ActorSource.actorRef[UserCommand](completionMatcher = {
            case UserWsCompleteMessage =>
          }, failureMatcher = {
            case UserWsFailMessage(ex) => ex
          }, bufferSize = 8, overflowStrategy = OverflowStrategy.fail).preMaterialize()
          newUser ! UserNotifierMessage(actorAndSource._1, userID)

//          val outgoingMessages: Source[UserCommand, Unit] =
//            ActorSource.actorRef[UserCommand](completionMatcher = {
//                          case UserWsCompleteMessage =>
//                        }, failureMatcher = {
//                          case UserWsFailMessage(ex) => ex
//                        }, bufferSize = 8, overflowStrategy = OverflowStrategy.fail)
//              .mapMaterializedValue { outActor =>
//                // give the user actor a way to send messages out
//                newUser ! UserNotifierMessage(outActor, userID)
//              }
          val userFlow: Flow[Message, Message, Any] = convertFlow.via(Flow.fromSinkAndSource(incomingMessages, actorAndSource._2).map(
             command => {
               println("command", command)
               TextMessage.Strict(IOUtils.serialize(command).get)
             }
          ))
          //          val source: Source[UserCommand, ActorRef[UserCommand]] = ActorSource.actorRef[UserCommand](completionMatcher = {
//            case UserWsCompleteMessage =>
//          }, failureMatcher = {
//            case UserWsFailMessage(ex) => ex
//          }, bufferSize = 8, overflowStrategy = OverflowStrategy.fail)
//          val userFlow: Flow[Message, Message, ActorRef[UserCommand]] = Flow.fromGraph(GraphDSL.create(source) {
//            implicit builder =>
//              (pushSource: SourceShape[UserCommand]) =>
//                import GraphDSL.Implicits._
//
//                implicit val timeout: akka.util.Timeout = 1.second
//
//                val flowFromWs: FlowShape[Message, UserCommand] = builder.add(
//                  Flow[Message].map {
//                    case TextMessage.Strict(text: String) =>
//                      IOUtils.deserialize[UserCommand](text).get
//                    case BinaryMessage.Strict(text) => UserChatMessage("")
//                  }.buffer(1024 * 1024, OverflowStrategy.fail)
//                )
//
//                val flowToUser: FlowShape[UserCommand, Strict] = builder.add(ActorFlow.ask(newUser)(makeMessage = (el: UserCommand, replyTo: ActorRef[Strict]) => {
//                  UserWsConvertMessage(el, replyTo)
//                }))
//
//                val connectedWs: Flow[ActorRef[UserCommand], UserNotifierMessage, NotUsed] = Flow[ActorRef[UserCommand]].map((actor: ActorRef[UserCommand]) => UserNotifierMessage(actor, userID))
//
//                val pushActorSink = ActorSink.actorRef[UserCommand](ref = newUser, onCompleteMessage = UserPushCompleteMessage, onFailureMessage = onUserPushFail)
//
//                val mergeToUser: UniformFanInShape[UserCommand, UserCommand] = builder.add(Merge[UserCommand](2))
//
//                val printFlow: Flow[UserCommand, UserCommand, NotUsed] = Flow[UserCommand].map(c => {
//                  println("new command ", c)
//                  c
//                }
//                )
//                val printWSFlow: Flow[Strict, Strict, NotUsed] = Flow[Strict].map(c => {
//                  println("new ws ", c)
//                  c
//                }
//                )
//                val flowBackWS: FlowShape[Strict, Strict] = builder.add(
//                  Flow[Strict].collect {
//                    case Strict(text) =>
//                      println("important ", text)
//                      TextMessage(text)
//                  }
//                )
//
////                val convertToWS: FlowShape[UserCommand, Strict] = builder.add(Flow[UserCommand].map(
////                  c => TextMessage.Strict(IOUtils.serialize(c).get)
////                ))
////                val mergeToUserChanged: UniformFanInShape[Strict, Strict] = builder.add(Merge[Strict](2))
//
//                //                flowFromWs ~> flowToUser ~> flowBackWS
//                //                pushSource ~> convertToWS
//                //                flowToUser ~> mergeToUserChanged.in(0)
//                //                convertToWS ~> mergeToUserChanged.in(1)
//                //                mergeToUserChanged ~> flowBackWS
//
////                flowFromWs ~> flowToUser
////                pushSource ~> printFlow ~> convertToWS
////                flowToUser ~> mergeToUserChanged.in(0)
////                convertToWS ~> mergeToUserChanged.in(1)
////                mergeToUserChanged ~> flowBackWS
//
//                flowFromWs ~> mergeToUser.in(0)
//                pushSource ~> printFlow ~> mergeToUser.in(1)
//                mergeToUser ~> flowToUser
//                flowToUser ~> flowBackWS
//                // 添加推送用actor
//                builder.materializedValue ~> connectedWs ~> pushActorSink
//                FlowShape(flowFromWs.in, flowBackWS.out)
//          })
          userMap.update(userID, newUser)
          userFlowMap.update(userID, userFlow)
          sender ! StatusReply.success(UserFlowResponseMessage(userFlow))
        }
        Behaviors.same
      case UserSystemInitializeMessage(projectID, userID, sender: ActorRef[StatusReply[UserInitializeResponseMessage]]) =>
        userMap(userID) ! UserWsInitializeMessage(projectID, userID, sender)
        println("user system get initialize message!")
        sender ! StatusReply.success(UserInitializeResponseMessage(true))
        Behaviors.same
    }

  override def onSignal: PartialFunction[Signal, Behavior[UserSystemCommand]] = {
    case ChildFailed(ref, cause) =>
      println("terminated", ref.path.name)
      userMap.remove(ref.path.name)
      Behaviors.same
  }
}
