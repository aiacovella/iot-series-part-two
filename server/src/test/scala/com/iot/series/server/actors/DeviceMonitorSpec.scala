package com.iot.series.server.actors

import akka.actor._
import akka.testkit.{TestKit, TestProbe}
import com.iot.series.server.{DeviceConfiguration, Settings, UnitSpec}
import com.sandinh.paho.akka.MqttPubSub.PSConfig
import com.typesafe.config.ConfigFactory
import java.time.Instant
import java.util.UUID

import com.iot.series.server.actors.DeviceMonitor.Protocol.{DeviceDataReceived, MonitoringInitialized}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mock.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Unit tests for the [[DeviceMonitor]] actor
  */
class DeviceMonitorSpec
  extends TestKit(ActorSystem("device-monitor-spec", ConfigFactory.load()))
    with UnitSpec
    with MockitoSugar
    with MQTTUtils
    with BeforeAndAfterAll {

  val PORT: Integer = 9091

  override protected def beforeAll() = startBroker()

  override protected def afterAll() = {
    stopBroker()
    TestKit.shutdownActorSystem(system)
  }

  private val DeviceValueOne = 50125

  "DeviceMonitor" must {
    "dispatch a message to any configured listeners when it receives new device data" in new Ctx {
      probe.expectMsg(MonitoringInitialized(deviceId))

      val message = buildMessage(deviceId, DeviceValueOne.toString)

      awaitAssert({
        publish(message)
        probe.expectMsg(DeviceDataReceived(deviceId, DeviceValueOne, StaticTime))
      }, 5 seconds, 100 millis)

      act ! PoisonPill
      probe.expectTerminated(act)
    }
  }

  val StaticTime = Instant.now()

  class Ctx(
    eventsPerSnapshot: Int = 500,
    now: () => Instant = () => StaticTime,
    emitRate: Option[FiniteDuration] = None){

    val settings = mock[Settings]
    val deviceId = UUID.randomUUID().toString
    val deviceConfigurations = List(DeviceConfiguration(deviceId))
    val probe = TestProbe()

    // Configure mock settings
    when(settings.deviceConfigurations).thenReturn(deviceConfigurations)
    when(settings.messageBrokerConfig).thenReturn(PSConfig( brokerUrl = s"tcp://127.0.0.1:$PORT"))
    when(settings.deviceMonitorListeners).thenReturn(Set(probe.testActor.path.toString))

    val act = system.actorOf(DeviceMonitor.props(deviceId, settings, now), UUID.randomUUID().toString)
    probe.watch(act)
  }

}

