# Unified Submissions Interface Installation

## Requirements

Java 1.8
Git
Mongo 3.2.6 +
RabbitMQ 3.6.1 +

# Building and testing

# MongoDB and RabbitMQ

Install mongo and rabbitmq on to the same server where you which to run the SUBS API.  By default the system is configured to connect to 
a mongodb and rabbitmq server running on the same host.  For the tests to work you will need to start up a mongodb and rabbitmq server on your local machine.

# Building

```bash
git@github.com:EMBL-EBI-SUBS/subs.git
cd subs
./gradlew build
```

## Running the API, dispatcher and progress monitor on a local server

Execute the following gradle tasks in separate terminal sessions :

```bash
gradlew subs-api:bootRun
gradlew subs-dispatcher:bootRun
gradlew subs-progress-monitor:bootRun
```

You should then be able to point your browser to the following URL :

[http://localhost:8080/api](http://localhost:8080/api)

## USI application properties

The application is configured using spring bean application properties, all three subs components will attempt to connect to a rabbitmq and mongo instance running
on the same host.  The application can be configured using the following spring properties with the default given : 


```
spring.data.mongodb.port=27017 # Mongo server port.
spring.data.mongodb.host=localhost # Mongo server host.
spring.rabbitmq.host=localhost # RabbitMQ host
spring.rabbitmq.port=5672 # RabbitMQ port
spring.data.rest.basePath=/api # the base URI for the subs REST API (only applicable to the subs-api component) 
spring.data.mongodb.database=subs # Database name
```