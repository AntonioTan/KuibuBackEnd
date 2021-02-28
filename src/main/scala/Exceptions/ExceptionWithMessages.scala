package Exceptions

import Plugins.CommonUtils.CommonExceptions.ExceptionWithMessage

case class UserNotExistedException() extends ExceptionWithMessage{
  override val message: String="错误：用户不存在！"
}

