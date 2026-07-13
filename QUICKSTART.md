# Quick Start Guide - Microservices Setup

## One-Minute Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- 3 terminal windows open

### Build All Services

```bash
# From the root directory (epam-spring-tasks)

# Build Eureka
cd eureka-server && mvn clean package && cd ..

# Build Trainer Workload Service
cd trainer-workload-service && mvn clean package && cd ..

# Build Main Service
cd gym_crm_system && mvn clean package && cd ..
```

### Run Services (in separate terminals)

**Terminal 1 - Eureka Server**
```bash
cd eureka-server
java -jar target/eureka-server-1.0-SNAPSHOT.jar
# Or: mvn spring-boot:run
```

Expected output:
```
o.s.c.n.e.s.EurekaServerInitializerConfiguration : Started Eureka Server
```

**Terminal 2 - Trainer Workload Microservice**
```bash
cd trainer-workload-service
java -jar target/trainer-workload-service-1.0-SNAPSHOT.jar
# Or: mvn spring-boot:run
```

Expected output:
```
o.s.c.n.registry.InstanceRegistry : Registered instance
```

**Terminal 3 - Main CRM Service**
```bash
cd gym_crm_system
java -jar target/gym_crm_system-1.0-SNAPSHOT.jar
# Or: mvn spring-boot:run
```

Expected output:
```
o.s.c.n.registry.InstanceRegistry : Registered instance
GymController initialization successful
```

---

## Verify Services Are Running

### Service Discovery Status
```bash
# Check Eureka dashboard
curl http://localhost:8761/
# Or open browser: http://localhost:8761/
```

### Health Checks
```bash
# Main Service
curl http://localhost:8080/actuator/health

# Trainer Workload Service
curl http://localhost:8081/api/trainer-workload/health
```

---

## Test Workflow

### 1. Register Trainer
```bash
curl -X POST http://localhost:8080/api/trainers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "specialization": "Yoga"
  }'

# Response:
# {
#   "username": "john.doe",
#   "password": "GeneratedPassword123"
# }
```

### 2. Register Trainee
```bash
curl -X POST http://localhost:8080/api/trainees \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "dateOfBirth": "2000-01-15",
    "address": "123 Main St"
  }'

# Response:
# {
#   "username": "jane.smith",
#   "password": "GeneratedPassword456"
# }
```

### 3. Login (Get JWT Token)
```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "password": "GeneratedPassword123"
  }'

# Response:
# {
#   "type": "Bearer",
#   "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZSIsImlhdCI6MTcyMDI1Njg0MCwiZXhwIjoxNzIwMjYwNDQwfQ.X1Yx..."
# }

# Save token for next request
export TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZSIsImlhdCI6MTcyMDI1Njg0MCwiZXhwIjoxNzIwMjYwNDQwfQ.X1Yx..."
```

### 4. Add Training (Triggers Microservice Call)
```bash
curl -X POST http://localhost:8080/api/trainings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "trainingName": "Morning Yoga Class",
    "traineeUsername": "jane.smith",
    "trainerUsername": "john.doe",
    "trainingDate": "2024-07-05",
    "trainingDuration": 60
  }'

# This automatically calls the trainer-workload-service
# You should see in the logs the transactionId propagated through both services
```

### 5. Check Trainer Workload (from Microservice)
```bash
curl http://localhost:8081/api/trainer-workload/trainer/john.doe

# Response:
# {
#   "trainerUsername": "john.doe",
#   "trainerFirstName": "John",
#   "trainerLastName": "Doe",
#   "isActive": true,
#   "years": [
#     {
#       "year": 2024,
#       "months": [
#         {
#           "month": 7,
#           "trainingHours": 60
#         }
#       ]
#     }
#   ]
# }
```

### 6. Check Logs with TransactionId

In main service (gym_crm_system):
```bash
# Look for logs with transactionId pattern
grep -i "transactionId" logs/spring.log | tail -20
```

Example log output:
```
2024-07-05 10:30:45 [7f8a-9b1c-2d3e-4f5g] POST /api/trainings - Request received
2024-07-05 10:30:45 [7f8a-9b1c-2d3e-4f5g] Adding training name='Morning Yoga Class'
2024-07-05 10:30:45 [7f8a-9b1c-2d3e-4f5g] Calling trainer-workload-service with action=ADD
2024-07-05 10:30:46 [7f8a-9b1c-2d3e-4f5g] Trainer workload service processed successfully
2024-07-05 10:30:46 [7f8a-9b1c-2d3e-4f5g] POST /api/trainings - Response 200
```

---

## Common Commands

### Stop a Service
Press `Ctrl+C` in the terminal where it's running

### View Real-Time Logs
```bash
# Main Service
tail -f gym_crm_system/logs/spring.log

# Trainer Workload Service
tail -f trainer-workload-service/logs/spring.log
```

### Check Service Discovery
```bash
# View registered instances
curl http://localhost:8761/eureka/apps/
```

### Monitor Metrics
```bash
# Prometheus metrics from main service
curl http://localhost:8080/actuator/prometheus | grep -i "workload\|circuit"
```

### Clear Previous Builds
```bash
# Clean all services
mvn clean -DskipTests=true --projects gym_crm_system,trainer-workload-service,eureka-server
```

---

## Troubleshooting

### "Connection refused" when adding training
**Cause**: Trainer Workload Service not running
**Solution**: Start trainer-workload-service on port 8081

### "Cannot find service instance" error
**Cause**: Eureka server not running
**Solution**: Start eureka-server on port 8761 first

### Circuit breaker open - "Service unavailable"
**Cause**: Trainer Workload Service crashed
**Solution**: Restart trainer-workload-service; circuit breaker will auto-recover

### TransactionId not showing in logs
**Cause**: MDC not initialized
**Solution**: Ensure @ModelAttribute initializes MDC in controller

### Port already in use
**Solution**:
```bash
# Unix/Linux/Mac
lsof -i :8080  # Find process on port 8080
kill -9 <PID>

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

---

## Architecture Verification Checklist

- [ ] Eureka server running on port 8761
- [ ] Trainer Workload Service running on port 8081
- [ ] Main CRM Service running on port 8080
- [ ] Both services registered in Eureka
- [ ] Can login and get JWT token
- [ ] Can add training
- [ ] Automatic call to microservice completes
- [ ] Workload data appears in microservice
- [ ] TransactionId appears in logs
- [ ] Circuit breaker working (if you stop microservice)

---

## Performance Notes

### Typical Response Times
- Registration: 50-100ms
- Login: 100-200ms
- Add Training (with microservice call): 200-400ms
- Get Workload: 50-80ms

### Circuit Breaker Behavior
- **Closed** (normal): Requests pass through
- **Open** (failures detected): Requests fail fast with fallback
- **Half-Open** (recovering): Tests with 3 calls before recovering

---

## Next Steps

1. **Explore the API** - Try different endpoints
2. **Monitor Logs** - Watch transactionId flow through services
3. **Break the System** - Stop a service and observe fallback behavior
4. **Add More Trainings** - Build workload history
5. **Implement Deletion** - Add DELETE training endpoint (see IMPLEMENTATION_SUMMARY.md)

---

## For More Information

- **Architecture Details**: See [MICROSERVICES_README.md](./MICROSERVICES_README.md)
- **Implementation Notes**: See [IMPLEMENTATION_SUMMARY.md](./IMPLEMENTATION_SUMMARY.md)
- **Code Documentation**: Check inline comments in service classes

