package ActorModels

import ActorModels.UserBehavior.UserChatMessage
import ActorModels.UserSystemBehavior.{UserInitializeResponseMessage, UserSystemInitializeMessage}
import ActorModels.UserWebRequestBehavior.{UserWebAddProjectMessage, UserWebAddTaskMessage, UserWebAddToDoMessage, UserWebBasicProjectInfoMessage, UserWebBasicUserInfoMessage, UserWebCommand, UserWebGanttDateChangeMessage, UserWebGetCompleteProjectInfoMessage, UserWebGetCompleteTaskInfoMessage, UserWebGetGanttDataMessage, UserWebGetMemberMapMessage, UserWebGetMyTaskListMessage, UserWebGetSessionInfoMessage, UserWebGetSyncTaskInfoMessage, UserWebGetTasksInfoMessage, UserWebGetUserNameMessage, UserWebLoginCommand, UserWebLoginMessage, UserWebMessage, UserWebProcessInfoUpdateMessage, UserWebRegisterMessage, UserWebToDoJudgeMessage, UserWebToDoStatusChangeMessage, UserWebWSInitializeMessage, WebReplyAddProjectMessage, WebReplyAddTaskMessage, WebReplyAddToDoMessage, WebReplyBasicProjectInfoMessage, WebReplyBasicUserInfoMessage, WebReplyGanttDateChangeMessage, WebReplyGetCompleteProjectInfoMessage, WebReplyGetCompleteTaskInfoMessage, WebReplyGetGanttDataMessage, WebReplyGetMyTaskListMessage, WebReplyGetSessionInfoMessage, WebReplyGetSyncTaskInfoMessage, WebReplyGetTasksInfoMessage, WebReplyGetUserNameMessage, WebReplyLoginMessage, WebReplyMemberMapMessage, WebReplyMessage, WebReplyRegisterMessage, WebReplyTaskProcessInfoUpdateMessage, WebReplyToDoJudgeMessage, WebReplyToDoStatusChangeMessage, WebReplyWSInitializeMessage}
import Globals.GlobalVariables.userSystem
import Plugins.CommonUtils.CommonTypes.JacksonSerializable
import Plugins.MSUtils.AkkaBase.AkkaUtils.system
import Tables.{ChatSessionInfo, ChatSessionInfoTable, GanttData, GanttTask, MyTask, ProjectBasicInfo, ProjectCompleteInfo, ProjectInfoTable, SyncTask, TaskAddResult, TaskCompleteInfo, TaskInfoTable, TaskNewFromWeb, TaskProcessInfoTable, TaskToDoInfoTable, TaskWebProcessInfo, TaskWebToDoInfo, UserAccountTable, UserBasicInfo}
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
      new JsonSubTypes.Type(value = classOf[UserWebGetMyTaskListMessage], name = "UserWebGetMyTaskListMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebGetSyncTaskInfoMessage], name = "UserWebGetSyncTaskInfoMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebToDoJudgeMessage], name = "UserWebToDoJudgeMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebToDoStatusChangeMessage], name = "UserWebToDoStatusChangeMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebProcessInfoUpdateMessage], name = "UserWebProcessInfoUpdateMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebAddToDoMessage], name = "UserWebAddToDoMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebGetGanttDataMessage], name = "UserWebGetGanttDataMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebGanttDateChangeMessage], name = "UserWebGanttDateChangeMessage"),
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
  case class UserWebWSInitializeMessage(lastProjectID: String, projectID: String, userID: String) extends UserWebCommand with JacksonSerializable
  case class UserWebGetUserNameMessage(userID: String) extends UserWebCommand with JacksonSerializable
  case class UserWebGetMyTaskListMessage(projectID: String, userID: String) extends UserWebCommand with JacksonSerializable
  case class UserWebGetSyncTaskInfoMessage(taskID: String) extends UserWebCommand with JacksonSerializable
  case class UserWebToDoJudgeMessage(taskToDoID: String, outcome: Boolean) extends UserWebCommand with JacksonSerializable
  case class UserWebToDoStatusChangeMessage(taskToDoID: String, status: String, finishUserID: String) extends UserWebCommand with JacksonSerializable
  case class UserWebProcessInfoUpdateMessage(taskID: String, editUserID: String, content: String) extends UserWebCommand with JacksonSerializable
  case class UserWebAddToDoMessage(taskID: String, newToDo: TaskWebToDoInfo) extends UserWebCommand with JacksonSerializable
  case class UserWebGetGanttDataMessage(projectID: String) extends UserWebCommand with JacksonSerializable
  case class UserWebGanttDateChangeMessage(projectID: String, taskList: List[GanttTask]) extends UserWebCommand with JacksonSerializable

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
      new JsonSubTypes.Type(value = classOf[WebReplyGetMyTaskListMessage], name = "WebReplyGetMyTaskListMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyGetSyncTaskInfoMessage], name = "WebReplyGetSyncTaskInfoMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyToDoJudgeMessage], name = "WebReplyToDoJudgeMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyTaskProcessInfoUpdateMessage], name = "WebReplyTaskProcessInfoUpdateMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyAddToDoMessage], name = "WebReplyAddToDoMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyGetGanttDataMessage], name = "WebReplyGetGanttDataMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyGanttDateChangeMessage], name = "WebReplyGanttDateChangeMessage"),
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
  case class WebReplyGetMyTaskListMessage(myTaskList: List[MyTask]) extends WebReplyMessage with JacksonSerializable
  case class WebReplyGetSyncTaskInfoMessage(syncTaskInfo: SyncTask) extends WebReplyMessage with JacksonSerializable
  case class WebReplyToDoJudgeMessage(outcome: Boolean) extends WebReplyMessage with JacksonSerializable
  case class WebReplyToDoStatusChangeMessage(outcome: Boolean, endDate: String) extends WebReplyMessage with JacksonSerializable
  case class WebReplyTaskProcessInfoUpdateMessage(outcome: Boolean, newTaskProcessInfo: TaskWebProcessInfo) extends  WebReplyMessage with JacksonSerializable
  case class WebReplyAddToDoMessage(outcome: Boolean, newToDo: TaskWebToDoInfo) extends WebReplyMessage with JacksonSerializable
  case class WebReplyGetGanttDataMessage(ganttData: GanttData) extends WebReplyMessage with JacksonSerializable
  case class WebReplyGanttDateChangeMessage(outcome: Boolean, projectID: String) extends WebReplyMessage with JacksonSerializable

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
              ref ! StatusReply.success(WebReplyRegisterMessage(newID, "??????????????????!", outcome = true))
            } else {
              ref ! StatusReply.error("????????????????????????")
            }
            Behaviors.stopped
          case UserWebLoginMessage(userID, pwd) =>
            val loginState: Boolean = UserAccountTable.checkLogin(userID, pwd).get
            if(loginState) {
              ref ! StatusReply.success(WebReplyLoginMessage(reason = "???????????????", outcome = true))
            } else {
              ref ! StatusReply.success(WebReplyLoginMessage(reason = "???????????????", outcome = false))
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
          case UserWebWSInitializeMessage(lastProjectID, projectID, userID) =>
            import akka.actor.typed.scaladsl.AskPattern._
            implicit val pathUserWebSystem: ActorSystem[SystemBehavior.SystemCommand] = system
            implicit val ec: ExecutionContext = system.executionContext
            implicit val timeout: Timeout = Timeout(3.seconds)
            val initializeResponse: Future[UserSystemBehavior.UserInitializeResponseMessage] =  userSystem.askWithStatus(ref => UserSystemInitializeMessage(lastProjectID, projectID, userID, ref))
            initializeResponse.onComplete((userWSInitiazlieResponseMessage: Try[UserInitializeResponseMessage]) => {
              ref ! StatusReply.success(WebReplyWSInitializeMessage(outcome = userWSInitiazlieResponseMessage.get.outcome))
            })
            Behaviors.stopped
          case UserWebGetUserNameMessage(userID) =>
            ref ! StatusReply.success(WebReplyGetUserNameMessage(UserAccountTable.getNameByID(userID).get))
            Behaviors.stopped
          case UserWebGetMyTaskListMessage(projectID, userID) =>
            ref ! StatusReply.success(WebReplyGetMyTaskListMessage(TaskInfoTable.getMyTaskList(projectID, userID).get))
            Behaviors.stopped
          case UserWebGetSyncTaskInfoMessage(taskID) =>
            ref ! StatusReply.success(WebReplyGetSyncTaskInfoMessage(TaskInfoTable.getSyncTaskInfo(taskID).get))
            Behaviors.stopped
          case UserWebToDoJudgeMessage(taskToDoID, outcome) =>
            TaskToDoInfoTable.updateTaskToDoJudgeStatus(taskToDoID, outcome)
            ref ! StatusReply.success(WebReplyToDoJudgeMessage(outcome = true))
            Behaviors.stopped
          case UserWebToDoStatusChangeMessage(taskToDoID, status, finishUserID) =>
            ref ! StatusReply.success(WebReplyToDoStatusChangeMessage(outcome = true, endDate = TaskToDoInfoTable.updateTaskToDoStatus(taskToDoID = taskToDoID, status = status, finishUserID = finishUserID).get))
            Behaviors.stopped
          case UserWebProcessInfoUpdateMessage(taskID, editUserID, content) =>
            ref ! StatusReply.success(WebReplyTaskProcessInfoUpdateMessage(outcome = true, TaskProcessInfoTable.updateProcessInfo(taskID, editUserID, content).get))
            Behaviors.stopped
          case UserWebAddToDoMessage(taskID, newToDo) =>
            ref ! StatusReply.success(WebReplyAddToDoMessage(outcome=true, TaskToDoInfoTable.addTaskToDoFromWeb(taskID, newToDo).get))
            Behaviors.stopped
          case UserWebGetGanttDataMessage(projectID) =>
            ref ! StatusReply.success(WebReplyGetGanttDataMessage(ProjectInfoTable.getGanttData(projectID).get))
            Behaviors.stopped
          case UserWebGanttDateChangeMessage(projectID, taskList) =>
            TaskInfoTable.updateGanttDateFromWeb(taskList)
            ref ! StatusReply.success(WebReplyGanttDateChangeMessage(outcome = true, projectID = projectID))
            Behaviors.stopped
        }
    }
  }
}
