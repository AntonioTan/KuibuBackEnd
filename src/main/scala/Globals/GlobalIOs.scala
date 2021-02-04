package Globals

import Plugins.CommonUtils.CommonTypes.UserPath.chosenPath

object GlobalIOs {
  /** 根据path决定选择哪个server */
  lazy val (serverIP, serverPort) = chosenPath.setServer()

  /** 登录成功之后可以得到一个token，用于以后登录 */
  var userToken : String = ""
}
