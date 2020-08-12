import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//________________________________________________________________________________
// DEPENDENCY VERSIONS
//________________________________________________________________________________
val kafkaVersion: String  by rootProject.extra

//________________________________________________________________________________
// PLUGINS
//________________________________________________________________________________
plugins {
  java
}

//________________________________________________________________________________
// PROJECT SETTINGS
//________________________________________________________________________________
val artifactId = "kafka"

//________________________________________________________________________________
// DEPENDENCIES
//________________________________________________________________________________
dependencies {
  implementation(project(":common"))

  // KAFKA
  implementation("org.apache.kafka:kafka-clients:$kafkaVersion")
  // ZOOKEEPER
  implementation("com.101tec:zkclient:0.11")

}

//________________________________________________________________________________
// TASKS
//________________________________________________________________________________
tasks {

  test {
    if (project.hasProperty("jenkins")) {
      systemProperty("bootstrapServer", "kafkaNet:9092")
    }
    testLogging {
      events("passed", "skipped", "failed")
      exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
      showExceptions = true
      showCauses = true
      showStackTraces = true
      showStandardStreams = false
    }
    useJUnitPlatform()
  }

}
