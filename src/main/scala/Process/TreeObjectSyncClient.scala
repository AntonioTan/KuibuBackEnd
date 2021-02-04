package Process

import java.io._

import Globals.GlobalStrings
import Plugins.CommonUtils.CommonTypes.UserPath
import Plugins.CommonUtils.IOUtils
import Plugins.EngineOperationAPI.AkkaEngineOperationMessages.TreeObjectQueryMessage
import Plugins.EngineOperationAPI.EngineObjectGlobals
import Plugins.EngineOperationAPI.TreeObjectUpdate.{SnapshotAnswerMessage, TreeObjectAnswerMessage}
import Plugins.EngineShared.BrotherHeadRow
import Plugins.EngineShared.ObjectClients.TreeObjectClient
import Utils.LocalUtils
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.duration.DurationInt
import scala.swing.Dialog

/** 这个akka 主要用于同步Tree Object。
 *  这个流程主要是这样的：服务器有了更新之后，会给client发送一个消息。client接收到消息之后，就会调用queryAndUpdate，发送一个
 *  更新的请求。发送了更新的请求之后，服务器就会根据当前的版本号，返回一些必要的更新operation信息。
 *  那么，为什么我们要用这样的方式呢？ 首先，服务器必须是有最新的版本，所以说，更新请求必须是服务器发送的。
 *  其次，服务器不能够直接把更新的内容推送过来，是因为网络可能会出现一些延迟和问题。有可能更新的内容是无法直接apply到当前的版本的。
 *  所以，最好的方式就是服务器告诉客户端，有一些更新了，你们快来根据自己的情况更新一下，这样是最不容易出错的了。*/
object TreeObjectSyncClient {
  sealed trait SyncCommand
  final case class UpdateVersion() extends SyncCommand
  final case class PingTest() extends SyncCommand
  final case class TimeOut() extends SyncCommand

  /** 不断地处于waitForMention的状态*/
  def waitForMention() : Behavior[SyncCommand] = Behaviors.withTimers {
    timers => {
      timers.startSingleTimer(TimeOut, TimeOut(), 20.seconds)
      Behaviors.receiveMessage {
        case UpdateVersion() =>
          queryAndUpdate()
          waitForMention()
        /** 如果收到了pingtest，那么就会自动地刷新自己的状态，之前的timer自然也就没有了。 */
        case PingTest() =>
          waitForMention()
        /** 如果在20秒之内还没有收到消息，那么就说明20秒内还没有从服务器收到ping，这个时候就知道网络断掉了。 */
        case TimeOut() =>
          Dialog.showMessage(null, "网络不好，与服务器连接超时，请重新登录")
          Behaviors.stopped
      }
    }
  }

  /** 初始化之后就开始处于WaitForMention的状态 */
  def apply() : Behavior[SyncCommand] = Behaviors.setup { ctx =>
    if (UserPath.chosenPath.deploy())
      readLocalVersion()
    queryAndUpdate()
    waitForMention()
  }

  def getFileName(checked:Boolean):String= {
    GlobalStrings.treeObjectFolder + (if (checked) "checked.obj" else "unchecked.obj")
  }

  /** 看看folder是否存在 */
  def checkTreeObjectFolder():Unit={
    val path = new File(GlobalStrings.treeObjectFolder)
    if (!path.exists()) path.mkdir()
  }

  /** 把本地读的内容变成treeObject */
  def readLocalVersion() : Unit = {
    checkTreeObjectFolder()
    val fileName = getFileName(checked = true)
    val file = new File(fileName)
    if (file.exists){
      val fileIn=new ObjectInputStream(new FileInputStream(fileName))
      IOUtils.deserialize[SnapshotAnswerMessage](fileIn.readObject().asInstanceOf[String]).get.applyToTree()
    }
  }

  /** 把目前的TreeObject保存到本地。先删除原有的文档，新建一个后缀为unchecked的文件，直接存snapshot,然后改名字为checked，防止写入时崩溃
   * */
  def commitToLocal() : Unit = {
    checkTreeObjectFolder()
    LocalUtils.deleteFile(getFileName(checked = true))
    val clients: List[TreeObjectClient] = EngineObjectGlobals.IDMap.values.map(_.toClient).toList
    val brothers: List[BrotherHeadRow] = EngineObjectGlobals.brotherMap.values.map(_.toRow).toList
    val message = SnapshotAnswerMessage(clients, brothers, EngineObjectGlobals.treeObjectVersion)
    try {
      val oos = new ObjectOutputStream(new FileOutputStream(getFileName(checked = false)))
      oos.writeObject(IOUtils.serialize(message).get)
      oos.close()
      val oldFile = new File(getFileName(checked = false))
      val newFile = new File(getFileName(checked = true))
      oldFile.renameTo(newFile)
    } catch {
      case e : Exception =>
        LocalUtils.handleException(e)
    }
  }
  /** 请求更新内容，然后更新到本地 */
  def queryAndUpdate() : Unit= {
    LocalUtils.send(TreeObjectQueryMessage(EngineObjectGlobals.treeObjectVersion)).foreach{
      value=>
        try {
          if (IOUtils.deserialize[TreeObjectAnswerMessage](value).get.applyToTree()) {
            if (UserPath.chosenPath.deploy())
              commitToLocal()
          }
        } catch {
          case e : Exception =>
            LocalUtils.handleException(e)
        }
    }
  }
}