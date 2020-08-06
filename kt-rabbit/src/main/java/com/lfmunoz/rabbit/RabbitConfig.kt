package com.lfmunoz.rabbit

import com.rabbitmq.client.BasicProperties
import java.io.Serializable

//________________________________________________________________________________
// RABBIT CONFIG
//________________________________________________________________________________
data class RabbitConfig(
  var amqp: String = "amqp://guest:guest@localhost:5672",
  var queue: RabbitQueueConfig = RabbitQueueConfig(),
  var exchange: RabbitExchangeConfig = RabbitExchangeConfig(),
  var compression: String = "NONE"
) : Serializable

data class RabbitQueueConfig(
    var name: String = "",
    var durable: Boolean = false,
    var autoDelete: Boolean = true,
    var messageTtl: Int = 60_000,
    var maxLength: Int = 100_000,
    var prefetch: Int = 5
) : Serializable

data class RabbitExchangeConfig(
    var name: String = "",
    var type: String = "fanout",
    var durable: Boolean = false,
    var autoDelete: Boolean = true
) : Serializable

//________________________________________________________________________________
// RABBIT MESSAGE
//________________________________________________________________________________
data class RabbitMessage(
  val body: ByteArray,
  val properties: BasicProperties
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as RabbitMessage

    if (!body.contentEquals(other.body)) return false
    if (properties != other.properties) return false

    return true
  }

  override fun hashCode(): Int {
    var result = body.contentHashCode()
    result = 31 * result + properties.hashCode()
    return result
  }


}
