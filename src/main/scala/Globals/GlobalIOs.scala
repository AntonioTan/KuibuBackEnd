package Globals

import Plugins.CommonUtils.CommonTypes.UserPath.chosenPath

object GlobalIOs {
  /** 根据path决定选择哪个server */
  lazy val (serverIP, serverPort) = chosenPath.setServer()

}
