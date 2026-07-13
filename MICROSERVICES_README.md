# Gym CRM System - Microservices Implementation

## Overview
This project implements a microservices architecture for the Gym CRM System with a dedicated trainer workload tracking microservice.

## Architecture Components

### 1. **Main Service (gym-crm-system)**
- Port: `8080`
- Handles user registration, authentication, profile management, and training sessions
- Communicates with trainer-workload-service via Feign client
- Implements circuit breaker pattern for resilience

### 2. **Trainer Workload Microservice (trainer-workload-service)**
- Port: `8081`
- Tracks monthly trainer workload hours
- In-memory database (HashMap-based) for fast access
- RESTful API for processing training workload

### 3. **Eureka Service Discovery (eureka-server)**
- Port: `8761`
- Service registry for dynamic service discovery
- Enables inter-service communication without hardcoded URLs

## Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL or H2 (for local testing)

### Building the Services

```bash
# Build Eureka Server
cd eureka-server
mvn clean package

# Build Trainer Workload Service
cd ../trainer-workload-service
mvn clean package

# Build Main Service
cd ../gym_crm_system
mvn clean package
```

### Running the Services

**Recommended order:**

1. **Start Eureka Server**
```bash
cd eureka-server
mvn spring-boot:run
# or
java -jar target/eureka-server-1.0-SNAPSHOT.jar
```

2. **Start Trainer Workload Microservice**
```bash
cd trainer-workload-service
mvn spring-boot:run
# or
java -jar target/trainer-workload-service-1.0-SNAPSHOT.jar
```

3. **Start Main Service**
```bash
cd gym_crm_system
mvn spring-boot:run
# or
java -jar target/gym_crm_system-1.0-SNAPSHOT.jar
```

### Verification

Check service registration:
- Eureka Dashboard: http://localhost:8761/

Health check endpoints:
- Main Service: http://localhost:8080/actuator/health
- Trainer Workload: http://localhost:8081/api/trainer-workload/health

## API Endpoints

### Main Service (gym-crm-system) - Port 8080

#### Authentication
- `POST /api/login` - Login with username/password (returns JWT token)
- `POST /api/logout` - Logout (invalidates JWT token)

#### Trainer Management
- `POST /api/trainers` - Register new trainer
- `GET /api/trainers/{username}` - Get trainer profile
- `PUT /api/trainers/{username}` - Update trainer profile
- `PATCH /api/trainers/{username}/activate` - Activate trainer
- `PATCH /api/trainers/{username}/deactivate` - Deactivate trainer

#### Training Management
- `POST /api/trainings` - Add new training session
  - This endpoint automatically calls the trainer-workload-service
  - Request payload includes:
    ```json
    {
      "trainingName": "Morning Yoga",
      "traineeUsername": "john.doe",
      "trainerUsername": "jane.smith",
      "trainingDate": "2024-07-05",
      "trainingDuration": 60
    }
    ```

- `GET /api/trainees/{username}/trainings` - Get trainee trainings with optional filters
  - Query params: `periodFrom`, `periodTo`, `trainerName`, `trainingType`

- `GET /api/trainers/{username}/trainings` - Get trainer trainings with optional filters
  - Query params: `periodFrom`, `periodTo`, `traineeName`

### Trainer Workload Microservice - Port 8081

#### Workload Processing
- `POST /api/trainer-workload/process` - Process trainer workload (ADD/DELETE)
  - Request payload:
    ```json
    {
      "trainerUsername": "jane.smith",
      "trainerFirstName": "Jane",
      "trainerLastName": "Smith",
      "isActive": true,
      "trainingDate": "2024-07-05",
      "trainingDuration": 60,
      "actionType": "ADD"
    }
    ```

#### Workload Retrieval
- `GET /api/trainer-workload/trainer/{username}` - Get trainer monthly workload summary
  - Response structure:
    ```json
    {
      "trainerUsername": "jane.smith",
      "trainerFirstName": "Jane",
      "trainerLastName": "Smith",
      "isActive": true,
      "years": [
        {
          "year": 2024,
          "months": [
            {
              "month": 7,
              "trainingHours": 120
            }
          ]
        }
      ]
    }
    ```

#### Health Check
- `GET /api/trainer-workload/health` - Service health check

## Key Features

### 1. **In-Memory Database**
- Trainer workload data stored in HashMap
- Fast access without database latency
- Data is lost on service restart (suitable for current requirements)

### 2. **Service Discovery**
- Eureka automatically registers services
- Main service discovers trainer-workload-service dynamically
- No hardcoded service URLs needed

### 3. **Circuit Breaker Pattern**
- Resilience4j circuit breaker for trainer-workload-service calls
- Prevents cascading failures
- Fallback method returns error response if service is unavailable
- Main service continues to function even if trainer-workload-service is down

### 4. **Transaction Logging**
- **Transaction ID (transactionId)** generated per request
- MDC (Mapped Diagnostic Context) propagates transactionId through logs
- Two-level logging:
  1. **Transaction Level**: Endpoint called, request/response, status code, duration
  2. **Operation Level**: Detailed logs within each service

### 5. **Security**
- JWT-based authentication
- Stateless API communication
- Authorization headers for inter-service calls

## Logging Output

Logs include transactionId for request tracing:

```
2024-07-05 10:30:45 [1234-5678-90ab-cdef] POST /api/trainings - Request received
2024-07-05 10:30:45 [1234-5678-90ab-cdef] Adding training name='Morning Yoga'...
2024-07-05 10:30:46 [1234-5678-90ab-cdef] Calling trainer-workload-service
2024-07-05 10:30:46 [1234-5678-90ab-cdef] Trainer workload service processed successfully
2024-07-05 10:30:46 [1234-5678-90ab-cdef] POST /api/trainings - Response 200
```

## Configuration Files

### Main Service (application.yml)
- Eureka client configured at: `http://localhost:8761/eureka/`
- Resilience4j circuit breaker for `trainerWorkloadService`
- JWT secret and expiration time
- Brute-force protection settings

### Trainer Workload Service (application.yml)
- Eureka client configured at: `http://localhost:8761/eureka/`
- Service port: 8081
- JWT secret for request validation

### Eureka Server (application.yml)
- Server port: 8761
- Not registered with itself
- Configured as discovery server

## Training Deletion Scenario

**When can training be deleted?**
1. Only the authorized trainer or admin can delete training
2. Upon deletion, the trainer workload service is called with `actionType: DELETE`
3. The microservice decrements the trainer's monthly hours
4. Training record is removed from the database

**Implementation Note**: Training deletion logic should be implemented in `TrainingServiceImpl` following the same pattern as `addTraining`:
- Fetch training from repository
- Call microservice with DELETE action
- Delete from database

## Monitoring & Metrics

Access metrics and health indicators:
- Prometheus: http://localhost:8080/actuator/prometheus
- Health: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics

## Troubleshooting

### Service Not Registered in Eureka
- Ensure Eureka server is running on port 8761
- Check application.yml eureka.client.serviceUrl.defaultZone

### Circuit Breaker Open
- Trainer-workload-service may be down
- Check logs for circuit breaker state changes
- Service continues with fallback response

### TransactionId Not Propagating
- Verify MDC initialization in @ModelAttribute methods
- Check filter/interceptor configuration
- Ensure MDC.put() is called before service calls

## Future Enhancements

1. **Persistent Storage for Workload**
   - Replace HashMap with Redis or database
   - Data survives service restarts

2. **Advanced Monitoring**
   - Distributed tracing with Spring Cloud Sleuth  
   - Zipkin integration for trace visualization

3. **API Gateway**
   - Spring Cloud Gateway for unified API
   - Centralized authentication

4. **Message Queue Integration**
   - Eventually consistent training deletion
   - Event-driven architecture

5. **Caching Layer**
   - Redis caching for workload queries
   - Reduced database/memory access

