package com.influencer.trainer_workload.repository;

import com.influencer.trainer_workload.model.TrainerMonthlyWorkload;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrainerWorkloadRepository {

    private final Map<String, TrainerMonthlyWorkload> workloadDatabase = new HashMap<>();

    public void save(TrainerMonthlyWorkload workload) {
        workloadDatabase.put(workload.getTrainerUsername(), workload);
    }

    public Optional<TrainerMonthlyWorkload> findByUsername(String username) {
        return Optional.ofNullable(workloadDatabase.get(username));
    }

    public Map<String, TrainerMonthlyWorkload> findAll() {
        return new HashMap<>(workloadDatabase);
    }

    public boolean existsByUsername(String username) {
        return workloadDatabase.containsKey(username);
    }

    public void deleteByUsername(String username) {
        workloadDatabase.remove(username);
    }
}

