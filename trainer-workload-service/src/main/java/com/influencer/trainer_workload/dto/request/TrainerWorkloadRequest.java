package com.influencer.trainer_workload.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class TrainerWorkloadRequest {

    @NotBlank(message = "Trainer username is required")
    private String trainerUsername;

    @NotBlank(message = "Trainer first name is required")
    private String trainerFirstName;

    @NotBlank(message = "Trainer last name is required")
    private String trainerLastName;

    @NotNull(message = "IsActive is required")
    private Boolean isActive;

    @NotNull(message = "Training date is required")
    private LocalDate trainingDate;

    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be greater than zero")
    private Integer trainingDuration;

    @NotBlank(message = "Action type is required (ADD or DELETE)")
    private String actionType; // ADD or DELETE

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public TrainerWorkloadRequest setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
        return this;
    }

    public String getTrainerFirstName() {
        return trainerFirstName;
    }

    public TrainerWorkloadRequest setTrainerFirstName(String trainerFirstName) {
        this.trainerFirstName = trainerFirstName;
        return this;
    }

    public String getTrainerLastName() {
        return trainerLastName;
    }

    public TrainerWorkloadRequest setTrainerLastName(String trainerLastName) {
        this.trainerLastName = trainerLastName;
        return this;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public TrainerWorkloadRequest setIsActive(Boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public TrainerWorkloadRequest setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
        return this;
    }

    public Integer getTrainingDuration() {
        return trainingDuration;
    }

    public TrainerWorkloadRequest setTrainingDuration(Integer trainingDuration) {
        this.trainingDuration = trainingDuration;
        return this;
    }

    public String getActionType() {
        return actionType;
    }

    public TrainerWorkloadRequest setActionType(String actionType) {
        this.actionType = actionType;
        return this;
    }
}

