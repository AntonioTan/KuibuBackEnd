package ActorModels

import ActorModels.UserBehavior.UserChatMessage
import ActorModels.UserSystemBehavior.{UserInitializeResponseMessage, UserSystemInitializeMessage}
import ActorModels.UserWebRequestBehavior.{UserWebAddProjectMessage, UserWebAddTaskMessage, UserWebBasicProjectInfoMessage, UserWebBasicUserInfoMessage, UserWebCommand, UserWebGetCompleteProjectInfoMessage, UserWebGetCompleteTaskInfoMessage, UserWebGetMemberMapMessage, UserWebGetSessionInfoMessage, UserWebGetTasksInfoMessage, UserWebGetUserNameMessage, UserWebLoginCommand, UserWebLoginMessage, UserWebMessage, UserWebRegisterMessage, UserWebWSInitializeMessage, WebReplyAddProjectMessage, WebReplyAddTaskMessage, WebReplyBasicProjectInfoMessage, WebReplyBasicUserInfoMessage, WebReplyGetCompleteProjectInfoMessage, WebReplyGetCompleteTaskInfoMessage, WebReplyGetSessionInfoMessage, WebReplyGetTasksInfoMessage, WebReplyGetUserNameMessage, WebReplyLoginMessage, WebReplyMemberMapMessage, WebReplyMessage, WebReplyRegisterMessage, WebReplyWSInitializeMessage}
import Globals.GlobalVariables.userSystem
import Plugins.CommonUtils.CommonTypes.JacksonSerializable
import Plugins.MSUtils.AkkaBase.AkkaUtils.system
import Tables.{ChatSessionInfo, ChatSessionInfoTable, ProjectBasicInfo, ProjectCompleteInfo, ProjectInfoTable, TaskAddResult, TaskCompleteInfo, TaskInfoTable, TaskNewFromWeb, UserAccountTable, UserBasicInfo}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.http.scaladsl.server.Directives.onComplete
import akka.pattern.StatusReply
import akka.util.Timeout
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

object UserWebRequestBehavior {

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
  @JsonSubTypes(
    Array(
      new JsonSubTypes.Type(value = classOf[UserWebMessage], name = "UserWebMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebLoginCommand], name = "UserWebLoginCommand"),
      new JsonSubTypes.Type(value = classOf[UserWebRegisterMessage], name = "UserWebRegisterMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebLoginMessage], name = "UserWebLoginMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebBasicUserInfoMessage], name = "UserWebBasicUserInfoMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebBasicProjectInfoMessage], name = "UserWebBasicProjectInfoMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebGetMemberMapMessage], name = "UserWebGetMemberMapMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebAddProjectMessage], name = "UserWebAddProjectMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebGetCompleteProjectInfoMessage], name = "UserWebGetCompleteProjectInfoMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebGetCompleteTaskInfoMessage], name = "UserWebGetCompleteTaskInfoMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebAddTaskMessage], name = "UserWebAddTaskMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebGetTasksInfoMessage], name = "UserWebGetTasksInfoMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebGetSessionInfoMessage], name = "UserWebGetSessionInfoMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebWSInitializeMessage], name = "UserWebWSInitializeMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebGetUserNameMessage], name = "UserWebGetUserNameMessage"),
    ))
  sealed trait UserWebCommand
  case class UserWebMessage(message: UserWebCommand, sender: ActorRef[StatusReply[WebReplyMessage]]) extends UserWebCommand with JacksonSerializable
  case class UserWebLoginCommand(text: String) extends UserWebCommand with JacksonSerializable
  case class UserWebRegisterMessage(userName: String, passWord: String, rePassWord: String) extends UserWebCommand with JacksonSerializable
  case class UserWebLoginMessage(userID: String, passWord: String) extends UserWebCommand with JacksonSerializable
  case class UserWebBasicUserInfoMessage(userID: String) extends UserWebCommand with JacksonSerializable
  case class UserWebBasicProjectInfoMessage(projectID: String) extends UserWebCommand with JacksonSerializable
  case class UserWebGetMemberMapMessage(userIDs: List[String]) extends UserWebCommand with JacksonSerializable
  case class UserWebAddProjectMessage(projectName: String, createUserID: String, description: String, userIDList: List[String]) extends UserWebCommand with JacksonSerializable
  case class UserWebGetCompleteProjectInfoMessage(projectID: String) extends UserWebCommand with JacksonSerializable
  case class UserWebGetCompleteTaskInfoMessage(taskID: String) extends UserWebCommand with JacksonSerializable
  case class UserWebAddTaskMessage(newTask: TaskNewFromWeb) extends UserWebCommand with JacksonSerializable
  case class UserWebGetTasksInfoMessage(projectID: String) extends UserWebCommand with JacksonSerializable
  case class UserWebGetSessionInfoMessage(sessionID: String) extends UserWebCommand with JacksonSerializable
  case class UserWebWSInitializeMessage(projectID: String, userID: String) extends UserWebCommand with JacksonSerializable
  case class UserWebGetUserNameMessage(userID: String) extends UserWebCommand with JacksonSerializable

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
  @JsonSubTypes(
    Array(
      new JsonSubTypes.Type(value = classOf[WebReplyLoginMessage], name = "WebReplyLoginMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyRegisterMessage], name = "WebReplyRegisterMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyBasicUserInfoMessage], name = "WebReplyBasicUserInfoMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyMemberMapMessage], name = "WebReplyMemberMapMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyAddProjectMessage], name = "WebReplyAddProjectMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyGetCompleteProjectInfoMessage], name = "WebReplyGetCompleteProjectInfoMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyGetCompleteTaskInfoMessage], name = "WebReplyGetCompleteTaskInfoMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyAddTaskMessage], name = "WebReplyAddTaskMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyGetTasksInfoMessage], name = "WebReplyGetTasksInfoMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyGetSessionInfoMessage], name = "WebReplyGetSessionInfoMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyWSInitializeMessage], name = "WebReplyWSInitializeMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyGetUserNameMessage], name = "WebReplyGetUserNameMessage"),
    ))
  sealed trait WebReplyMessage
  case class WebReplyRegisterMessage(userID: String, reason: String, outcome: Boolean) extends WebReplyMessage with JacksonSerializable
  case class WebReplyLoginMessage(reason: String, outcome: Boolean) extends WebReplyMessage with JacksonSerializable
  case class WebReplyBasicUserInfoMessage(userBasicInfo: UserBasicInfo) extends  WebReplyMessage with JacksonSerializable
  case class WebReplyBasicProjectInfoMessage(projectBasicInfo: ProjectBasicInfo) extends WebReplyMessage with JacksonSerializable
  case class WebReplyMemberMapMessage(memberMap: Map[String, String]) extends WebReplyMessage with JacksonSerializable
  case class WebReplyAddProjectMessage(projectID: String) extends WebReplyMessage with JacksonSerializable
  case class WebReplyGetCompleteProjectInfoMessage(projectCompleteInfo: ProjectCompleteInfo) extends WebReplyMessage with JacksonSerializable
  case class WebReplyGetCompleteTaskInfoMessage(taskCompleteInfo: TaskCompleteInfo) extends WebReplyMessage with JacksonSerializable
  case class WebReplyAddTaskMessage(taskAddResult: TaskAddResult) extends WebReplyMessage with JacksonSerializable
  case class WebReplyGetTasksInfoMessage(taskMap: Map[String, String]) extends WebReplyMessage with JacksonSerializable
  case class WebReplyGetSessionInfoMessage(chatSessionInfo: ChatSessionInfo) extends WebReplyMessage with JacksonSerializable
  case class WebReplyWSInitializeMessage(outcome: Boolean) extends WebReplyMessage with JacksonSerializable
  case class WebReplyGetUserNameMessage(userName: String) extends  WebReplyMessage with JacksonSerializable

  def apply(): Behavior[UserWebCommand] = {
    Behaviors.setup(context =>
      new UserWebRequestBehavior(context)
    )
  }

}

class UserWebRequestBehavior(context: ActorContext[UserWebCommand]) extends AbstractBehavior[UserWebCommand](context) {
  override def onMessage(msg: UserWebCommand): Behavior[UserWebCommand] = {
    msg match {
      case UserWebLoginCommand(text: String) =>
        Behaviors.stopped
      case UserWebMessage(message: UserWebCommand, ref: ActorRef[StatusReply[WebReplyMessage]]) =>
        message match {
          case UserWebRegisterMessage(userName, pwd, rePwd) =>
            val newID = UserAccountTable.generateNewID()
            val rst = UserAccountTable.addUserWithUserID(newID, userName, pwd)
            if(rst.isSuccess) {
              ref ! StatusReply.success(WebReplyRegisterMessage(newID, "成功创建用户!", outcome = true))
            } else {
              ref ! StatusReply.error("未能成功创建用户")
            }
            Behaviors.stopped
          case UserWebLoginMessage(userID, pwd) =>
            val loginState: Boolean = UserAccountTable.checkLogin(userID, pwd).get
            if(loginState) {
              ref ! StatusReply.success(WebReplyLoginMessage(reason = "成功登录！", outcome = true))
            } else {
              ref ! StatusReply.success(WebReplyLoginMessage(reason = "未成功登录", outcome = false))
            }
            Behaviors.stopped
          case UserWebBasicUserInfoMessage(userID) =>
            ref ! StatusReply.success(WebReplyBasicUserInfoMessage(UserAccountTable.getBasicUserInfo(userID).get))
            Behaviors.stopped
          case UserWebBasicProjectInfoMessage(projectID) =>
            ref ! StatusReply.success(WebReplyBasicProjectInfoMessage(ProjectInfoTable.getBasicProjectInfo(projectID).get))
            Behaviors.stopped
          case UserWebGetMemberMapMessage(userIDs) =>
            ref ! StatusReply.success(WebReplyMemberMapMessage(UserAccountTable.getUserNamesByIDs(userIDs).get))
            Behaviors.stopped
          case UserWebAddProjectMessage(projectName, createUserID, description, userIDList) =>
            ref ! StatusReply.success(WebReplyAddProjectMessage(ProjectInfoTable.addProject(projectName, createUserID, description, userIDList).get))
            Behaviors.stopped
          case UserWebGetCompleteProjectInfoMessage(projectID) =>
            ref ! StatusReply.success(WebReplyGetCompleteProjectInfoMessage(ProjectInfoTable.getCompleteProjectInfo(projectID).get))
            Behaviors.stopped
          case UserWebGetCompleteTaskInfoMessage(taskID) =>
            ref ! StatusReply.success(WebReplyGetCompleteTaskInfoMessage(TaskInfoTable.getTaskCompleteInfo(taskID).get))
            Behaviors.stopped
          case UserWebAddTaskMessage(newTask) =>
            ref ! StatusReply.success(WebReplyAddTaskMessage(TaskInfoTable.addTaskFromWeb(newTask).get))
            Behaviors.stopped
          case UserWebGetTasksInfoMessage(projectID) =>
            ref ! StatusReply.success(WebReplyGetTasksInfoMessage(taskMap = ProjectInfoTable.getTasksInfo(projectID).get))
            Behaviors.stopped
          case UserWebGetSessionInfoMessage(sessionID) =>
            ref ! StatusReply.success(WebReplyGetSessionInfoMessage(chatSessionInfo = ChatSessionInfoTable.getSessionInfo(sessionID).get))
            Behaviors.stopped
          case UserWebWSInitializeMessage(projectID, userID) =>
            import akka.actor.typed.scaladsl.AskPattern._
            implicit val pathUserWebSystem: ActorSystem[SystemBehavior.SystemCommand] = system
            implicit val ec: ExecutionContext = system.executionContext
            implicit val timeout: Timeout = Timeout(3.seconds)
            val initializeResponse: Future[UserSystemBehavior.UserInitializeResponseMessage] =  userSystem.askWithStatus(ref => UserSystemInitializeMessage(projectID, userID, ref))
            initializeResponse.onComplete((userWSInitiazlieResponseMessage: Try[UserInitializeResponseMessage]) => {
              ref ! StatusReply.success(WebReplyWSInitializeMessage(outcome = userWSInitiazlieResponseMessage.get.outcome))
            })
            Behaviors.stopped
          case UserWebGetUserNameMessage(userID) =>
            ref ! StatusReply.success(WebReplyGetUserNameMessage(UserAccountTable.getNameByID(userID).get))
            Behaviors.stopped
        }
    }
  }
}
