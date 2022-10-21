package org.grapheco.lynx.types.time

import org.grapheco.lynx.LynxType
import org.grapheco.lynx.types.LynxValue
import org.grapheco.lynx.types.property.LynxString
import org.grapheco.lynx.types.time.LynxComponentTime.{getHourMinuteSecond, getNanosecond}
import org.grapheco.lynx.types.time.LynxComponentTimeZone.{getOffset, getZone}
import org.grapheco.lynx.util.LynxTemporalParseException
import org.grapheco.lynx.util.LynxTemporalParser.splitDateTime
import org.opencypher.v9_0.util.symbols.CTTime

import java.time.{Instant, OffsetTime, ZoneId, ZoneOffset}
import java.util.TimeZone

/**
 * @ClassName LynxTime
 * @Description TODO
 * @Author huchuan
 * @Date 2022/4/1
 * @Version 0.1
 */
case class LynxTime(offsetTime: OffsetTime) extends LynxTemporalValue with LynxComponentTime with LynxComponentTimeZone {
  def value: OffsetTime = offsetTime

  def lynxType: LynxType = CTTime

  override def sameTypeCompareTo(o: LynxValue): Int = ???

  //LynxComponentTime
  var hour: Int = offsetTime.getHour
  var minute: Int = offsetTime.getMinute
  var second: Int = offsetTime.getSecond
  var millisecond: Int = (offsetTime.getNano * Math.pow(0.1, 6)).toInt
  var microsecond: Int = (offsetTime.getNano * Math.pow(0.1, 3) - millisecond * Math.pow(10, 3)).toInt
  var nanosecond: Int = offsetTime.getNano % Math.pow(10, 3).toInt
  var fraction: Int = offsetTime.getNano

  //LynxComponentZone
  var timeZone: String = offsetTime.getOffset.getId
  var offset: String = offsetTime.getOffset.toString
  var offsetMinutes: Int = offsetTime.getOffset.getTotalSeconds / 60
  var offsetSeconds: Int = offsetTime.getOffset.getTotalSeconds

}

object LynxTime {
  def now(): LynxTime = LynxTime(OffsetTime.now())

  def of(offsetTime: OffsetTime): LynxTime = LynxTime(offsetTime)

  def of(hour: Int, minute: Int, second: Int, nanosOfSecond: Int, offset: ZoneOffset): LynxTime =
    LynxTime(OffsetTime.of(hour, minute, second, nanosOfSecond, offset))

  def parse(timeWithOffsetStr: String): LynxTime = {

    val map = splitDateTime(timeWithOffsetStr)
    val timeTuple = getHourMinuteSecond(map.get("timeStr").get)
    val offsetStr = LynxComponentTimeZone.getOffset(map.get("offsetStr").getOrElse(null))

    var timeStr = timeTuple._1.formatted("%02d") + ":" + timeTuple._2.formatted("%02d") + ":" + timeTuple._3.formatted("%02d") + (timeTuple._4 match {
      case 0 => ""
      case v: Int => "." + v.toString
    }) + offsetStr
    LynxTime(OffsetTime.parse(timeStr))
  }

  def getOffset(zoneId: ZoneId): ZoneOffset = {
    zoneId match {
      case v: ZoneOffset => v
      case v: ZoneId => v.getRules.getOffset(Instant.now)
    }
  }

  def parse(map: Map[String, Any]): LynxTime = {
    if (map.isEmpty) {
      throw LynxTemporalParseException("At least one temporal unit must be specified")
    }
    var v: OffsetTime = null
    val zoneId = getZone(map.getOrElse("timezone", "Z") match {
      case v: String => v
      case LynxString(v) => v
    })
    val zoneOffset: ZoneOffset = getOffset(zoneId)
    if (map.size == 1 && map.contains("timezone")) {
      of(OffsetTime.now(zoneOffset))
    } else {
      val (hour, minute, second) = getHourMinuteSecond(map, false)
      val nanoOfSecond = getNanosecond(map, true)
      of(hour, minute, second, nanoOfSecond, zoneOffset)

    }

  }
}
