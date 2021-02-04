package Globals

import Globals.GlobalStrings._
import Plugins.CloudSourcingShared.Questions.{Question, bookCutQuestionType}
import Plugins.EngineOperationAPI
import Plugins.EngineOperationAPI.{TreeObjectSyncClient, TreeObjectVersion}
import Plugins.EngineShared.StringObject.CandidateObjectInfo
import Plugins.EngineShared.{TreeObjectMajorVersionID, TreeObjectMinorVersionID}
import akka.actor
import akka.actor.typed.ActorSystem
import org.joda.time.DateTime

import scala.collection.mutable

object GlobalVariables {
  /** 是否可以处理申诉 */
  var superUser:Boolean=false
  /**  是否是Treeobject的管理员 */
  var treeObjectAdmin : Boolean = false
  /** treeObjectAdmin的正在审核的treeObjectVersion */
  var treeObjectAdminVersion: TreeObjectVersion = EngineOperationAPI.TreeObjectVersion(TreeObjectMajorVersionID(-1), TreeObjectMinorVersionID(-1))

  /** 本地的stringObjectMap，用于存放用户的使用习惯 */
  val stringObjectMap:mutable.Map[String, List[CandidateObjectInfo]]=mutable.Map[String, List[CandidateObjectInfo]]()

  /** 用于同步的akka 环境 */
  var akkaSyncClient : ActorSystem[TreeObjectSyncClient.SyncCommand] = _
  /** 定义一个akka 环境，这个不是typed akka，和上面的不一样，用于当做implicit value */
  lazy val requestAkka: actor.ActorSystem = akka.actor.ActorSystem("SingleRequest")

  /*****************************************  显示相关  ***************************************/

  /** 当前应该发的钱数 */
  var currentPocketCredit: Double= 0
  /** 这个表示是否把object的其他名称也显示出来？*/
  var oneNotePanelOtherNameDisplayMode:Boolean=true
  /** 是否已经成功登陆 */
  var loginSuccess : Boolean = false
  /** 用于显示当前的消息传输状态 */
  var handlingMessageInfo : String = ""
  var handlingMessage : Boolean = false
  /** 上次更新时间，给用户显示确认  */
  var lastUpdateTime:DateTime=DateTime.now()

  /*****************************************  Question Pool相关  ***************************************/
  /** 当前选中的问题 */
  var selectedQuestionTypeName:String=bookCutQuestionType
  /** 问题库集合 */
  var questionPools: Map[String, Set[Question]] = questionTypeNames.map(q=> q->Set[Question]()).toMap
  /** 返回当前选中的问题库 */
  def selectedQuestionPool:Set[Question]=questionPools(selectedQuestionTypeName)
  /** 问题pool是否已经空了？ */
  var runOutQuestions: Map[String, Boolean]= questionTypeNames.map(q=> q-> false).toMap
  /** 判断当前选中的问题库是不是空了 */
  def selectedRunOutQuestion:Boolean=runOutQuestions(selectedQuestionTypeName)

}
