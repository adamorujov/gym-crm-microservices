package com.influencer.trainer_workload.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainerMonthlyWorkload {

    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Boolean isActive;
    private Map<Integer, Map<Integer, Integer>> yearMonthlyHours; // year -> (month -> hours)

    public TrainerMonthlyWorkload() {
        this.yearMonthlyHours = new HashMap<>();
    }

    public TrainerMonthlyWorkload(String username, String firstName, String lastName, Boolean isActive) {
        this.trainerUsername = username;
        this.trainerFirstName = firstName;
        this.trainerLastName = lastName;
        this.isActive = isActive;
        this.yearMonthlyHours = new HashMap<>();
    }

    public double getTotalHours() {
        return yearMonthlyHours.values().stream()
                .flatMap(monthMap -> monthMap.values().stream())
                .mapToInt(Integer::intValue)
                .sum();
    }

    public int getHoursForMonth(int year, int month) {
        return yearMonthlyHours
                .getOrDefault(year, new HashMap<>())
                .getOrDefault(month, 0);
    }

    public void addHours(int year, int month, int hours) {
        yearMonthlyHours
                .computeIfAbsent(year, k -> new HashMap<>())
                .merge(month, hours, Integer::sum);
    }

    public void removeHours(int year, int month, int hours) {
        int current = getHoursForMonth(year, month);
        if (current > 0) {
            int updated = Math.max(0, current - hours);
            yearMonthlyHours
                    .computeIfAbsent(year, k -> new HashMap<>())
                    .put(month, updated);
        }
    }

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public TrainerMonthlyWorkload setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
        return this;
    }

    public String getTrainerFirstName() {
        return trainerFirstName;
    }

    public TrainerMonthlyWorkload setTrainerFirstName(String trainerFirstName) {
        this.trainerFirstName = trainerFirstName;
        return this;
    }

    public String getTrainerLastName() {
        return trainerLastName;
    }

    public TrainerMonthlyWorkload setTrainerLastName(String trainerLastName) {
        this.trainerLastName = trainerLastName;
        return this;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public TrainerMonthlyWorkload setIsActive(Boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    public Map<Integer, Map<Integer, Integer>> getYearMonthlyHours() {
        return yearMonthlyHours;
    }

    public TrainerMonthlyWorkload setYearMonthlyHours(Map<Integer, Map<Integer, Integer>> yearMonthlyHours) {
        this.yearMonthlyHours = yearMonthlyHours;
        return this;
    }
}

