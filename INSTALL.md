# Unified Submissions Interface Installation

## Requirements

 * Java 1.8
 * Git
 * Mongo 3.2.6 +
 * RabbitMQ 3.6.1 +

# MongoDB and RabbitMQ

Install mongo and rabbitmq on to the same server where you which to run the SUBS API.  By default the system is configured to connect to 
a mongodb and rabbitmq server running on the same host.  For the tests to work you will need to start up a mongodb and rabbitmq server on your local machine.

# Building and testing

```bash
git clone git@github.com:EMBL-EBI-SUBS/subs.git
cd subs
```

## Running on a local server

This will run the API, dispatcher, progress monitor and archive agents representing BioSamples and ArrayExpress on a local server.

Execute each of the following gradle tasks in a different terminal session :

```bash
./gradlew subs-api:bootRun
./gradlew subs-dispatcher:bootRun
./gradlew subs-progress-monitor:bootRun
./gradlew subs-api-support:bootRun
./gradlew subs-samples-prototype-agent:bootRun
./gradlew subs-aeagent:bootRun
```

You should then be able to point your browser to the following URL :

[http://localhost:8080/api](http://localhost:8080/api)

## USI application properties

The application is configured using spring bean application properties. By default, all six components will attempt to connect to a rabbitmq and mongo instance running
on the same host.  The application can be configured using the following spring properties with the defaults given : 

```
spring.data.mongodb.port=27017 # Mongo server port 
spring.data.mongodb.host=localhost # Mongo server host 
spring.rabbitmq.host=localhost # RabbitMQ host 
spring.rabbitmq.port=5672 # RabbitMQ port 
spring.data.rest.basePath=/api # the base URI for the subs REST API (only applicable to the subs-api component) 
spring.data.mongodb.database=subs # Database name
server.port=8080 # the HTTP port the server will listen on (only applicable to the subs-api component) 
```

The project jar is an executable spring boot application and can be executed directly on a UNIX environment.  

For example to start up the subs-api component execute the following :

```bash
java -jar subs-api/build/libs/subs-api-1.0.0-SNAPSHOT.jar
```

The can also overide any application properties on the command line, for example to start up the subs-api with a mongo server on different host name :
 
```bash
java -jar subs-api/build/libs/subs-api-1.0.0-SNAPSHOT.jar --spring.data.mongodb.host=example-mongo-server.com
```



