package com.influencer.trainer_workload.dto.response;

public class TrainerWorkloadResponse {

    private String status;
    private String message;

    public TrainerWorkloadResponse() {
    }

    public TrainerWorkloadResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public TrainerWorkloadResponse setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public TrainerWorkloadResponse setMessage(String message) {
        this.message = message;
        return this;
    }
}

