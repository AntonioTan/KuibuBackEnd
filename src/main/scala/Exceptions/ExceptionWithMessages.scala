package Exceptions

import Plugins.CommonUtils.CommonExceptions.ExceptionWithMessage

case class UserNotExistedException() extends ExceptionWithMessage{
  override val message: String="错误：用户不存在！"
}

case class PasswordNotMatchedException() extends ExceptionWithMessage{
  override val message: String="错误：密码不匹配！"
}
