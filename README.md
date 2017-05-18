# Action Service
This repository contains the Action service. This microservice is a RESTful web service implemented using [Spring Boot](http://projects.spring.io/spring-boot/).
It receives actionLifeCycle event messages via RabbitMQ from the action Service, which indicates what has happened to a action ie activation, deactivation etc
The action service will execute an action plan for each action that is actionable, off of which actions are created.
Each action follows a state transition model or path, which involves distribution of the actions to handlers, and for some types of actions, the service will expect
feedback messages indicating successful downstream processing of the action or otherwise by the handler.

The action service is agnostic of what any given handler will actually do with the action sent to it, and as such, will send the same format of ActionInstruction message to each handler.
It is upto the handler to pick out what information is relevant to it from the instruction sent to it by this service.


* to run actionsvc

      cd code/rm-action-service
      mvn clean install
      cd actionsvc
      ./mvnw spring-boot:run


## API
See [API.md](https://github.com/ONSdigital/rm-action-service/blob/master/API.md) for API documentation.

## Copyright
Copyright (C) 2017 Crown Copyright (Office for National Statistics)
