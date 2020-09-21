package com.lfmunoz.consul

import com.lfmunoz.utils.readPropertiesFile
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.MergeResult
import org.eclipse.jgit.transport.FetchResult
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.fissore.slf4j.FluentLoggerFactory
import java.io.File


class GitService(
  val repoName: String,
  val gitUri: String
) {
  companion object {
    private val log = FluentLoggerFactory.getLogger(GitService::class.java)
  }

  private var git: Git? = null

  private val gitDir = "/home/luis/.c0ckp1t/git/$repoName"
  private val ctxDir = "/home/luis/.c0ckp1t/ctx/"
  private val propertiesFile = "ctx.properties"

  private val properties: Map<String, String> = readPropertiesFile("$ctxDir/$propertiesFile")
  private val credentials = UsernamePasswordCredentialsProvider(
    properties["gitUsername"], properties["gitPassword"] )


  fun open() {
    git = Git.open(File(gitDir))
  }

  fun clone() {
    val git: Git = Git.cloneRepository()
      .setURI(gitUri)
      .setDirectory(File(gitDir))
      .setCredentialsProvider(credentials)
      .call()
  }


  fun pull() : String {
    if(null == git)  throw RuntimeException("Git repo has not been opened")
    val pull = git!!.pull().apply {
      setCredentialsProvider(credentials)
      remote = "origin"
      remoteBranchName = "master"
    }
    val result = pull.call()
    val fetchResult: FetchResult = result.getFetchResult()
    val mergeResult: MergeResult = result.getMergeResult()
//    println(mergeResult.getMergeStatus())
    return mergeResult.mergeStatus.toString()
  }


    // init
  // Git git = Git.init().setDirectory("/path/to/repo").call();

  // ObjectId head = repository.resolve("HEAD");

  // Ref HEAD = repository.getRef("refs/heads/master");

  // Git git = new Git(db);
  //AddCommand add = git.add();
  //add.addFilepattern("someDirectory").call();


  // Git git = new Git(db);
  //CommitCommand commit = git.commit();
  //commit.setMessage("initial commit").call();


  // Git git = new Git(db);
  //Iterable<RevCommit> log = git.log().call();



}
