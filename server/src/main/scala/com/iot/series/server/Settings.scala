package com.iot.series.server

import akka.actor.{Actor, ExtendedActorSystem, Extension, ExtensionKey}
import com.sandinh.paho.akka.PSConfig
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

    PSConfig(
      brokerUrl = conf.getString("url"),
      userName = Option(conf.getString("user")).filterNot(_.isEmpty).getOrElse(null),
      password = Option(conf.getString("password")).filterNot(_.isEmpty).getOrElse(null),

      stashTimeToLive = Duration(conf.getDuration("stash-time-to-live", SECONDS), SECONDS),
      stashCapacity = conf.getInt("stash-capacity"),
      reconnectDelayMin = Duration(conf.getDuration("reconnect-delay-min", MILLISECONDS), MILLISECONDS),
      reconnectDelayMax = Duration(conf.getDuration("reconnect-delay-max", SECONDS), SECONDS),
      cleanSession = true
    )
  }

}

final case class DeviceConfiguration(id:String)
