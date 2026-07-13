package com.influencer.trainer_workload.service.impl;

import com.influencer.trainer_workload.dto.request.TrainerWorkloadRequest;
import com.influencer.trainer_workload.dto.response.TrainerWorkloadDetailsResponse;
import com.influencer.trainer_workload.model.TrainerMonthlyWorkload;
import com.influencer.trainer_workload.repository.TrainerWorkloadRepository;
import com.influencer.trainer_workload.service.TrainerWorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerWorkloadServiceImpl.class);

    private final TrainerWorkloadRepository workloadRepository;

    public TrainerWorkloadServiceImpl(TrainerWorkloadRepository workloadRepository) {
        this.workloadRepository = workloadRepository;
    }

    @Override
    public void processTrainerWorkload(TrainerWorkloadRequest request) {
        String transactionId = MDC.get("transactionId");
        LOGGER.info("[{}] Processing trainer workload: username='{}' action='{}' duration={}",
                transactionId, request.getTrainerUsername(), request.getActionType(), request.getTrainingDuration());

        TrainerMonthlyWorkload workload = workloadRepository.findByUsername(request.getTrainerUsername())
                .orElseGet(() -> {
                    LOGGER.debug("[{}] Creating new workload record for trainer: {}", transactionId, request.getTrainerUsername());
                    return new TrainerMonthlyWorkload(
                            request.getTrainerUsername(),
                            request.getTrainerFirstName(),
                            request.getTrainerLastName(),
                            request.getIsActive()
                    );
                });

        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();

        if ("ADD".equalsIgnoreCase(request.getActionType())) {
            workload.addHours(year, month, request.getTrainingDuration());
            LOGGER.info("[{}] Added {} hours to trainer {} for {}-{}", transactionId,
                    request.getTrainingDuration(), request.getTrainerUsername(), year, month);
        } else if ("DELETE".equalsIgnoreCase(request.getActionType())) {
            workload.removeHours(year, month, request.getTrainingDuration());
            LOGGER.info("[{}] Removed {} hours from trainer {} for {}-{}", transactionId,
                    request.getTrainingDuration(), request.getTrainerUsername(), year, month);
        }

        workload.setIsActive(request.getIsActive());
        workloadRepository.save(workload);
        LOGGER.debug("[{}] Workload updated successfully for trainer: {}", transactionId, request.getTrainerUsername());
    }

    @Override
    public TrainerWorkloadDetailsResponse getTrainerWorkload(String trainerUsername) {
        String transactionId = MDC.get("transactionId");
        LOGGER.info("[{}] Fetching trainer workload for: {}", transactionId, trainerUsername);

        TrainerMonthlyWorkload workload = workloadRepository.findByUsername(trainerUsername)
                .orElseGet(() -> {
                    LOGGER.warn("[{}] No workload found for trainer: {}", transactionId, trainerUsername);
                    return new TrainerMonthlyWorkload(trainerUsername, "N/A", "N/A", false);
                });

        TrainerWorkloadDetailsResponse response = new TrainerWorkloadDetailsResponse(
                workload.getTrainerUsername(),
                workload.getTrainerFirstName(),
                workload.getTrainerLastName(),
                workload.getIsActive()
        );

        List<TrainerWorkloadDetailsResponse.YearSummary> years = workload.getYearMonthlyHours().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(yearEntry -> {
                    TrainerWorkloadDetailsResponse.YearSummary year = new TrainerWorkloadDetailsResponse.YearSummary(yearEntry.getKey());
                    List<TrainerWorkloadDetailsResponse.MonthSummary> months = yearEntry.getValue().entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .map(monthEntry -> new TrainerWorkloadDetailsResponse.MonthSummary(
                                    monthEntry.getKey(),
                                    monthEntry.getValue()
                            ))
                            .collect(Collectors.toList());
                    year.setMonths(months);
                    return year;
                })
                .collect(Collectors.toList());

        response.setYears(years);
        LOGGER.debug("[{}] Workload retrieved for trainer: {} with {} years of data", transactionId, trainerUsername, years.size());
        return response;
    }
}

