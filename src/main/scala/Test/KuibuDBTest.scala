package Test

import Plugins.CommonUtils.CommonTypes.UserPath
import Plugins.CommonUtils.Hub.ServiceCenter.{portMap, treeObjectServiceCode, userAccountServiceCode}
import Tables.UserAccountTable
import Utils.DBUtils


case class LocalTestPath() extends UserPath {
  override def setHttpServerIP(): String = "localhost"

  override def dbServerName(): String = "localhost"

  override def akkaServerHostName(): String = "localhost"

  override def seedNodeName(): String = "\"akka://QianFangCluster@localhost:" + portMap(treeObjectServiceCode) + "\"," +
    " \"akka://QianFangCluster@localhost:" + portMap(userAccountServiceCode) + "\""

  override def deploy(): Boolean = false

  override def setServer(): (String, Int) = {
    /** 外网稳定版server端口 */
    ("222.128.10.132", 2003)

    /** 内网测试版server端口 (30071 <=> 3071) */
    //    ("192.168.50.232", 30071)

    /** 本地版server端口 */
    //    ("localhost", 6070)
  }

}

object KuibuDBTest {
  def main(args: Array[String]): Unit = {

    UserPath.chosenPath=LocalTestPath()
    DBUtils.dropKuibuDatabase()
    DBUtils.initKuibuDatabase()
    val newID: String = UserAccountTable.generateNewID()
    UserAccountTable.addUser(newID, "test1", "test2")

  }

}
