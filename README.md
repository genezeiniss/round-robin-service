# Round Robin HTTP Routing Service

## Overview

An implementation of an HTTP Round Robin load balancer that distributes requests evenly across multiple Echo Service instances (running independently on different ports).

Each request is forwarded to the next available Echo instance in rotation, and the response from the Echo Service is returned to the client.

---
## Features
- **Round Robin Request Distribution**: Evenly distributes HTTP POST requests across configured backend instances
- **Fault Tolerance**: Automatically retries with next available instance if one fails
- **Easy Configuration**: Backend instances configured via yml file

---
## **Technologies**
- **Java 21**
- **Spring Boot 3.x**
- **Maven** (dependency management)

---
## **Setup**
### Prerequisites
- Java 21 JDK
- Maven 3.8+

### Installation
1. Clone the repository:
```bash
   git clone https://github.com/genezeiniss/round-robin-service.git
   cd round-robin-service
```
2. Build the project:
```bash
    mvn clean install
```
---
## 🔧 Configuration

The list of Echo Service URLs must be configured via application.yml.

---
## API Endpoint

**Method**: `POST /api/round-robin/echo`
- Accepts: Any JSON payload
- Returns: The same JSON payload from the selected backend instance