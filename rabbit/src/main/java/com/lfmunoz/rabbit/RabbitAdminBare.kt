package com.lfmunoz.rabbit

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import org.fissore.slf4j.FluentLoggerFactory
import org.slf4j.LoggerFactory

class RabbitAdminBare {

  companion object {
    private val log = FluentLoggerFactory.getLogger(RabbitAdminBare::class.java)
    private const val queueIsExclusive: Boolean = false

    fun buildConnectionFactory(
      uri: String
    ): ConnectionFactory {
      return ConnectionFactory().apply {
        setUri(uri)
        // Attempt recovery every 5 seconds
        isAutomaticRecoveryEnabled = true
      }
    }

    fun buildConnectionFactory(
      _userName: String,
      _password: String,
      _host: String,
      _virtualHost: String
    ): ConnectionFactory {
      return ConnectionFactory().apply {
        username = _userName
        password = _password
        host = _host
        virtualHost = _virtualHost
        // Attempt recovery every 5 seconds
        isAutomaticRecoveryEnabled = true
      }
    }


    fun createExchangeAndBindQueue(
      rabbitChannel: Channel,
      queueConfig: RabbitQueueConfig,
      exchangeConfig: RabbitExchangeConfig
    ) {
      createExchange(rabbitChannel, exchangeConfig)
      createQueue(rabbitChannel, queueConfig)
      rabbitChannel.queueBind(queueConfig.name, exchangeConfig.name, "")
    }

    fun createExchange(aChannel: Channel, exchangeConfig: RabbitExchangeConfig) {
      aChannel.exchangeDeclare(exchangeConfig.name, exchangeConfig.type, exchangeConfig.durable)
      log.info().log("[EXCHANGE CREATED] - exchange=${exchangeConfig.name}")
    }

    fun createQueue(rabbitChannel: Channel, queueConfig: RabbitQueueConfig) {
      val queueArgs: Map<String, Any> = mutableMapOf()
      if (queueConfig.messageTtl != 0) {
        queueArgs.plus("x-message-ttl" to queueConfig.messageTtl)
      }
      if (queueConfig.maxLength != 0) {
        queueArgs.plus("x-max-length" to queueConfig.maxLength)
      }
      val queueOk = rabbitChannel.queueDeclare(queueConfig.name, queueConfig.durable,
        queueIsExclusive, queueConfig.autoDelete, queueArgs)
      log.info().log("[QUEUE CREATED] - queue=${queueConfig}")
    }

  } // end of companion object

}
