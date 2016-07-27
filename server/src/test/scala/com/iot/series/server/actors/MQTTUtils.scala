package com.iot.series.server.actors

import java.io.File
import java.util.Properties

import akka.testkit.TestKitBase
import com.typesafe.scalalogging.StrictLogging
import io.moquette.BrokerConstants
import io.moquette.BrokerConstants._
import io.moquette.interception.AbstractInterceptHandler
import io.moquette.interception.messages._
import io.moquette.proto.messages.AbstractMessage
import io.moquette.proto.messages.PublishMessage
import io.moquette.server.Server
import io.moquette.server.config.{IConfig, MemoryConfig}
import java.nio.ByteBuffer

import scala.collection.JavaConversions._

trait MQTTUtils extends StrictLogging with TestKitBase {

  val PORT: Integer

  var broker: Option[Server] = None

  def stopBroker() = {
    logger.debug("Stopping broker")
    broker.foreach(_.stopServer())
  }

  def publish(msg: PublishMessage) = broker.foreach(_.internalPublish(msg))

  def clearBrokerCache(): Unit = {
    val currentDir: String = System.getProperty("user.dir")
    val fileName = currentDir + File.separator + DEFAULT_MOQUETTE_STORE_MAP_DB_FILENAME

    val dbFile = new File(fileName)
    if (dbFile.exists) {
      dbFile.delete
      new File(fileName + ".p").delete
      new File(fileName + ".t").delete
    }
  }

  def startBroker() = {

    val tmp = System.getProperty("java.io.tmpdir")

    val props = new Properties()
    props.put("port", PORT.toString)
    props.put("persistent_store", BrokerConstants.DEFAULT_PERSISTENT_PATH)

    val classPathConfig: IConfig = new MemoryConfig(props)

    val mqttBroker: Server = new Server()

    val userHandlers = List(new PublisherListener())

    mqttBroker.startServer(classPathConfig, userHandlers)

    val message:PublishMessage = new PublishMessage()
    message.setTopicName("/exit")
    message.setRetainFlag(false)
    message.setQos(AbstractMessage.QOSType.MOST_ONE)

    broker = Some(mqttBroker)
  }

  def buildMessage(topic: String, msg: String) = {
    val message:PublishMessage = new PublishMessage()
    message.setTopicName(topic)
    message.setRetainFlag(false)
    message.setQos(AbstractMessage.QOSType.LEAST_ONE)
    message.setPayload(ByteBuffer.wrap(msg.getBytes))
    message
  }

}

class PublisherListener extends AbstractInterceptHandler with StrictLogging {

  override def onPublish(msg: InterceptPublishMessage): Unit = {
    super.onPublish(msg)
    msg.getPayload.array()
    logger.debug(s"Received on topic: ${msg.getTopicName} content: ${msg.getPayload.array().map(_.toChar).mkString}")
  }

}
