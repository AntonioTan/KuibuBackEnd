package Globals

import Tables.{ChatMessageTable, ChatSessionInfoTable, ProjectInfoRow, ProjectInfoTable, TaskInfoTable, UserAccountTable}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.util.Try

object GlobalDBs {
  val display_schema: Option[String] = Some("display_user")
  val kuibu_schema: Option[String] = Some("kuibu")

  val dateTimeFormatter = DateTimeFormat.forPattern("yyyy/MM/dd")


  def addInitialData(): Try[Unit] = Try {
    UserAccountTable.addUserWithUserID("tttttt", "Tianyi", "Tianyi")
    UserAccountTable.addUserWithUserID("aaaaaa", "Phoebie", "Phoebie")
    UserAccountTable.addUserWithUserID("bbbbbb", "Chandler", "Chandler")
    UserAccountTable.addUserWithUserID("cccccc", "Joey", "Joey")
    UserAccountTable.addUserWithUserID("dddddd", "Monica", "Monica")
    UserAccountTable.addUserWithUserID("eeeeee", "Ross", "Ross")
    UserAccountTable.addUserWithUserID("ffffff", "Rachel", "Rachel")
    var rst = UserAccountTable.addUserFriend("tttttt", "aaaaaa")
    rst = UserAccountTable.addUserFriend("tttttt", "bbbbbb")
    rst = UserAccountTable.addUserFriend("tttttt", "cccccc")
    rst = UserAccountTable.addUserFriend("tttttt", "dddddd")
    rst = UserAccountTable.addUserFriend("tttttt", "eeeeee")
    rst = UserAccountTable.addUserFriend("tttttt", "ffffff")
    ProjectInfoTable.addProjectWithID("000000", "美的集团项目分析", "tttttt", "进行美的集团的财务分析",
      List("tttttt", "aaaaaa", "bbbbbb", "cccccc", "dddddd", "eeeeee", "ffffff"))
    ProjectInfoTable.addProjectWithID("111111", "篮球比赛训练", "tttttt", "进行信息学院篮球比赛训练",
      List("tttttt", "aaaaaa", "bbbbbb", "cccccc", "dddddd", "eeeeee", "ffffff"))
    ProjectInfoTable.addProjectWTithID("222222", "任务管理信息系统开发", "tttttt", "进行任务管理信息系统的开发",
      List("tttttt","aaaaaa", "bbbbbb", "cccccc", "dddddd", "eeeeee", "ffffff"))
    TaskInfoTable.addTaskWithID(taskID = "000000", taskName = "周训练", projectID = "111111",
      startDate = DateTime.parse("2021/04/02", dateTimeFormatter), endDate = DateTime.parse("2021/04/03", dateTimeFormatter), description = "完成周训练", leaderIDList=List[String]("tttttt"), childrenIDList = List[String]("111111", "222222", "333333"), userIDList = List[String]("tttttt", "aaaaaa", "bbbbbb", "cccccc", "dddddd"))
    TaskInfoTable.addTaskWithID(taskID = "111111", taskName = "下蹲", projectID = "111111",
      startDate = DateTime.parse("2021/04/02", dateTimeFormatter), endDate = DateTime.parse("2021/04/03", dateTimeFormatter), description = "主要是力量训练 让大家增强力量", parentID = "000000", childrenIDList = List.empty[String], userIDList = List[String]("tttttt", "aaaaaa"))
    TaskInfoTable.addTaskWithID(taskID = "222222", taskName = "投篮", projectID = "111111",
      startDate = DateTime.parse("2021/04/02", dateTimeFormatter), endDate = DateTime.parse("2021/04/03", dateTimeFormatter), description = "主要是中投训练让大家适应比赛", parentID = "000000", childrenIDList = List.empty[String], userIDList = List[String]("tttttt", "cccccc"))
    TaskInfoTable.addTaskWithID(taskID = "333333", taskName = "跑位", projectID = "111111",
      startDate = DateTime.parse("2021/04/02", dateTimeFormatter), endDate = DateTime.parse("2021/04/03", dateTimeFormatter), description = "主要是跑位训练 让大家更灵活", parentID = "000000", childrenIDList = List.empty[String], userIDList = List[String]("tttttt", "bbbbbb"))
    ChatSessionInfoTable.addChatSessionInfoWithID("111111", "000000", "Tianyi,Phoebie", List("tttttt", "aaaaaa"))
    ChatSessionInfoTable.addChatSessionInfoWithID("111111", "111111", "Tianyi,Chandler", List("tttttt", "bbbbbb"))
    ChatSessionInfoTable.addChatSessionInfoWithID("111111", "222222", "Tianyi,Chandler,Monica", List("tttttt", "bbbbbb", "dddddd"))
    ChatSessionInfoTable.addChatSessionInfoWithID("111111", "333333", "Tianyi,Phoebie,Chandler,Joey,Monica,Ross,Rachel", List("tttttt","aaaaaa","bbbbbb","cccccc","dddddd","eeeeee","ffffff"))
    ChatMessageTable.addChatMessageWithID("000000", "000000","tttttt","Hello")
    Thread.sleep(100)
    ChatMessageTable.addChatMessageWithID("000001", "000000","aaaaaa","Hello")
    Thread.sleep(100)
    ChatMessageTable.addChatMessageWithID("000002", "000000","aaaaaa","Hello")
    Thread.sleep(100)
    ChatMessageTable.addChatMessageWithID("000003", "000000","tttttt","Phoebie")
    Thread.sleep(100)
    ChatMessageTable.addChatMessageWithID("000004", "000000","aaaaaa","Tianyi")
    Thread.sleep(100)
    ChatMessageTable.addChatMessageWithID("000005", "000000","tttttt","I")
    Thread.sleep(100)
    ChatMessageTable.addChatMessageWithID("000006", "000000","tttttt","Love")
    Thread.sleep(100)
    ChatMessageTable.addChatMessageWithID("000007", "000000","aaaaaa","Tianyi")
    Thread.sleep(100)
    ChatMessageTable.addChatMessageWithID("000008", "000000","aaaaaa","I love you!")
    Thread.sleep(100)

  }

}
