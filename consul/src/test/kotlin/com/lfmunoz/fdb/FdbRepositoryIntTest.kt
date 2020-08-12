package com.lfmunoz.fdb



import com.apple.foundationdb.FDB
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

/**
 * Integration Test - FoundationDB Repository
 *   Depends on FoundationDB
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FdbRepositoryIntTest {

  data class FbTestEntity (
    val key: String,
    val value: String,
    val metadata: String
  )

  private val db  = FDB.selectAPIVersion(620).open()!!
  private val repo = FdbRepository(db, FbTestEntity::class.java, "fbSubspace", "dbNamespace")

  //________________________________________________________________________________
  // BEFORE ALL
  //________________________________________________________________________________
  @BeforeAll
  fun beforeAll() {
    runBlocking {
      repo.clear()
    }
  }

  //________________________________________________________________________________
  // Test Cases
  //________________________________________________________________________________
  @Test
  fun `simple write and read`() {
    runBlocking {
      val entity = FbTestEntity("keyA", "valueA", "metaDataA")
      repo.save(entity.key, entity)
      val read = repo.findByKey(entity.key)!!
      assertThat(read).isEqualTo(entity)
    }
  }

  @Test
  fun `simple write read delete`() {
    runBlocking {
      val entity = FbTestEntity("keyA", "valueA", "metaDataA")
      repo.save(entity.key, entity)
      val read = repo.findByKey(entity.key)!!
      assertThat(read).isEqualTo(entity)
      repo.delete(entity.key)
      val readDeleted = repo.findByKey(entity.key)
      assertThat(readDeleted).isNull()
    }
  }
  @Test
  fun `read range`() {
    runBlocking {
      repeat(500) {
        val entity = FbTestEntity("keyA$it", "valueA", "metaDataA")
        repo.save("keyA$it", entity)
      }
      val result = repo.readRange().toList()
      assertThat(result.size).isGreaterThanOrEqualTo(500)
    }
  }

  @Test
  fun `watch should work`() {
    val watchCount = 5
    val tPool= newFixedThreadPoolContext(2, "tWatch")
    runBlocking() {
      val entity = FbTestEntity("keyA", "valueA", "metaDataA")
      repo.save(entity.key, entity)
      launch(tPool) {
        repeat(watchCount) {
          val job = async(tPool) {
            repo.watch(entity.key)!!
          }
          delay(100)
          val entity = FbTestEntity("keyA", "$it", "metaDataA")
          repo.save(entity.key, entity)
          val result = job.await()
          assertThat(result.value).isEqualTo("$it")
        }
      }
    }
  }

}

