package com.lfmunoz.rabbit

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import org.fissore.slf4j.FluentLoggerFactory
import org.slf4j.LoggerFactory

class RabbitAdminBare(
  private val amqp: String = "amqp://guest:guest@localhost:5672"
) {

  companion object {
    private val log = FluentLoggerFactory.getLogger(RabbitAdminBare::class.java)
  }

  val factory: ConnectionFactory = ConnectionFactory().apply {
    setUri(amqp)
    // Attempt recovery every 5 seconds
    isAutomaticRecoveryEnabled = true
  }

  private fun createExchange(aChannel: Channel, exchangeName: String) {
    aChannel.exchangeDeclare(exchangeName, "fanout", false)
    log.info().log("[EXCHANGE CREATED] - exchange=$exchangeName")
  }

  /*
  fun createQueue() {
    val queueArgs: Map<String, Any> = mutableMapOf()
    if (queueConfig.messageTtl != 0) {
      queueArgs.plus("x-message-ttl" to queueConfig.messageTtl)
    }
    if (queueConfig.maxLength != 0) {
      queueArgs.plus("x-max-length" to queueConfig.maxLength)
    }
    rabbitChannel.exchangeDeclare(exchangeConfig.name, exchangeConfig.type, exchangeConfig.durable)
    val queueOk = rabbitChannel.queueDeclare(queueConfig.name, queueConfig.durable,
      RabbitConsumerBare.queueIsExclusive, queueConfig.autoDelete, queueArgs)
    rabbitChannel.queueBind(queueOk.queue, exchangeConfig.name, "")
    RabbitConsumerBare.log.info("[QUEUE CREATED] - queue=${queueConfig}")
  }

   */


}
