package com.iot.series.server

import akka.actor.{Actor, ExtendedActorSystem, Extension, ExtensionKey}
import com.sandinh.paho.akka.MqttPubSub.PSConfig
import com.typesafe.config.{ConfigObject, Config}

import scala.collection.JavaConversions._
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.reflect.internal.util.StringOps

object Settings extends ExtensionKey[Settings]

class Settings(system: ExtendedActorSystem) extends Extension {
  val config = system.settings.config

  val deviceConfigurations = config.getConfigList("device-configurations").map(buildDeviceConfig).toList

  // Remote Listen for device events
  val deviceMonitorListeners = config.getStringList("device-monitor-listeners").toSet

  private def buildDeviceConfig(config: Config):DeviceConfiguration = DeviceConfiguration(config.getString("id"))

  val messageBrokerConfig = {

    val conf = config.getConfig("message-broker")

    // Nulls are not optimal but the underlying api doesn't accept and empty string.
    PSConfig(
      brokerUrl = conf.getString("url"),
      userName = Option(conf.getString("user")).filterNot(_.isEmpty).getOrElse(null),
      password = Option(conf.getString("password")).filterNot(_.isEmpty).getOrElse(null),

      //messages received when disconnected will be stashed. Messages isOverdue after stashTimeToLive will be discard
      stashTimeToLive = Duration(conf.getDuration("stash-time-to-live", SECONDS), SECONDS),
      stashCapacity = conf.getInt("stash-capacity"), //stash messages will be drop first haft elems when reach this size
      reconnectDelayMin = Duration(conf.getDuration("reconnect-delay-min", MILLISECONDS), MILLISECONDS),
      reconnectDelayMax = Duration(conf.getDuration("reconnect-delay-max", SECONDS), SECONDS),
      cleanSession = true
    )
  }

}

final case class DeviceConfiguration(id:String)
