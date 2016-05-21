package com.iot.series.server.actors

import java.io.File
import java.util.{UUID, Properties}

import akka.actor._
import com.iot.series.server.Settings
import com.sandinh.paho.akka.MqttPubSub
import com.sandinh.paho.akka.MqttPubSub.Message
import com.sun.xml.internal.ws.encoding.MtomCodec.ByteArrayBuffer
import com.typesafe.scalalogging.StrictLogging
import io.moquette.BrokerConstants
import io.moquette.BrokerConstants._
import io.moquette.interception.AbstractInterceptHandler
import io.moquette.interception.messages._
import io.moquette.proto.messages.AbstractMessage
import io.moquette.proto.messages.PublishMessage
import io.moquette.server.Server
import io.moquette.server.config.{MemoryConfig, IConfig}
import org.mockito.Mockito._
import com.sandinh.paho.akka.MqttPubSub.{SubscribeAck, Subscribe, PSConfig, Message}
import akka.testkit.{TestProbe, TestKitBase, TestKit}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import scala.concurrent.duration._
import java.nio.ByteBuffer

import scala.collection.JavaConversions._


trait MQTTUtils extends StrictLogging with TestKitBase with MockitoSugar {

  val PORT: Integer

  var broker: Option[Server] = None

  var subscriberActor: Option[ActorRef] = None

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

    val settings = mock[Settings]
    val config = PSConfig( brokerUrl = s"tcp://127.0.0.1:$PORT" )
    when(settings.messageBrokerConfig) thenReturn config
    subscriberActor = Some(system.actorOf(Props(new MqttPubSub(settings.messageBrokerConfig)),
      s"message-subscriber-${UUID.randomUUID().toString}"))

    val probe = TestProbe()
    subscriberActor.get ! Subscribe("foo", probe.ref)
    probe.expectMsgClass(classOf[SubscribeAck])

    val pingProbe = TestProbe()
    val testSubscriber = system.actorOf(Props(new TestSubscriber(subscriberActor.get, pingProbe.ref)))
    pingProbe.watch(testSubscriber)

    val pingMsg = buildMessage("foo", "ping")
    awaitAssert({
      publish(pingMsg)
      pingProbe.expectMsg("ping")
    }, 5 seconds, 100 millis)
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

class TestSubscriber(subAct: ActorRef, probe: ActorRef) extends Actor with ActorLogging {
  import scala.concurrent.ExecutionContext.Implicits.global

  context.system.scheduler.scheduleOnce(1 second, self, SubscribeToTopic)

  // Start in the un-initialized state
  override def receive: Actor.Receive = unInitialized

  def unInitialized: Receive = {
    case ReceiveTimeout ⇒
      // Resubscribe if we haven't received a subscription ack yet.
      context.system.scheduler.scheduleOnce(1 second, self, SubscribeToTopic)

    case SubscribeAck(Subscribe(subscriberTopic, ref, _)) ⇒
      context.become(initialized)

    case SubscribeToTopic ⇒
      subAct ! Subscribe("foo", self)
      context.setReceiveTimeout(10 seconds)
  }

  def initialized: Receive = {
    case msg: Message =>
      val payload = (msg.payload map (_.toChar)).mkString("")
      probe ! payload
  }

  case object SubscribeToTopic
}