package com.influencer.trainer_workload.service;

import com.influencer.trainer_workload.dto.request.TrainerWorkloadRequest;
import com.influencer.trainer_workload.dto.response.TrainerWorkloadDetailsResponse;

public interface TrainerWorkloadService {
    void processTrainerWorkload(TrainerWorkloadRequest request);
    TrainerWorkloadDetailsResponse getTrainerWorkload(String trainerUsername);
}

