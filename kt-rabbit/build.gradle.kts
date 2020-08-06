import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//________________________________________________________________________________
// DEPENDENCY VERSIONS
//________________________________________________________________________________
val kotlinVersion: String  by rootProject.extra
val kotlinCoroutinesVersion: String  by rootProject.extra
val vertxVersion: String  by rootProject.extra
val koinVersion: String  by rootProject.extra
val jacksonVersion: String  by rootProject.extra
val guavaVersion: String by rootProject.extra
val juniperVersion: String by rootProject.extra
val kafkaVersion: String  = "2.4.0"

//________________________________________________________________________________
// PLUGINS
//________________________________________________________________________________
plugins {
  java
}
apply {
  plugin("kotlin")
}
//________________________________________________________________________________
// PROJECT SETTINGS
//________________________________________________________________________________
group = "com.lfmunoz"
version = "1.0.0-SNAPSHOT"
val artifactID = "flink-monitor"

//________________________________________________________________________________
// DEPENDENCIES
//________________________________________________________________________________
dependencies {
  // MISC
  implementation("com.google.guava:guava:21.0")
  implementation("net.sf.expectit:expectit-core:0.9.0")
  implementation("com.google.code.findbugs:jsr305:3.0.2")
  // LOGGING
  implementation("org.fissore:slf4j-fluent:0.12.0")
  implementation( "ch.qos.logback:logback-classic:1.2.3")
  implementation( "ch.qos.logback:logback-core:1.2.3")
  implementation( "org.codehaus.janino:janino:3.0.8")

  //RABBITMQ
  implementation("com.rabbitmq:amqp-client:5.9.0")

  // https://mvnrepository.com/artifact/net.jpountz.lz4/lz4
  implementation("net.jpountz.lz4:lz4:1.3.0")


  // KOIN
  implementation("org.koin:koin-core:$koinVersion")
  implementation("org.koin:koin-logger-slf4j:$koinVersion")
  testImplementation("org.koin:koin-test:$koinVersion")
  // KOTLIN
  implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
  implementation(kotlin("stdlib-jdk8", kotlinVersion))
  implementation(kotlin("reflect", kotlinVersion))

  // JSON
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")



  // TEST
  testImplementation("org.awaitility:awaitility:2.0.0")
  testImplementation("org.awaitility:awaitility-kotlin:4.0.1")
//  testCompile("io.mockk:mockk:1.9")
  testImplementation("org.assertj:assertj-core:3.11.1")
  testImplementation("org.junit.jupiter:junit-jupiter-api:$juniperVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$juniperVersion")
}


tasks {
  withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
  }
  withType(Test::class.java) {
    testLogging.showStandardStreams = true
    useJUnitPlatform()
  }

}
