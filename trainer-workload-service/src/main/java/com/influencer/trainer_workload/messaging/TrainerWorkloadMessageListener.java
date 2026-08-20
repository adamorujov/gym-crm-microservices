package com.influencer.trainer_workload.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.influencer.trainer_workload.dto.request.InvalidTrainerWorkloadMessage;
import com.influencer.trainer_workload.dto.request.TrainerWorkloadRequest;
import com.influencer.trainer_workload.service.TrainerWorkloadService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class TrainerWorkloadMessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerWorkloadMessageListener.class);

    private final ObjectMapper objectMapper;
    private final TrainerWorkloadService trainerWorkloadService;
    private final Validator validator;
    private final JmsTemplate jmsTemplate;
    private final String invalidQueue;

    public TrainerWorkloadMessageListener(
            ObjectMapper objectMapper,
            TrainerWorkloadService trainerWorkloadService,
            Validator validator,
            JmsTemplate jmsTemplate,
            @Value("${app.messaging.trainer-workload.invalid-queue:trainer.workload.invalid.dlq}") String invalidQueue
    ) {
        this.objectMapper = objectMapper;
        this.trainerWorkloadService = trainerWorkloadService;
        this.validator = validator;
        this.jmsTemplate = jmsTemplate;
        this.invalidQueue = invalidQueue;
    }

    @JmsListener(destination = "${app.messaging.trainer-workload.queue:trainer.workload.queue}")
    public void consumeWorkloadEvent(String payload, @Header(name = "transactionId", required = false) String transactionId) {
        String effectiveTransactionId =
                (transactionId == null || transactionId.isBlank()) ? UUID.randomUUID().toString() : transactionId;
        MDC.put("transactionId", effectiveTransactionId);

        try {
            TrainerWorkloadRequest request = objectMapper.readValue(payload, TrainerWorkloadRequest.class);
            Set<ConstraintViolation<TrainerWorkloadRequest>> violations = validator.validate(request);

            if (!violations.isEmpty()) {
                String reason = violations.stream()
                        .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                        .collect(Collectors.joining("; "));
                sendToInvalidQueue(payload, reason, effectiveTransactionId);
                LOGGER.warn("[{}] Invalid trainer workload event sent to DLQ: {}", effectiveTransactionId, reason);
                return;
            }

            trainerWorkloadService.processTrainerWorkload(request);
            LOGGER.debug("[{}] Trainer workload event processed", effectiveTransactionId);
        } catch (JsonProcessingException ex) {
            sendToInvalidQueue(payload, "Failed to parse message payload", effectiveTransactionId);
            LOGGER.warn("[{}] Unparseable workload event sent to DLQ", effectiveTransactionId);
        } finally {
            MDC.remove("transactionId");
        }
    }

    private void sendToInvalidQueue(String payload, String reason, String transactionId) {
        InvalidTrainerWorkloadMessage invalidMessage = new InvalidTrainerWorkloadMessage()
                .setTransactionId(transactionId)
                .setReason(reason)
                .setPayload(payload)
                .setReceivedAt(Instant.now());

        try {
            String invalidPayload = objectMapper.writeValueAsString(invalidMessage);
            jmsTemplate.convertAndSend(invalidQueue, invalidPayload);
        } catch (JsonProcessingException ex) {
            LOGGER.error("[{}] Failed to serialize invalid workload event for DLQ", transactionId, ex);
        }
    }
}

