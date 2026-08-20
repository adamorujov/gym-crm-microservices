package com.influencer.trainer_workload.controller;

import com.influencer.trainer_workload.dto.request.TrainerWorkloadRequest;
import com.influencer.trainer_workload.dto.response.TrainerWorkloadDetailsResponse;
import com.influencer.trainer_workload.dto.response.TrainerWorkloadResponse;
import com.influencer.trainer_workload.service.TrainerWorkloadService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainer-workload")
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

    @GetMapping("/trainer/{username}")
    public ResponseEntity<TrainerWorkloadDetailsResponse> getTrainerWorkload(
            @PathVariable String username) {

        String transactionId = MDC.get("transactionId");
        LOGGER.info("[{}] GET /api/trainer-workload/trainer/{}  - Request received", transactionId, username);

        TrainerWorkloadDetailsResponse response = trainerWorkloadService.getTrainerWorkload(username);
        LOGGER.info("[{}] GET /api/trainer-workload/trainer/{} - Response 200", transactionId, username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<TrainerWorkloadResponse> health() {
        String transactionId = MDC.get("transactionId");
        LOGGER.debug("[{}] GET /api/trainer-workload/health", transactionId);
        return ResponseEntity.ok(new TrainerWorkloadResponse("OK", "Trainer Workload Service is running"));
    }
}

