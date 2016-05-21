package com.iot.series.server

import com.typesafe.scalalogging.StrictLogging
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

/**
 * Base spec for unit tests.
 */
trait UnitSpec extends WordSpecLike with MustMatchers with OptionValues with EitherValues with ScalaFutures with StrictLogging {
  //Disable "should" and "can" verbs to force consistent use of "must". Disabled through overriding the implicit conversions
  override def convertToStringShouldWrapper(o: String) = super.convertToStringShouldWrapper(o)
  override def convertToStringCanWrapper(o: String) = super.convertToStringCanWrapper(o)
}
