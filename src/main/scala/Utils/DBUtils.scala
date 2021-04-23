package Utils

import Plugins.MSUtils.ServiceUtils.exec
import Tables._
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.swing.Dialog
import scala.swing.Dialog.{Options, Result}


object DBUtils {
  def initDatabase():Unit={
    exec(
      DBIO.seq(
        sql"""CREATE SCHEMA IF NOT EXISTS display_user""".as[Long],
        UserMessageTable.userMessageTable.schema.createIfNotExists,
        SuperUserTable.superUserTable.schema.createIfNotExists,
      )
    )
  }
  def initKuibuDatabase():Unit = {
    exec(
      DBIO.seq(
        sql"""CREATE SCHEMA IF NOT EXISTS kuibu""".as[Long],
        UserAccountTable.userAccountTable.schema.createIfNotExists,
        UserChatContentTable.userChatContentTable.schema.createIfNotExists,
        UserUnreadMessageTable.userUnreadMessageTable.schema.createIfNotExists,
        ChatSessionInfoTable.chatSessionInfoTable.schema.createIfNotExists,
        ProjectInfoTable.projectInfoTable.schema.createIfNotExists,
        TaskInfoTable.taskInfoTable.schema.createIfNotExists,
      )
    )
  }
  def dropDatabases():Unit={
    val rlt = Dialog.showConfirmation(null, "真的要删除所有的数据库么？", "", optionType = Options.YesNo)
    if (rlt == Result.Yes || rlt == Result.Ok) {
      exec(
        DBIO.seq(
          UserMessageTable.userMessageTable.schema.dropIfExists,
          SuperUserTable.superUserTable.schema.dropIfExists,
          UserChatContentTable.userChatContentTable.schema.dropIfExists,
          sql"""DROP SCHEMA IF EXISTS display_user""".as[Long],
        )
      )
    }
  }

  def dropKuibuDatabase(): Unit={
    exec(
      DBIO.seq(
        UserAccountTable.userAccountTable.schema.dropIfExists,
        UserChatContentTable.userChatContentTable.schema.dropIfExists,
        UserUnreadMessageTable.userUnreadMessageTable.schema.dropIfExists,
        ChatSessionInfoTable.chatSessionInfoTable.schema.dropIfExists,
        ProjectInfoTable.projectInfoTable.schema.dropIfExists,
        TaskInfoTable.taskInfoTable.schema.dropIfExists,
        sql"""DROP SCHEMA IF EXISTS kuibu""".as[Long],
      )
    )
  }
}
