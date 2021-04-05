package Globals

import Plugins.MSUtils.AkkaBase.AkkaUtils
import Plugins.UserAccountAPI.UserAccountAPIMain

object GlobalThreads {
  /** 注册插件 */
  def addPlugins():Unit={
    AkkaUtils.akkaThreads=AkkaUtils.akkaThreads ++ List(UserAccountAPIMain.setter(_))
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
