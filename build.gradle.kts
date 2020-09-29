//________________________________________________________________________________
// BUILD SCRIPT
//________________________________________________________________________________
buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath(kotlin("gradle-plugin", version = "1.3.72"))
  }
}

plugins {
  kotlin("jvm") version "1.3.72"

}

//________________________________________________________________________________
// VERSIONS
//________________________________________________________________________________
val flinkVersion by extra("1.8.2")
val flinkScalaRuntime by extra { "2.12" }
val guavaVersion by extra { "28.0-jre" }
val juniperVersion by extra { "5.4.0" }
val jacksonVersion by extra { "2.10.3"}
val kafkaVersion by extra  { "2.4.0"}
val kotlinVersion by extra { "1.3.72"}
val kotlinCoroutinesVersion by extra { "1.3.5"}
val koinVersion by extra { "2.1.5"}
val vertxVersion by extra {"3.9.0"}

//________________________________________________________________________________
// GLOBAL
//________________________________________________________________________________
allprojects {

  apply {
    plugin("kotlin")
  }

  group = "com.lfmunoz"
  version = "1.0.0-SNAPSHOT"


  repositories {
    mavenCentral()
    jcenter()
  }

  dependencies {
    // LOGGING
    implementation("org.fissore:slf4j-fluent:0.12.0")
    implementation( "ch.qos.logback:logback-classic:1.2.3")
    implementation( "ch.qos.logback:logback-core:1.2.3")
    implementation( "org.codehaus.janino:janino:3.0.8")

    // KOTLIN
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation(kotlin("stdlib-jdk8", kotlinVersion))

    // JSON
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")

    // TEST
    testImplementation("org.awaitility:awaitility:2.0.0")
    testImplementation("org.awaitility:awaitility-kotlin:4.0.1")
    testImplementation("io.mockk:mockk:1.9")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$juniperVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$juniperVersion")
  }

  tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
      kotlinOptions.jvmTarget = "1.8"
    }
  }
}

