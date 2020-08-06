package com.lfmunoz.rabbit


import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.lfmunoz.utils.createSingleThreadExecutor
import com.rabbitmq.client.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.sendBlocking
//import kotlinx.coroutines.channels.Channel
import org.fissore.slf4j.FluentLoggerFactory
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.concurrent.ThreadSafe

/**
 *  https://www.rabbitmq.com/java-client.html
 *  https://www.rabbitmq.com/api-guide.html
 *
 *  Publisher Confirms:
 *      https://www.rabbitmq.com/tutorials/tutorial-seven-java.html
 */
@ThreadSafe
class RabbitPublisherBare(
  var amqp: String = "amqp://guest:guest@localhost:5672",
  var exchangeConfig: RabbitExchangeConfig = RabbitExchangeConfig(),
  var compression: String = "NONE"
) {
  companion object {
    private val log = FluentLoggerFactory.getLogger(RabbitPublisherBare::class.java)
  }

  private val threadContext  = newSingleThreadContext("pThread-${(10..99).random()}")
  private val scope = CoroutineScope(threadContext)
  private val publishChannel: kotlinx.coroutines.channels.Channel<ByteArray> = kotlinx.coroutines.channels.Channel(8)

  private val factory: ConnectionFactory = ConnectionFactory()
  private val connection: Connection
  // Sharing Channel instances between threads is something to be avoided
  // Concurrent publishing on a shared channel can result in incorrect frame interleaving on the wire,
  //  triggering a connection-level protocol exception and immediate connection closure by the broker.
  // Sharing channels between threads will also interfere with Publisher Confirms.
  // Concurrent publishing on a shared channel is best avoided entirely, e.g. by using a channel per thread.
  private val rabbitChannel: Channel

  // ________________________________________________________________________________
  // PUBLIC
  // ________________________________________________________________________________
  init {
    log.info().log("[PUBLISHER CONNECT] - amqp=$amqp exchange=${exchangeConfig.name}")
    factory.setUri(amqp)
    connection = factory.newConnection()
    rabbitChannel = connection.createChannel()
    createExchange(rabbitChannel, exchangeConfig.name)
    scope.launch {
      for(byteArray in publishChannel) {
        rabbitChannel.basicPublish(exchangeConfig.name, "", byteArrayRabbitMessage, byteArray)
        log.debug().log("[PUBLISH] - sent bytearray of length ${byteArray.size}")
      }
    }
  }

  fun publish(aByteArray: ByteArray) {
    publishChannel.sendBlocking(aByteArray)
  }

  fun shutdown() {
    scope.cancel()
    publishChannel.close()
    rabbitChannel.close()
    connection.close()
  }

  // ________________________________________________________________________________
  // PRIVATE
  // ________________________________________________________________________________
  private fun createExchange(aChannel: Channel?, exchangeName: String) {
    aChannel?.exchangeDeclare(exchangeName, "fanout", false)
      ?: throw RuntimeException("[EXCHANGE DECLARE] - channel is null")
    log.info().log("[EXCHANGE CREATED] - exchange=$exchangeName")
  }

  private val byteArrayRabbitMessage: AMQP.BasicProperties =
    AMQP.BasicProperties.Builder()
      .contentType("application/octet-stream")
      .deliveryMode(1) // 1 nonpersistent, 2 persistent
      .priority(1)
      .appId("eco")
      .build()

  private fun buildTextRabbitMessage(): AMQP.BasicProperties {
    return AMQP.BasicProperties.Builder()
      .contentType("text/plain")
      .deliveryMode(1)
      .priority(1)
      .appId("eco")
      .build()
  }



}
