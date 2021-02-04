package Globals

import Plugins.CommonUtils.CommonTypes.UserPath


/** 默认路径 */
case class DefaultPath() extends UserPath {
  override def dbServerName(): String = "localhost"
  override def akkaServerHostName(): String = "localhost"
  override def seedNodeName(): String = "\"akka://QianFangCluster@localhost:25251\", \"akka://QianFangCluster@localhost:25259\", \"akka://QianFangCluster@localhost:25260\""
  override def deploy(): Boolean = false
}
