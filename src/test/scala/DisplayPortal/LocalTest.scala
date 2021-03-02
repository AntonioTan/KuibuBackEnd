package DisplayPortal
import Globals.GlobalVariables
import Plugins.CommonUtils.CommonTypes.UserPath
import Plugins.CommonUtils.Hub.ServiceCenter.{engineServiceCode, portMap, userAccountServiceCode}
import Plugins.CommonUtils.IOUtils
import Plugins.EngineOperationAPI.AkkaEngineOperationMessages.TreeObjectQueryMessage
import Plugins.EngineOperationAPI.{EngineObjectGlobals, TreeObjectSyncClient}
import Plugins.EngineOperationAPI.TreeObjectSyncClient.commitToLocal
import Plugins.EngineOperationAPI.TreeObjectUpdate.TreeObjectAnswerMessage
import Plugins.MSUtils.AkkaBase.AkkaUtils
import Plugins.MSUtils.{API, MailSender, ServiceUtils}
import Process.{DisplayHttpServer, DisplayRoutes}
import Utils.{DBUtils, LocalUtils}
import akka.actor.typed.ActorSystem


case class LocalTestPath() extends UserPath {
  override def setHttpServerIP(): String = "localhost"

  override def dbServerName(): String = "localhost"

  override def akkaServerHostName(): String = "localhost"

  override def seedNodeName(): String = "\"akka://QianFangCluster@localhost:" + portMap(engineServiceCode) + "\"," +
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
object LocalTest {
  def main(args: Array[String]): Unit = try {
    println("=== Local Test version ===")
    println("version: " + ServiceUtils.getVersionInfo())

    UserPath.chosenPath=LocalTestPath()
    println("setting up", AkkaUtils.clusterSystem)

    DBUtils.initDatabase()

    Thread.sleep(1000)
    GlobalVariables.akkaSyncClient = ActorSystem(TreeObjectSyncClient(), "syncClient")
    DisplayHttpServer.startHttpServer(new DisplayRoutes()(AkkaUtils.clusterSystem).displayRoutes, AkkaUtils.clusterSystem)
  } catch {
    case exception: Exception =>
      MailSender.emailException(exception)
  }
}
