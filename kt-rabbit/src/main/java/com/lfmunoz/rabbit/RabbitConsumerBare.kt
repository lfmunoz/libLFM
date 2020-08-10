package com.lfmunoz.rabbit

import com.google.common.io.ByteStreams
import com.lfmunoz.rabbit.RabbitAdminBare.Companion.createQueue
import com.lfmunoz.utils.Compression
import com.lfmunoz.utils.CompressionUtil
import com.lfmunoz.utils.FastByteArrayInputStream
import com.rabbitmq.client.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.CountDownLatch
import javax.annotation.concurrent.ThreadSafe


/**
 *  https://www.rabbitmq.com/java-client.html
 *  https://www.rabbitmq.com/api-guide.html
 *  Rabbit Consumer Bare Implementation
 */
@ThreadSafe
class RabbitConsumerBare(
        private val consumerTag: String, // unique identifier of consumer
        private val connectionFactory: ConnectionFactory,
        private val queueConfig: RabbitQueueConfig
) {

    companion object {
        private val log = LoggerFactory.getLogger(RabbitConsumerBare::class.java)
        // Required to be false for QoS, we will manually acknowledge batches of messages
        private const val autoAck: Boolean = false

        @Throws(IOException::class)
        fun decompress(compression: Compression, data: ByteArray): ByteArray {
            return ByteStreams.toByteArray(CompressionUtil.wrap(compression, FastByteArrayInputStream(data)))
        }
    }

    private var rabbitConnection: Connection
    // Each Channel has its own dispatch thread.
    // For the most common use case of one Consumer per Channel, this means
    //  Consumers do not hold up other Consumers.
    // Sharing Channel instances between threads is something to be avoided
    private val rabbitChannel: Channel

    init {
        log.info("[NEW CONSUMER CONNECTION] tag=$consumerTag")
        rabbitConnection = connectionFactory.newConnection()
        rabbitChannel = rabbitConnection.createChannel()
        createQueue(rabbitChannel, queueConfig) // idempotent
        // Accepts prefetchSize unack-ed message at a time
        rabbitChannel.basicQos(queueConfig.prefetch)
    }

    // ________________________________________________________________________________
    // PUBLIC
    // ________________________________________________________________________________
    fun consumeChannelWithCallback(block: (ByteArray) -> Unit ) {
        Thread {
            val latch = CountDownLatch(1)
          rabbitChannel.basicConsume(queueConfig.name, autoAck, consumerTag, object : DefaultConsumer(rabbitChannel) {

                override fun handleDelivery(
                    consumerTag: String?,
                    envelope: Envelope,
                    properties: AMQP.BasicProperties,
                    body: ByteArray?
                ) {
                    val deliveryTag: Long = envelope.deliveryTag
                    //decompress if it the message is compressed
                    body?.let {
                      block(it)
                    }
                    // We acknowledge each message individually (can be modified to ack batches)
                    channel.basicAck(deliveryTag, false)
                }

                override fun handleShutdownSignal(consumerTag: String, sig: ShutdownSignalException) {
                    log.warn("[CONSUMER SHUTDOWN] - tag=$consumerTag reason={}", sig.message)
                    latch.countDown()
                }
            })
            latch.await()
        }.run()
    }

  fun consumeFlow( ) = callbackFlow<RabbitMessage> {
    rabbitChannel.basicConsume(queueConfig.name, autoAck, consumerTag, object : DefaultConsumer(rabbitChannel) {
      // Callbacks to Consumers are dispatched in a thread pool separate from the thread that instantiated its Channel
      override fun handleDelivery(
        consumerTag: String?,
        envelope: Envelope,
        properties: AMQP.BasicProperties,
        body: ByteArray?
      ) {
        val deliveryTag: Long = envelope.deliveryTag
        //decompress if it the message is compressed
        body?.let { sendBlocking(RabbitMessage(it, properties)) }
        // We acknowledge each message individually (can be modified to ack batches)
        // It is important to consider what thread does the acknowledgement.
        channel.basicAck(deliveryTag, false)
      }

      override fun handleShutdownSignal(consumerTag: String, sig: ShutdownSignalException) {
        log.warn("[CONSUMER SHUTDOWN] - tag=$consumerTag reason={}", sig.message)
      }

    })
    awaitClose { shutdown() }
  }

    fun shutdown() {
      rabbitChannel.close()
      rabbitConnection.close()
    }

    // ________________________________________________________________________________
    // PRIVATE
    // ________________________________________________________________________________
    fun queueBind(exchangeConfig: RabbitExchangeConfig) {
      rabbitChannel.queueBind(queueConfig.name, exchangeConfig.name, "")
    }

} // end of RabbitConsumerBare


