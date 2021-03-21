package Globals

import Plugins.CloudSourcingAPI.CloudSourcingAPIMain
import Plugins.MSUtils.AkkaBase.AkkaUtils
import Plugins.OCRAPI.OCRAPIMain
import Plugins.SendToTreeObject.SendToTreeObject
import Plugins.TreeObjectWatcherClient.TreeObjectWatcherClient
import Plugins.UserAccountAPI.UserAccountAPIMain

object GlobalThreads {
  /** 注册插件 */
  def addPlugins():Unit={
    AkkaUtils.akkaThreads=AkkaUtils.akkaThreads ++ List(UserAccountAPIMain.setter(_))
    AkkaUtils.akkaThreads=AkkaUtils.akkaThreads ++ List(OCRAPIMain.setter(_))
    AkkaUtils.akkaThreads=AkkaUtils.akkaThreads ++ List(CloudSourcingAPIMain.setter(_))
    AkkaUtils.akkaThreads=AkkaUtils.akkaThreads ++ List(SendToTreeObject.setter(_))
    AkkaUtils.akkaThreads=AkkaUtils.akkaThreads ++ List(TreeObjectWatcherClient.setter(_))
  }
  /** 注册主线程 */
  def addThreads():Unit={
  }

  /** 注册主线程、插件 */
  def init():Unit={
    addThreads()
    addPlugins()
  }
}
