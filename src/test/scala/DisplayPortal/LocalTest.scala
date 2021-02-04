package DisplayPortal
import Plugins.CommonUtils.CommonTypes.UserPath
import Plugins.CommonUtils.Hub.ServiceCenter.{engineServiceCode, portMap, userAccountServiceCode}
import Plugins.MSUtils.AkkaBase.AkkaUtils
import Plugins.MSUtils.{MailSender, ServiceUtils}
import Process.{DisplayHttpServer, DisplayRoutes, TreeObjectSyncClient}


case class LocalTestPath() extends UserPath {
  override def setHttpServerIP(): String = "localhost"
  override def dbServerName(): String = "localhost"

  override def akkaServerHostName(): String = "localhost"
  override def seedNodeName(): String =  "\"akka://QianFangCluster@localhost:"+portMap(engineServiceCode)+"\"," +
    " \"akka://QianFangCluster@localhost:"+portMap(userAccountServiceCode)+"\""
  override def deploy(): Boolean = false
}

object LocalTest {
  def main(args: Array[String]): Unit = try {
    println("=== Local Test version ===")
    println("version: " + ServiceUtils.getVersionInfo())
    UserPath.chosenPath=LocalTestPath()

    // pass source around for materialization
    println("setting up", AkkaUtils.clusterSystem)
    TreeObjectSyncClient.readLocalVersion()
    Thread.sleep(10000)
    DisplayHttpServer.startHttpServer(new DisplayRoutes()(AkkaUtils.clusterSystem).displayRoutes, AkkaUtils.clusterSystem)

  } catch {
    case exception: Exception =>
      MailSender.emailException(exception)
  }
}
