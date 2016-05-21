package com.iot.series.server

import akka.actor.{ActorRef, ActorSystem, Props}
import com.iot.series.server.actors.DeviceMonitor
import com.sandinh.paho.akka.MqttPubSub

object Main extends App {

  implicit val system = ActorSystem("iot-series")
  implicit val ctx = system.dispatcher

  val settings = Settings(system)

  // Start up an actor for each configured device.
  settings.deviceConfigurations.foreach { deviceConfiguration =>
    val deviceId = deviceConfiguration.id

    // Actor names don't support a forward slash in the name so we filter it out.
    val actorName = s"device-monitor.${deviceId.replaceAll("/",".")}"

    // Initialize an instance of the device monitor with the configuration settings and a device identifier.
    system.actorOf(DeviceMonitor.props(deviceId, settings), actorName)
  }

}
