package Globals

import Plugins.CloudSourcingAPI.CloudSourcingAPIMain
import Plugins.EngineWatcherAPI.EngineWatcher
import Plugins.MSUtils.AkkaBase.AkkaUtils
import Plugins.OCRAPI.OCRAPIMain
import Plugins.UserAccountAPI.UserAccountAPIMain
import Process.DisplayToEngine

object GlobalThreads {
  /** 注册插件 */
  def addPlugins():Unit={
    AkkaUtils.akkaThreads=AkkaUtils.akkaThreads ++ List(UserAccountAPIMain.setter(_))
    AkkaUtils.akkaThreads=AkkaUtils.akkaThreads ++ List(OCRAPIMain.setter(_))
    AkkaUtils.akkaThreads=AkkaUtils.akkaThreads ++ List(CloudSourcingAPIMain.setter(_))
    AkkaUtils.akkaThreads=AkkaUtils.akkaThreads ++ List(DisplayToEngine.setter(_))
    AkkaUtils.akkaThreads=AkkaUtils.akkaThreads ++ List(EngineWatcher.setter(_))
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
