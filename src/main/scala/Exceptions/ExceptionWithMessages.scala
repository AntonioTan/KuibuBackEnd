package Exceptions

import Plugins.CommonUtils.CommonExceptions.ExceptionWithMessage

case class NoAuthorityToGetAnswersException() extends ExceptionWithMessage{
  override val message: String = "你无权限获取答案！"
}

case class UserNotExistedException() extends ExceptionWithMessage{
  override val message: String="错误：用户不存在！"
}

