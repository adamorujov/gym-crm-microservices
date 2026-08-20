package com.influencer.trainer_workload.dto.request;

import java.time.Instant;

public class InvalidTrainerWorkloadMessage {

    private String transactionId;
    private String reason;
    private String payload;
    private Instant receivedAt;

    public String getTransactionId() {
        return transactionId;
    }

    public InvalidTrainerWorkloadMessage setTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public String getReason() {
        return reason;
    }

    public InvalidTrainerWorkloadMessage setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public String getPayload() {
        return payload;
    }

    public InvalidTrainerWorkloadMessage setPayload(String payload) {
        this.payload = payload;
        return this;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public InvalidTrainerWorkloadMessage setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
        return this;
    }
}

