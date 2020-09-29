package com.lfmunoz.kafka

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.newSingleThreadContext
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.fissore.slf4j.FluentLoggerFactory
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Kafka Consumer that maintains state seems inferior to
 *  KafkaConsumerBare.kt so this is deprecated
 *
 * https://kafka.apache.org/documentation/
 */
@Deprecated("Use KafkaConsumerBare")
class KafkaConsumer2(
  private val aKafkaConfig: KafkaConfig
) {

  companion object {
    private val log = FluentLoggerFactory.getLogger(KafkaConsumer2::class.java)
    private val POLL_INTERVAL_MS: Duration = Duration.ofMillis(200)
  }

  private val threadContext = newSingleThreadContext("kafkaSrc${(10..99).random()}")
  private val scope = CoroutineScope(threadContext)
  private val aKafkaConsumer = KafkaConsumer<ByteArray, ByteArray>(props(aKafkaConfig))

  fun connect() = flow {
    // NOT thread-safe -  All network I/O happens in the thread of the application making the call.
    //  More consumers means more TCP connections to the cluster (one per thread).
    log.info().log("[kafka consumer connecting] - {}", aKafkaConfig)
    aKafkaConsumer.subscribe(listOf(aKafkaConfig.topic))
    while (threadContext.isActive) {
      val record = aKafkaConsumer.poll(POLL_INTERVAL_MS)
      record.forEach {
        log.trace().log("[received kafka record] - {}", it)
        emit(KafkaMessage(it.key(), it.value()))
      }
    }
  }.flowOn(threadContext)


  private fun consumerFlow(aKafkaConsumer: KafkaConsumer<ByteArray, ByteArray>, isLooping: AtomicBoolean): Flow<KafkaMessage> {
    return flow {
      try {
        while (isLooping.get()) {
          val record = aKafkaConsumer.poll(Duration.ofMillis(200))
          record.forEach {
            log.trace().log("[received kafka record] - {}", it)
            emit(KafkaMessage(it.key(), it.value()))
          }
        }
      } catch (e: CancellationException) {
        log.info().withCause(e).log("[CancellationException] - aborted")
      } catch (e: WakeupException) {
        log.info().withCause(e).log("[WakeupException] - shutdown signal")
      } catch (e: Exception) {
        log.error().withCause(e).log("[kafka consumer error] - captured exception")
      } finally {
        aKafkaConsumer.close()
      }
    }
  }

  private fun limitedConsumerFlow(count: Int, aKafkaConsumer: KafkaConsumer<ByteArray, ByteArray>): Flow<KafkaMessage> {
    return flow {
      try {
        repeat(count) {
          val record = aKafkaConsumer.poll(Duration.ofMillis(100))
          record.forEach {
            log.trace().log("[received kafka record] - {}", it)
            emit(KafkaMessage(it.key(), it.value()))
          }
        }
      } catch (e: WakeupException) {
        log.info().withCause(e).log("[wakeupException] - shutdown signal")
      } catch (e: Exception) {
        log.error().withCause(e).log("[kafka consumer error] - captured exception")
      } finally {
        aKafkaConsumer.close()
      }
    }
  }

  //________________________________________________________________________________
  // PRIVATE
  //________________________________________________________________________________
  private fun props(kafkaConfig: KafkaConfig): Properties {
    val deserializer = ByteArrayDeserializer::class.java.canonicalName
    val props = Properties()
    props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.bootstrapServer)
    props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfig.groupId)
    props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, deserializer)
    props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer)
//      props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConfig.offset)
    return props
  }

}


