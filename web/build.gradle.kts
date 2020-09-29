import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//________________________________________________________________________________
// DEPENDENCY VERSIONS
//________________________________________________________________________________
val vertxVersion: String by rootProject.extra

//________________________________________________________________________________
// PLUGINS
//________________________________________________________________________________
plugins {
  java
}

//________________________________________________________________________________
// PROJECT SETTINGS
//________________________________________________________________________________
val artifactId = "web"

//________________________________________________________________________________
// DEPENDENCIES
//________________________________________________________________________________
dependencies {
  implementation(project(":common"))
  // VERTX
  implementation("io.vertx:vertx-web-client:$vertxVersion")
  implementation("io.vertx:vertx-junit5:$vertxVersion")
  implementation("io.vertx:vertx-web:$vertxVersion")
  implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
  implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
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
