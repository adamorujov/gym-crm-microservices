package com.influencer.trainer_workload.repository;

import com.influencer.trainer_workload.model.TrainerTrainingSummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerWorkloadRepository extends MongoRepository<TrainerTrainingSummary, String> {

    Optional<TrainerTrainingSummary> findByTrainerUsername(String trainerUsername);

    boolean existsByTrainerUsername(String trainerUsername);
}

