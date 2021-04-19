package Globals

import Tables.{ChatSessionInfoTable, ProjectInfoRow, ProjectInfoTable, UserAccountTable}

import scala.util.Try

object GlobalDBs {
  val display_schema: Option[String] = Some("display_user")
  val kuibu_schema: Option[String] = Some("kuibu")

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
    ChatSessionInfoTable.addChatSessionInfoWithID("000000", "Tianyi,Phoebie", List("tttttt", "aaaaaa"))
    ChatSessionInfoTable.addChatSessionInfoWithID("111111", "Tianyi,Chandler", List("tttttt", "bbbbbb"))
    ChatSessionInfoTable.addChatSessionInfoWithID("222222", "Tianyi,Chandler,Monica", List("tttttt", "bbbbbb", "dddddd"))
    ChatSessionInfoTable.addChatSessionInfoWithID("333333", "Tianyi,Phoebie,Chandler,Joey,Monica,Ross,Rachel", List("tttttt","aaaaaa","bbbbbb","cccccc","dddddd","eeeeee","ffffff"))
    ProjectInfoTable.addProjectWithID("000000", "美的集团项目分析", "tttttt", "进行美的集团的财务分析",
      List("tttttt", "aaaaaa", "bbbbbb", "cccccc", "dddddd", "eeeeee", "ffffff"))
    ProjectInfoTable.addProjectWithID("111111", "篮球比赛训练", "tttttt", "进行信息学院篮球比赛训练",
      List("tttttt", "aaaaaa", "bbbbbb", "cccccc", "dddddd", "eeeeee", "ffffff"))
    ProjectInfoTable.addProjectWithID("222222", "任务管理信息系统开发", "tttttt", "进行任务管理信息系统的开发",
      List("tttttt","aaaaaa", "bbbbbb", "cccccc", "dddddd", "eeeeee", "ffffff"))
  }

}
