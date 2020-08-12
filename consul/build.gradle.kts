import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//________________________________________________________________________________
// DEPENDENCY VERSIONS
//________________________________________________________________________________

//________________________________________________________________________________
// PLUGINS
//________________________________________________________________________________
plugins {
  java
}

//________________________________________________________________________________
// PROJECT SETTINGS
//________________________________________________________________________________
val artifactId = "consul"

//________________________________________________________________________________
// DEPENDENCIES
//________________________________________________________________________________
dependencies {
  implementation(project(":common"))
  // CONSUL CLIENT
  implementation("com.ecwid.consul:consul-api:1.4.5")
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
