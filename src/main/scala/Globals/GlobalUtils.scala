package Globals
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

object GlobalUtils {
  def convertDateTimeToWebString(date: DateTime): String = {
    val fmt = DateTimeFormat.forPattern("yyyy/MM/dd")
    fmt.print(date)
  }

  def convertDateTimeToWebTimeString(date: DateTime): String = {
    val fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    fmt.print(date)

//    s"${date.getYear}-${date.monthOfYear().get()}-${date.dayOfMonth().get()}T${date.hourOfDay().get()}:${date.minuteOfHour().get()}:${date.secondOfMinute().get()}"
  }

}
