#  libLFM

[![Build Status](https://travis-ci.org/lfmunoz/libLFM.svg?branch=master)](https://travis-ci.org/lfmunoz/libLFM)

This library contains easy to use client wrappers around typical third-party services.
They provide example usage and tests used for evaluation.


* common - JSON support
* Consul
* FoundationDB
* Kafka
* RabbitMQ
* web - Vertx

## Requirements (tested with)

* openjdk version "1.8.0_181"
* Docker version 19.03.12, build 48a66213fe
* npm 6.14.4
* node v12.18.0

## Running Application



```
git pull of https://github.com/lfmunoz/libLFM
```

```
cd libLFM/webapp
npm run build
```

```
cd libLFM
./gradlew shadowJar
java -jar ./app/build/libs/app-1.0.0-SNAPSHOT-all.jar
```




