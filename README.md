# Cloud-Native-Project

# Overview

This project is built to demonstrate my understanding of Java, developing a cloud-native e-commerce system. The goal is a modular microservices setup with a configuration server, two domain services, CRUD REST APIs, containerization, and two runtime profiles (dev and prod).

# Services

configserver – Spring Cloud Config Server (used for the native backend)

catalog-service – Product CRUD (including Postgres)

order-service – Order CRUD (including Postgres)

# Ports

Config Server: 8888

Catalog Service: 8081

Order Service: 8082

Postgres (catalog): host 5433 → container 5432

Postgres (orders): host 5434 → container 5432

# Tech Stack

Java 17, Maven

Spring Boot 3.3.x, Spring Data JPA, Hibernate

Spring Cloud Config (2023), Spring Retry (for config retries as I was running into issues)

PostgreSQL 16

Docker / Docker Compose

Postman for API verification

# Prerequisites

Docker Desktop (or compatible Docker Engine)

Java 17 and Maven (for local builds)

Postman (Desktop App)

# Build (for local JARs)

from repo root:

mvn -DskipTests clean package


This produces bootable JARs under each module’s target/.


Run (Docker Compose):
cd docker

docker compose up -d --build

docker compose ps


You should see:

configserver = 0.0.0.0:8888->8888/tcp

catalog-service = 0.0.0.0:8081

order-service = 0.0.0.0:8082


# Configuration (Profiles)

Config Server serves YAML from config-repo/. Two profiles are provided:

dev (default in compose)

prod (same DBs for demo, but differences in logging and banner to show visible change and functionality)

Switching profiles: edit docker/docker-compose.yml for each app:

environment:
  SPRING_PROFILES_ACTIVE: prod
  SPRING_CONFIG_IMPORT: optional:configserver:http://configserver:8888/


Then rebuild/restart the apps:

docker compose up -d --build catalog-service order-service


# REST APIs
Catalog (Product)

Base URL: http://localhost:8081/api/products

| Method     | Path                          | Notes |
| GET | /         | Lists the products |
| GET   | /{id} | Gets the product by its UUID |
| POST   | /            | Creates a new product |
| PUT   | /{id}            | Updates selected fields within product table |
| DELETE  | /{id}           | Deletes a product |

Orders

Base URL: http://localhost:8082/api/orders



| Method |	Path |	Notes |
| GET |	/ |	Lists the orders |
| GET |	/{id} |	Gets the order by its UUID |
| POST |	/ |	Creates an order |
| PUT |	/{id} |	Updates selected fields within order table |
| DELETE |	/{id} |	Deletes an order |

# Postman Workspace
With the Postman collection imported to Postman Desktop:
  Run the folders in order: Health, Catalog, Orders.
  “Create” requests capture productId / orderId for subsequent calls. The order create uses {{$timestamp}} to avoid duplicate order numbers.
