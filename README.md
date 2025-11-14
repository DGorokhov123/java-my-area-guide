# My Area Guide

My Area Guide is a web application for people who want to be in touch with 
current events in their local areas.

People can view the list of events, make participation requests, get approvals or rejections, 
leave comments.

Event makers can start new events and manage participants lists

Administrators can manage events, users, categories and make compilations of events. 
Also, they have statistics microservice to control important metrics. 

# Infrastructure modules (folder `/infra`)

## Discovery Service

The module `discovery-server` implements Service Registry - the central part of 
Service Discovery pattern. 

Stack:
- String Boot
- Spring Cloud Eureka server 

## Configuration Service

The module `config-server` implements an External Configuration pattern as a central storage 
of configuration files for all microservices except discovery.

Stack:
- String Boot
- Spring Cloud Config server

## Gateway Service

The module `gateway-server` implements an API Gateway pattern. It works as a single entry point
for all microservices. 

Stack:
- String Boot
- Spring Cloud Gateway server

# Application core modules (folder `/core`)

The Microservice-based application contains 4 integrated services. They have separate databases 
(implemented as different schemas in this release). The interaction is realized through Feign-clients with circuit breaker. 

Stack:
- Java ٩(◕‿◕｡)۶
- String Boot
- Spring MVC (REST)
- Spring Data JPA (Hibernate)
- Spring Cloud Discovery and Configuration
- OpenFeign client + Resilience4j
- PostgreSQL

## Common library

The module `core-common` contains common classes and interfaces used by all core modules such as:
- API description interfaces
- Feign client interaction helpers
- DTO
- Exceptions and exception handlers
- Validation custom annotations

## User Management Service

The module `user-service` provides Admin interface to manage users. It doesn't access other services
but provides them information about users.

Public API Specification: [API-user-service-specification.json](API-user-service-specification.json)

Interaction API endpoints:
- GET /admin/users/{userId}/short (returns UserShortDto to common interaction)
- GET /admin/users/all/short (returns list of UserShortDto)
- GET /admin/users/all/full (returns list of UserDto)

## Event Management Service

The module `event-service` provides Admin, Private and Public interface to operate events - the central 
entity in application. The module interacts with request-service (to get information about requests number) 
and user-service (to get extended user information)

Public API Specification: [API-event-service-specification.json](API-event-service-specification.json)

Interaction API endpoints:
- GET /events/{id}/dto/interaction (returns shortened EventInteractionDto to common interaction)
- GET /events/{id}/dto/comment (returns EventCommentDto for comment-service)
- POST /events/dto/list/comment (returns list of EventCommentDto for comment-service)

## Participation Requests Management Service

The module `request-service` provides Private interface to operate participation requests. It interacts with
event-service and user-service (to get extended info and check existence)

Public API Specification: [API-request-service-specification.json](API-request-service-specification.json)

Interaction API endpoints:
- POST /requests/confirmed (returns map of confirmed requests by event ID list)

## Event Comments Management Service

The module `comment-service` provides Admin, Private and Public interface to operate comments to events.
It interacts with event-service and user-service (to get extended info and check existence)

Public API Specification: [API-comment-service-specification.json](API-comment-service-specification.json)

# Author, with best wishes, 
Dmitriy Gorokhov dg187@yandex.ru
feat. Ivan Griko grikoivan@yandex.ru, Ahmed Kishev kishevahmed0@yandex.ru and Yandex Practucum team.
