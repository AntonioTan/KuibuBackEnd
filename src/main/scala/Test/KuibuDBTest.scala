package Test

import KuibuProcess.LocalTestPath
import Plugins.CommonUtils.CommonTypes.UserPath
import Plugins.CommonUtils.Hub.ServiceCenter.{portMap, treeObjectServiceCode, userAccountServiceCode}
import Tables.UserAccountTable
import Utils.DBUtils


object KuibuDBTest {
  def main(args: Array[String]): Unit = {

    UserPath.chosenPath=LocalTestPath()
    DBUtils.dropKuibuDatabase()
    DBUtils.initKuibuDatabase()
    val newID: String = UserAccountTable.generateNewID()
    UserAccountTable.addUserWithUserID(newID, "test1", "test2")

  }

}
