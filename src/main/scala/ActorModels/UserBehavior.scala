package ActorModels

import ActorModels.UserBehavior._
import ActorModels.UserSystemBehavior.{UserInitializeResponseMessage, UserSystemCommand, userMap}
import Plugins.CommonUtils.CommonTypes.JacksonSerializable
import Plugins.CommonUtils.IOUtils
import Tables.{ChatMessage, ChatMessageTable, ChatSessionInfoTable, ChatWsMessage, ProjectCompleteInfo, ProjectInfoTable, TaskInfoTable, TaskWebProcessInfo, TaskWebToDoInfo, UserUnreadMessageTable}
import akka.actor.TypedActor.self
import akka.actor.typed.pubsub.Topic
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, ChildFailed, Signal, Terminated}
import akka.http.scaladsl.model.ws.TextMessage.Strict
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.pattern.StatusReply
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import org.joda.time.DateTime

import scala.collection.concurrent.TrieMap

// 该behavior主要用于处理websocket发来的同步性质的消息
object UserBehavior {

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
  @JsonSubTypes(
    Array(
      new JsonSubTypes.Type(value = classOf[UserChatMessage], name = "UserChatMessage"),
      new JsonSubTypes.Type(value = classOf[UserWsInviteProjectMessage], name = "UserWsInviteProjectMessage"),
      new JsonSubTypes.Type(value = classOf[UserWsInvitedProjectMessage], name = "UserWsInvitedProjectMessage"),
      new JsonSubTypes.Type(value = classOf[UserWsInfoMessage], name = "UserWsInfoMessage"),
      new JsonSubTypes.Type(value = classOf[UserTestMessage], name = "UserTestMessage"),
      new JsonSubTypes.Type(value = classOf[UserWsInitializeMessage], name = "UserWsInitializeMessage"),
      new JsonSubTypes.Type(value = classOf[UserWsChatMessage], name = "UserWsChatMessage"),
      new JsonSubTypes.Type(value = classOf[UserWsPushChatMessage], name = "UserWsPushChatMessage"),
      new JsonSubTypes.Type(value = classOf[UserWsSyncEditMessage], name = "UserWsSyncEditMessage"),
      new JsonSubTypes.Type(value = classOf[UserWsUpdateToDoJudgeMessage], name = "UserWsUpdateToDoJudgeMessage"),
      new JsonSubTypes.Type(value = classOf[UserWsToDoStatusChangeMessage], name = "UserWsToDoStatusChangeMessage"),
      new JsonSubTypes.Type(value = classOf[UserWsTaskProcessInfoUpdateMessage], name = "UserWsTaskProcessInfoUpdateMessage"),
      new JsonSubTypes.Type(value = classOf[UserWsAddToDoMessage], name = "UserWsAddToDoMessage"),

    ))
  trait UserCommand

  case class UserChatMessage(content: String) extends UserCommand with JacksonSerializable

  case class UserNotifierMessage(notifier: ActorRef[UserCommand], userID: String) extends UserCommand with JacksonSerializable

  case object UserPushCompleteMessage extends UserCommand with JacksonSerializable

  case class UserPushFailMessage(ex: Throwable) extends UserCommand with JacksonSerializable

  val onUserPushFail: Throwable => UserPushFailMessage = (ex: Throwable) => UserPushFailMessage(ex)

  case object UserWsCompleteMessage extends UserCommand

  case class UserWsFailMessage(ex: Throwable) extends UserCommand with JacksonSerializable

  case class UserWsPushMessage(content: String) extends UserCommand with JacksonSerializable

  case class UserWsConvertMessage(usrCmd: UserCommand, replyTo: ActorRef[Strict]) extends UserCommand with JacksonSerializable

  // 邀请加入项目的消息
  case class UserWsInviteProjectMessage(senderID: String, inviteUserID: String, projectID: String, projectName: String) extends UserCommand with JacksonSerializable

  // 被邀请加入项目的消息
  case class UserWsInvitedProjectMessage(senderID: String, inviteUserID: String, projectID: String, projectName: String) extends UserCommand with JacksonSerializable

  case class UserWsInfoMessage(info: String) extends UserCommand with JacksonSerializable

  // useful
  case class UserWsInitializeMessage(lastProjectID: String, projectID: String, userID: String, sender: ActorRef[StatusReply[UserInitializeResponseMessage]]) extends UserCommand with JacksonSerializable

  case class UserWsChatMessage(chatMessage: ChatWsMessage) extends UserCommand with JacksonSerializable

  case class UserWsPushChatMessage(chatMessage: ChatWsMessage) extends UserCommand with JacksonSerializable

  case class UserWsSyncEditMessage(taskID: String, editUserID: String, content: String) extends UserCommand with JacksonSerializable

  case class UserWsUpdateToDoJudgeMessage(taskID: String, taskToDoID: String, outcome: Boolean) extends UserCommand with JacksonSerializable

  case class UserWsToDoStatusChangeMessage(taskID: String, taskToDoID: String, finishUserID: String, status: String, endDate: String) extends UserCommand with JacksonSerializable

  case class UserWsTaskProcessInfoUpdateMessage(taskID: String, newTaskProcessInfo: TaskWebProcessInfo) extends UserCommand with JacksonSerializable

  case class UserWsAddToDoMessage(taskID: String, newToDo: TaskWebToDoInfo) extends UserCommand with JacksonSerializable

  case class Structure(a: List[String])

  case class UserTestMessage(ab: Structure, ac: DateTime) extends UserCommand with JacksonSerializable

  //  trait UserChatProtocol
  //  case class Init(ackTo: ActorRef[UserCommand]) extends UserChatProtocol
  //  case class Message(ackTo: ActorRef[UserCommand], msg: String) extends UserChatProtocol
  //  case object Complete extends UserChatProtocol
  //  case class Fail(ex: Throwable) extends UserChatProtocol

  val sessionTopicHead = "sessionID"
  val taskTopicHead = "taskID"
  var replyToMap: TrieMap[String, ActorRef[UserCommand]] = TrieMap.empty[String, ActorRef[UserCommand]]
  var topicMap: TrieMap[String, ActorRef[Topic.Command[UserCommand]]] = TrieMap.empty[String, ActorRef[Topic.Command[UserCommand]]]

  def apply(): Behavior[UserCommand] = {
    Behaviors.setup(context =>
      new UserBehavior(context)
    )


  }
}


class UserBehavior(context: ActorContext[UserCommand]) extends AbstractBehavior[UserCommand](context) {

  //  override def onSignal: PartialFunction[Signal, Behavior[UserCommand]] = {
  //    case ChildFailed(ref, cause) =>
  //      println("terminated", ref.path.name)
  //      topicMap.remove(ref.path.name)
  //      Behaviors.same
  //  }

  override def onMessage(msg: UserCommand): Behavior[UserCommand] = {
    msg match {
      case UserChatMessage(content: String) =>
        context.log.info("User {} received: {}", content)
        this
      case UserPushCompleteMessage =>
        context.log.info(s"${context.self.path} User pushed a new message")
        this
      case UserPushFailMessage(ex) =>
        context.log.warn(s"${context.self.path} User push failed!")
        this
      case UserWsFailMessage(ex) =>
        context.log.warn("User failed to deal a WS message")
        this
      case UserWsCompleteMessage =>
        context.log.info("User completed a new ws message")
        this
      // 更新消息通知者的消息
      case UserNotifierMessage(replyTo: ActorRef[UserCommand], userID: String) =>
        println("notified message", replyTo)
        replyToMap.update(userID, replyTo)
        this
      case UserWsChatMessage(chatMessage: ChatWsMessage) =>
        println("chatMessage", chatMessage)
        ChatMessageTable.addChatFromWs(chatMessage)
        topicMap(s"${sessionTopicHead}-${chatMessage.sessionID}") ! Topic.Publish(UserWsPushChatMessage(chatMessage))
        this
      case UserWsSyncEditMessage(taskID: String, editUserID: String, content: String) =>
        context.log.info(IOUtils.serialize(msg).get)
        topicMap(s"${taskTopicHead}-${taskID}") ! Topic.Publish(msg)
        this
      case UserWsPushChatMessage(chatMessage: ChatWsMessage) =>
        println("publish new message", chatMessage)
        this
      case UserWsUpdateToDoJudgeMessage(taskID: String, taskToDoID: String, outcome: Boolean) =>
        topicMap(s"${taskTopicHead}-${taskID}") ! Topic.Publish(msg)
        this
      case UserWsToDoStatusChangeMessage(taskID: String, taskToDoID: String, endDate: String, finishUserID: String, status: String) =>
        topicMap(s"${taskTopicHead}-${taskID}") ! Topic.Publish(msg)
        this
      case UserWsTaskProcessInfoUpdateMessage(taskID: String, newTaskProcessInfo: TaskWebProcessInfo) =>
        topicMap(s"${taskTopicHead}-${taskID}") ! Topic.Publish(msg)
        this
      case UserWsAddToDoMessage(taskID: String, newToDo: TaskWebToDoInfo) =>
        topicMap(s"${taskTopicHead}-${taskID}") ! Topic.Publish(msg)
        this
      case UserWsInitializeMessage(lastProjectID, projectID, userID, sender) =>
        println("initialize", msg)
        // Unsubscribe to the old project
        if (ProjectInfoTable.IDExists(lastProjectID).get) {
          val oldSessionIDList: List[String] = ProjectInfoTable.getUserIncludedSessionIDList(lastProjectID, userID).get
          val oldTaskIDList: List[String] = TaskInfoTable.getMyTaskIDList(projectID = lastProjectID, userID = userID).get
          for (oldSessionID <- oldSessionIDList) {
            if (replyToMap.contains(userID)) topicMap(s"${sessionTopicHead}-${oldSessionID}") ! Topic.unsubscribe(replyToMap(userID))
          }
          for (oldTaskID <- oldTaskIDList) {
            if (replyToMap.contains(userID)) topicMap(s"${taskTopicHead}-${oldTaskID}") ! Topic.unsubscribe(replyToMap(userID))
          }
        }

        // Subscribe to new Project
        val newSessionIDList: List[String] = ProjectInfoTable.getUserIncludedSessionIDList(projectID, userID).get
        val newTaskIDList: List[String] = TaskInfoTable.getMyTaskIDList(projectID, userID).get
        for (newSessionID <- newSessionIDList) {
          val sessionTopicID: String = s"${sessionTopicHead}-${newSessionID}"
          if (!topicMap.contains(newSessionID)) {
            val newSessionTopic: ActorRef[Topic.Command[UserCommand]] = context.spawnAnonymous(Topic[UserCommand](sessionTopicID))
            topicMap.update(sessionTopicID, newSessionTopic)
            if (replyToMap.contains(userID)) newSessionTopic ! Topic.subscribe(replyToMap(userID))
          }
        }
        for (newTaskID <- newTaskIDList) {
          val taskTopicID: String = s"${taskTopicHead}-${newTaskID}"
          if (!topicMap.contains(taskTopicID)) {
            val newTaskTopic: ActorRef[Topic.Command[UserCommand]] = context.spawnAnonymous(Topic[UserCommand](taskTopicID))
            topicMap.update(taskTopicID, newTaskTopic)
            if (replyToMap.contains(userID)) newTaskTopic ! Topic.subscribe(replyToMap(userID))

          }
        }
        sender ! StatusReply.success(UserInitializeResponseMessage(true))
        this
      case UserWsConvertMessage(msg, replyTo) =>
        msg match {
          case UserChatMessage(content: String) =>
            replyTo ! TextMessage.Strict(content)
            this
          case UserWsInviteProjectMessage(senderID: String, inviteUserID: String, projectID: String, projectName: String) =>
            // TODO 这里需要加上一个未读消息列表
            if (replyToMap.contains(inviteUserID)) {
              replyToMap(inviteUserID) ! UserWsInvitedProjectMessage(senderID = senderID, inviteUserID = inviteUserID, projectID = projectID, projectName = projectName)
            }
            UserUnreadMessageTable.addMessage(receiverID = inviteUserID, senderID = senderID, message = IOUtils.serialize(msg).get)
            replyTo ! TextMessage.Strict(IOUtils.serialize(UserWsInfoMessage("已经发送项目加入请求!")).get)
            this
          case UserWsInvitedProjectMessage(senderID, inviteUserID, projectID, projectName) =>
            replyTo ! TextMessage.Strict(IOUtils.serialize(msg).get)
            this
          case UserWsChatMessage(chatMessage: ChatWsMessage) =>
            println("chatMessage", chatMessage)
            ChatMessageTable.addChatFromWs(chatMessage)
            //            println(replyToMap)
            topicMap(s"${sessionTopicHead}-${chatMessage.sessionID}") ! Topic.Publish(message = UserWsPushChatMessage(chatMessage))
            replyToMap("tttttt") ! UserWsPushChatMessage(chatMessage)
            replyTo ! TextMessage.Strict(IOUtils.serialize(UserWsInfoMessage("New Chat Message Saved")).get)
            this
          case UserWsPushChatMessage(chatMessage: ChatWsMessage) =>
            println("here get new push chat message!", IOUtils.serialize(msg).get)
            replyTo ! TextMessage.Strict(IOUtils.serialize(msg).get)
            this
          case _ =>
            println("other msg", msg)
            replyTo ! TextMessage.Strict(IOUtils.serialize(UserWsInfoMessage("")).get)
            this
        }

    }


  }


}
