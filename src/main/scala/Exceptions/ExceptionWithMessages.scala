package Exceptions

import Plugins.CommonUtils.CommonExceptions.ExceptionWithMessage

case class NoAuthorityToGetAnswersException() extends ExceptionWithMessage{
  override val message: String = "你无权限获取答案！"
}


