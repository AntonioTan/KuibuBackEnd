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
  def dropDatabases():Unit={
    val rlt = Dialog.showConfirmation(null, "真的要删除所有的数据库么？", "", optionType = Options.YesNo)
    if (rlt == Result.Yes || rlt == Result.Ok) {
      exec(
        DBIO.seq(
          UserMessageTable.userMessageTable.schema.dropIfExists,
          SuperUserTable.superUserTable.schema.dropIfExists,
          sql"""DROP SCHEMA IF EXISTS display_user""".as[Long],
        )
      )
    }
  }
}
