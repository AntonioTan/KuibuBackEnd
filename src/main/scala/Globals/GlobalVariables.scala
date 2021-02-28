package Globals

import Plugins.EngineOperationAPI.TreeObjectSyncClient
import akka.actor.typed.ActorSystem

object GlobalVariables {
  /** 用于同步的akka 环境 */
  var akkaSyncClient : ActorSystem[TreeObjectSyncClient.SyncCommand] = _

  var tokenUserMap:Map[String, String] = Map()
}
