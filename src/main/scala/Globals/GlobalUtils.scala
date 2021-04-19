package Globals
import org.joda.time.DateTime

object GlobalUtils {
  def convertDateTimeToWebString(date: DateTime): String = {
    s"${date.monthOfYear().get()}/${date.dayOfMonth().get()}/${date.getYear}"
  }

}
