package com.lfmunoz.kafka

import com.lfmunoz.utils.GenericData
import com.lfmunoz.utils.genericDataGenerator
import com.lfmunoz.utils.printResults
import com.lfmunoz.utils.toWarning
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.fissore.slf4j.FluentLoggerFactory
import org.junit.jupiter.api.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Integration Test:  Kafka
 *  Depends on Kafka
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaIntTest {

  // Dependencies
  private val tPool = newFixedThreadPoolContext(4, "tPool")

  companion object {
    private val log = FluentLoggerFactory.getLogger(KafkaIntTest::class.java)
  }

  //________________________________________________________________________________
  // BEFORE ALL / AFTER ALL
  //________________________________________________________________________________
  @BeforeAll
  fun before() {
    toWarning("org.apache.kafka.clients.producer.ProducerConfig")
    toWarning("org.apache.kafka.clients.consumer.ConsumerConfig")
    toWarning("org.apache.kafka.common.metrics.Metrics")
    toWarning("org.apache.kafka.clients.producer.KafkaProducer")
    toWarning("org.apache.kafka.clients.consumer.KafkaConsumer")
    toWarning("org.apache.kafka.common.utils.AppInfoParser")
    toWarning("org.apache.kafka.clients.NetworkClient")
    toWarning("org.apache.kafka.clients.Metadata")
    toWarning("org.apache.kafka.common.network.Selector")
  }

  //________________________________________________________________________________
  // Tests
  //________________________________________________________________________________
  @Test
  fun `simple publish and consume`() {
    val totalMessages = 1_000
    val atomicInteger = AtomicInteger(0)
    val aKafkaConfig = KafkaConfig().apply {
      topic = "test-topic-${(100..999).random()}"
      groupId = "test-groupId-${(100..999).random()}"
    }

    val scope = CoroutineScope(tPool)
    // CONSUMER
    KafkaConsumerBare.connect(aKafkaConfig)
      .map { GenericData.fromByteArray(it.value) }
      .onEach {
        log.debug().log("received ${it.key}")
        atomicInteger.getAndIncrement()
      }.launchIn(scope)
    Thread.sleep(1000L)

    // PRODUCER
    KafkaPublisherBare.connect(aKafkaConfig, flow {
      repeat(totalMessages) {
        log.debug().log("sending key=${it}")
        emit(generateKafkaMessage(it.toLong()))
      }
    }).launchIn(scope)

    await.timeout(5, TimeUnit.SECONDS).untilAsserted {
      assertThat(atomicInteger.get()).isEqualTo(totalMessages)
    }
    scope.cancel()
  } // end of Test

  @Test
  fun `multi-thread publish and consume`() {
    val totalMessages = 10_000
    val parallelism = 4
    val atomicInteger = AtomicInteger(0)
    val aKafkaConfig = KafkaConfig().apply {
      topic = "test-topic-${(100..999).random()}"
      groupId = "test-groupId-${(100..999).random()}"
    }


    val scope = CoroutineScope(tPool)
    // CONSUMER
    KafkaConsumerBare.connect(aKafkaConfig)
      .map { GenericData.fromByteArray(it.value) }
      .onEach {
        log.debug().log("received ${it.key}")
        atomicInteger.getAndIncrement()
      }.launchIn(scope)
    Thread.sleep(1000L)

    val start = System.currentTimeMillis()
    // PRODUCER
    repeat(parallelism) {
      KafkaPublisherBare.connect(aKafkaConfig, flow {
        repeat(totalMessages/parallelism) {
          log.debug().log("sending key=${it}")
          emit(generateKafkaMessage(it.toLong()))
        }
      }).launchIn(scope)
    }

    await.timeout(5, TimeUnit.SECONDS).untilAsserted {
      assertThat(atomicInteger.get()).isEqualTo(totalMessages)
    }
    val stop = System.currentTimeMillis()
    printResults(totalMessages,  stop - start)
    scope.cancel()
  } // end of Test


  //________________________________________________________________________________
  // Helper methods
  //________________________________________________________________________________
  private fun generateKafkaMessage(id: Long): KafkaMessage {
    val aGenericData = genericDataGenerator("key=${id}")
    return KafkaMessage(
      aGenericData.key.toByteArray(),
      aGenericData.toByteArray()
    )
  }


}



