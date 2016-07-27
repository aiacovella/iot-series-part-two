package com.iot.series.server.actors

import akka.actor._
import com.iot.series.server.actors.DeviceMonitor.Protocol._
import com.iot.series.server.Settings
import java.time.Instant

import com.sandinh.paho.akka._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Try

class DeviceMonitor(
  deviceId: String,
  settings: Settings,
  now: () => Instant = () => Instant.now()
)(implicit executionContext: ExecutionContext)
  extends Actor
    with ActorLogging {

  log.info(s"Initializing monitoring for topic $deviceId")

  override def postStop(): Unit = {
    log.debug(s"Device monitor stopped for device $deviceId")
  }

  private val subscriberActor: ActorRef = {
    log.info("Broker Configuration: " + settings.messageBrokerConfig)
    context.actorOf(Props(new MqttPubSub(settings.messageBrokerConfig)),"device-monitor-supervisor-subscriber")
  }

  context.system.scheduler.scheduleOnce(1.second, self, SubscribeToTopic)

  @throws[Exception](classOf[Exception])
  override def postRestart(reason: Throwable): Unit = {
    super.postRestart(reason)
    log.error(s"Device monitor restarted", reason)
  }

  // Start in the un-initialized state
  override def receive: Actor.Receive = unInitialized

  def unInitialized: Receive = {

    case SubscribeToTopic ⇒
      log.debug(s"Subscribing to topic ")
      subscriberActor ! Subscribe(deviceId, self)
      context.setReceiveTimeout(10.seconds)

    case ReceiveTimeout ⇒
      log.error("Timed out while attempting to connection to message server, retrying.")
      // Resubscribe if we haven't received a subscription ack yet.
      context.system.scheduler.scheduleOnce(1.second, self, SubscribeToTopic)

    case SubscribeAck(_, fail) =>
      fail match {
        case Some(err) ⇒
          // The Paho-Akka library should have returned a Try here rather than an option of error.
          log.error(s"Subscription to MQTT server failed for device $deviceId. Re-attempting connection.")

          context.system.scheduler.scheduleOnce(5.seconds, self, SubscribeToTopic)

        case None ⇒
          log.debug(s"${self.path} is subscribed to topic $deviceId")

          // Reset timeout to infinite.
          context.setReceiveTimeout(Duration.Undefined)
          dispatchEvent(MonitoringInitialized(deviceId))
          context.become(initialized)
      }
  }

  def initialized: Receive = {
    case msg: Message =>
      parseMessage(msg).foreach{event ⇒
        log.debug(s"Received device data ${event.value} for device ${deviceId}")
        dispatchEvent(event)
      }

  }

  private def parseMessage(message: Message): Option[DeviceDataReceived] = {
    val msg: String = (message.payload map (_.toChar)).mkString("")
    log.debug(s"Monitor Received MQTT message: $msg")
    Try(Some(DeviceDataReceived(message.topic, BigDecimal(msg), now()))).getOrElse(None)
  }

  private def dispatchEvent(event: MonitorEvent) = {
    log.debug(s"Dispatching device event $event for device $deviceId")

    settings.deviceMonitorListeners.foreach { listener ⇒
      log.debug(s"Dispatching $event for device $deviceId to $listener")
      context.actorSelection(listener) ! event
    }
  }

}

object DeviceMonitor {

  object Protocol {

    sealed trait Messages
    case object SubscribeToTopic extends Messages

    sealed trait MonitorEvent {
      def deviceId: String
    }

    final case class DeviceDataReceived(deviceId: String, value: BigDecimal, timeStamp: Instant) extends MonitorEvent
    final case class MonitoringInitialized(deviceId: String) extends MonitorEvent

  }

  def props(deviceId: String, settings: Settings, now: () => Instant = () => Instant.now)(implicit executionContext: ExecutionContext): Props
    = Props(new DeviceMonitor(deviceId, settings, now))

}


