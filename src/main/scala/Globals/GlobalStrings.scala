package Globals

import Plugins.CommonUtils.Hub.ServiceCenter.displayCode
import Plugins.CommonUtils.StringUtils

object GlobalStrings {
  val serviceCode: String = displayCode
  /** 工作目录 */
  val workingDir: String =System.getProperty("user.dir")
  /** 放treeObject的文件夹位置 */
  val treeObjectFolder : String = workingDir + StringUtils.slash + "tree-object" + StringUtils.slash
}
