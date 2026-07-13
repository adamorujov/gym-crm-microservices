package com.influencer.trainer_workload.dto.response;

import java.util.List;

public class TrainerWorkloadDetailsResponse {

    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Boolean isActive;
    private List<YearSummary> years;

    public TrainerWorkloadDetailsResponse() {
    }

    public TrainerWorkloadDetailsResponse(String trainerUsername, String trainerFirstName, String trainerLastName, Boolean isActive) {
        this.trainerUsername = trainerUsername;
        this.trainerFirstName = trainerFirstName;
        this.trainerLastName = trainerLastName;
        this.isActive = isActive;
    }

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public TrainerWorkloadDetailsResponse setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
        return this;
    }

    public String getTrainerFirstName() {
        return trainerFirstName;
    }

    public TrainerWorkloadDetailsResponse setTrainerFirstName(String trainerFirstName) {
        this.trainerFirstName = trainerFirstName;
        return this;
    }

    public String getTrainerLastName() {
        return trainerLastName;
    }

    public TrainerWorkloadDetailsResponse setTrainerLastName(String trainerLastName) {
        this.trainerLastName = trainerLastName;
        return this;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public TrainerWorkloadDetailsResponse setIsActive(Boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    public List<YearSummary> getYears() {
        return years;
    }

    public TrainerWorkloadDetailsResponse setYears(List<YearSummary> years) {
        this.years = years;
        return this;
    }

    public static class YearSummary {
        private Integer year;
        private List<MonthSummary> months;

        public YearSummary() {
        }

        public YearSummary(Integer year) {
            this.year = year;
        }

        public Integer getYear() {
            return year;
        }

        public YearSummary setYear(Integer year) {
            this.year = year;
            return this;
        }

        public List<MonthSummary> getMonths() {
            return months;
        }

        public YearSummary setMonths(List<MonthSummary> months) {
            this.months = months;
            return this;
        }
    }

    public static class MonthSummary {
        private Integer month;
        private Integer trainingHours;

        public MonthSummary() {
        }

        public MonthSummary(Integer month, Integer trainingHours) {
            this.month = month;
            this.trainingHours = trainingHours;
        }

        public Integer getMonth() {
            return month;
        }

        public MonthSummary setMonth(Integer month) {
            this.month = month;
            return this;
        }

        public Integer getTrainingHours() {
            return trainingHours;
        }

        public MonthSummary setTrainingHours(Integer trainingHours) {
            this.trainingHours = trainingHours;
            return this;
        }
    }
}

