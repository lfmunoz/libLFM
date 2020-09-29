#  libLFM

[![Build Status](https://travis-ci.org/lfmunoz/flink-dynamic-pipelines.svg?branch=master)](https://travis-ci.org/lfmunoz/flink-dynamic-pipelines)

This library contains easy to use client wrappers around typical third-party services.
They provide example usage and tests used for evaluation.

* common - JSON support
* Consul
* FoundationDB
* Kafka
* RabbitMQ


# Running Application

```
git pull of https://github.com/lfmunoz/libLFM
```

```
cd libLFM/weppapp
npm run build
```

```
cd libLFM
./gradlew shadowJar
java -jar ./app/build/libs/app-1.0.0-SNAPSHOT-all.jar
```




