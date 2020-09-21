package com.lfmunoz.fdb



import com.lfmunoz.consul.GitService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

/**
 * Integration Test - FoundationDB Repository
 *   Depends on FoundationDB
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GitServiceIntTest {

  val docsGitUri = "https://github.com/lfmunoz/documents"

  //________________________________________________________________________________
  // BEFORE ALL
  //________________________________________________________________________________
  @BeforeAll
  fun beforeAll() {
    runBlocking {
    }
  }

  //________________________________________________________________________________
  // Test Cases
  //________________________________________________________________________________
  @Test
  fun `git clone works`() {
    val git = GitService( "documents", docsGitUri)
    git.clone()
  }

  @Test
  fun `git pull works`() {
    val git = GitService( "documents", docsGitUri)
    git.open()
    assertThat(git.pull()).isEqualTo("Already-up-to-date")
  }


}

