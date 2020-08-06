package com.lfmunoz.monitor.kafka

import com.lfmunoz.monitor.BashService
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.Config
import org.apache.kafka.clients.admin.DescribeConfigsResult
import org.apache.kafka.common.config.ConfigResource
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout
import java.util.*
import java.util.Collection


/**
 * Integration Test:  Kafka
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class KafkaAdminBareIntTest {

  private val bash = BashService()
  private val kafkaAdmin = KafkaAdminBash(bash)
  private val topicName = "test-collect_mm"
  private val groupId = "test-collect_mm"

  //________________________________________________________________________________
  // Tests
  //________________________________________________________________________________
  @Test
  fun `list topics`() {
    runBlocking {
      val result = kafkaAdmin.listTopics()
      println(result)
    }

  }

  @Test
  fun `describe topic`() {
    runBlocking {
      val result = kafkaAdmin.describeTopic(topicName)
      println(result)
    }

  }

  @Test
  fun `list consumer groups`() {
    runBlocking {
      val result = kafkaAdmin.listConsumerGroups()
      println(result)
    }

  }

  @Test
  fun `describe group consumer group`() {
    runBlocking {
      val result = kafkaAdmin.describeConsumerGroup(groupId)
      println(result)
    }

  }

  @Test
  fun `disk usage`() {
    runBlocking {
      val result = kafkaAdmin.diskUsage(topicName)
      println(result)
    }
  }


  @Test
  fun `last message`() {
    runBlocking {
      val result = kafkaAdmin.getLastMessage("mapper-topic")
      println(result)
    }
  }

}
