package com.influencer.trainer_workload.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document representing a trainer's training summary.
 * Indexed by (trainerFirstName, trainerLastName) for name-based searches.
 */
@Document(collection = "trainer_training_summary")
@CompoundIndex(name = "idx_first_last_name", def = "{'trainerFirstName': 1, 'trainerLastName': 1}")
public class TrainerTrainingSummary {

    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "Trainer username is required")
    private String trainerUsername;

    @NotBlank(message = "Trainer first name is required")
    private String trainerFirstName;

    @NotBlank(message = "Trainer last name is required")
    private String trainerLastName;

    @NotNull(message = "Trainer status is required")
    private Boolean trainerStatus;

    private List<YearSummary> years = new ArrayList<>();

    public TrainerTrainingSummary() {
    }

    public TrainerTrainingSummary(String trainerUsername, String trainerFirstName,
                                   String trainerLastName, Boolean trainerStatus) {
        this.trainerUsername = trainerUsername;
        this.trainerFirstName = trainerFirstName;
        this.trainerLastName = trainerLastName;
        this.trainerStatus = trainerStatus;
        this.years = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public TrainerTrainingSummary setId(String id) {
        this.id = id;
        return this;
    }

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public TrainerTrainingSummary setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
        return this;
    }

    public String getTrainerFirstName() {
        return trainerFirstName;
    }

    public TrainerTrainingSummary setTrainerFirstName(String trainerFirstName) {
        this.trainerFirstName = trainerFirstName;
        return this;
    }

    public String getTrainerLastName() {
        return trainerLastName;
    }

    public TrainerTrainingSummary setTrainerLastName(String trainerLastName) {
        this.trainerLastName = trainerLastName;
        return this;
    }

    public Boolean getTrainerStatus() {
        return trainerStatus;
    }

    public TrainerTrainingSummary setTrainerStatus(Boolean trainerStatus) {
        this.trainerStatus = trainerStatus;
        return this;
    }

    public List<YearSummary> getYears() {
        return years;
    }

    public TrainerTrainingSummary setYears(List<YearSummary> years) {
        this.years = years;
        return this;
    }

    // ─── Nested document: year entry ──────────────────────────────────────────

    public static class YearSummary {

        private Integer year;
        private List<MonthSummary> months = new ArrayList<>();

        public YearSummary() {
        }

        public YearSummary(Integer year) {
            this.year = year;
            this.months = new ArrayList<>();
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

    // ─── Nested document: month entry ─────────────────────────────────────────

    public static class MonthSummary {

        private Integer month;
        /** Training summary duration in minutes (numeric type). */
        private Integer trainingsSummaryDuration;

        public MonthSummary() {
        }

        public MonthSummary(Integer month, Integer trainingsSummaryDuration) {
            this.month = month;
            this.trainingsSummaryDuration = trainingsSummaryDuration;
        }

        public Integer getMonth() {
            return month;
        }

        public MonthSummary setMonth(Integer month) {
            this.month = month;
            return this;
        }

        public Integer getTrainingsSummaryDuration() {
            return trainingsSummaryDuration;
        }

        public MonthSummary setTrainingsSummaryDuration(Integer trainingsSummaryDuration) {
            this.trainingsSummaryDuration = trainingsSummaryDuration;
            return this;
        }
    }
}
