package com.influencer.trainer_workload.controller;

import com.influencer.trainer_workload.dto.request.TrainerWorkloadRequest;
import com.influencer.trainer_workload.dto.response.TrainerWorkloadDetailsResponse;
import com.influencer.trainer_workload.dto.response.TrainerWorkloadResponse;
import com.influencer.trainer_workload.service.TrainerWorkloadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.Valid;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainer-workload")
@Api(tags = "Trainer Workload API")
public class TrainerWorkloadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerWorkloadController.class);

    private final TrainerWorkloadService trainerWorkloadService;

    public TrainerWorkloadController(TrainerWorkloadService trainerWorkloadService) {
        this.trainerWorkloadService = trainerWorkloadService;
    }

    @ModelAttribute
    public void initTransactionId() {
        MDC.put("transactionId", UUID.randomUUID().toString());
    }

    @ApiOperation(value = "Process trainer workload (ADD or DELETE training)")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Workload processed successfully"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 401, message = "Unauthorized")
    })
    @PostMapping("/process")
    public ResponseEntity<TrainerWorkloadResponse> processTrainerWorkload(
            @Valid @RequestBody TrainerWorkloadRequest request) {

        String transactionId = MDC.get("transactionId");
        LOGGER.info("[{}] POST /api/trainer-workload/process - Request received: action='{}' trainer='{}'",
                transactionId, request.getActionType(), request.getTrainerUsername());

        try {
            trainerWorkloadService.processTrainerWorkload(request);
            TrainerWorkloadResponse response = new TrainerWorkloadResponse("OK", "Workload processed successfully");
            LOGGER.info("[{}] POST /api/trainer-workload/process - Response 200: {}", transactionId, response.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            LOGGER.error("[{}] POST /api/trainer-workload/process - Error: {}", transactionId, ex.getMessage(), ex);
            return ResponseEntity.badRequest()
                    .body(new TrainerWorkloadResponse("ERROR", ex.getMessage()));
        }
    }

    @ApiOperation(value = "Get trainer workload summary")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Workload retrieved successfully"),
            @ApiResponse(code = 404, message = "Trainer not found")
    })
    @GetMapping("/trainer/{username}")
    public ResponseEntity<TrainerWorkloadDetailsResponse> getTrainerWorkload(
            @PathVariable String username) {

        String transactionId = MDC.get("transactionId");
        LOGGER.info("[{}] GET /api/trainer-workload/trainer/{}  - Request received", transactionId, username);

        TrainerWorkloadDetailsResponse response = trainerWorkloadService.getTrainerWorkload(username);
        LOGGER.info("[{}] GET /api/trainer-workload/trainer/{} - Response 200", transactionId, username);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Health check endpoint")
    @GetMapping("/health")
    public ResponseEntity<TrainerWorkloadResponse> health() {
        String transactionId = MDC.get("transactionId");
        LOGGER.debug("[{}] GET /api/trainer-workload/health", transactionId);
        return ResponseEntity.ok(new TrainerWorkloadResponse("OK", "Trainer Workload Service is running"));
    }
}

