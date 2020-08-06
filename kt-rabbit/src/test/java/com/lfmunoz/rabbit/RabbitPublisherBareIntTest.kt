package com.lfmunoz.rabbit

import ch.qos.logback.classic.Level
import com.lfmunoz.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Integration Test:  Rabbit Consumer
 *  Depends on RabbitMQ
 *  http://localhost:15672/#/queues
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RabbitPublisherBareIntTest {

  // DEPENDENCIES
  private val testThreadPool = newFixedThreadPoolContext(4, "tThread")
  private lateinit var consumerJob: Job
  private lateinit var scope : CoroutineScope

  private val atomicInteger = AtomicInteger()
  private val messageCount = 50_000

  private val rabbitConfig = RabbitConfig().apply {
    queue.name = "consumer.test.queue"
    exchange.name = "consumer.test.exchange"
  }

  //________________________________________________________________________________
  // BEFORE ALL / AFTER ALL
  //________________________________________________________________________________
  @BeforeAll
  fun before() {
//    changeLogLevel("com.lfmunoz.rabbit.RabbitPublisherBare", Level.DEBUG)
    atomicInteger.set(0)
    consumerJob = Job()
    scope = CoroutineScope(consumerJob + testThreadPool)
    scope.launch {
      startRabbitConsumer(rabbitConfig).collect {
        atomicInteger.getAndIncrement()
      }
    }
  }

  @AfterAll
  fun after() {
    consumerJob.cancel()
  }

  //________________________________________________________________________________
  // Tests
  //________________________________________________________________________________
  @Test
  fun `simple publish`() {
    runBlocking {
      val publisher = RabbitPublisherBare(rabbitConfig.amqp, rabbitConfig.exchange)
      repeat(messageCount) {
        val aGenericData = genericDataGenerator("$it")
        val ourByteArr = mapper.writeValueAsBytes(aGenericData)
        publisher.publish(ourByteArr)
      }
      await.timeout(8, TimeUnit.SECONDS).untilAsserted {
        assertThat(atomicInteger.get()).isEqualTo(messageCount)
      }
      publisher.shutdown()
    }
  }

  @Test
  fun `multi-thread publish`() {
    val start = System.currentTimeMillis()
    val numberOfThreads = 4
    runBlocking {
      val publisher = RabbitPublisherBare(rabbitConfig.amqp, rabbitConfig.exchange)
      repeat(numberOfThreads) { _ ->
        Thread() {
          repeat(messageCount/numberOfThreads) {
            val aGenericData = genericDataGenerator("key=$it", 1_000)
            val ourByteArr = mapper.writeValueAsBytes(aGenericData)
            publisher.publish(ourByteArr)
          }
        }.start()
      }
      await.timeout(10, TimeUnit.SECONDS).untilAsserted {
        assertThat(atomicInteger.get()).isEqualTo(messageCount)
      }
      printResults(messageCount, System.currentTimeMillis() - start)
      publisher.shutdown()
    }
  }
  //________________________________________________________________________________
  // Helper methods
  //________________________________________________________________________________
  private fun startRabbitConsumer(rabbitConfig: RabbitConfig) : Flow<GenericData> {
    val consumer = RabbitConsumerBare(
      "testBasicConsumer",
      rabbitConfig.amqp,
      rabbitConfig.queue,
      rabbitConfig.exchange
    )
    return consumer.consumeFlow().map {
      return@map mapper.readValue(it.body, GenericData::class.java)
    }
  }

}



