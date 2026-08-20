package com.influencer.trainer_workload.service.impl;

import com.influencer.trainer_workload.dto.request.TrainerWorkloadRequest;
import com.influencer.trainer_workload.dto.response.TrainerWorkloadDetailsResponse;
import com.influencer.trainer_workload.model.TrainerTrainingSummary;
import com.influencer.trainer_workload.repository.TrainerWorkloadRepository;
import com.influencer.trainer_workload.service.TrainerWorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
                transactionId, request.getTrainerUsername(), request.getActionType(),
                request.getTrainingDuration());

        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();

        Optional<TrainerTrainingSummary> existing = workloadRepository.findByTrainerUsername(request.getTrainerUsername());

        TrainerTrainingSummary summary;

        if (existing.isEmpty()) {
            LOGGER.debug("[{}] No record found for trainer '{}' -- creating new document",
                    transactionId, request.getTrainerUsername());
            summary = new TrainerTrainingSummary(
                    request.getTrainerUsername(),
                    request.getTrainerFirstName(),
                    request.getTrainerLastName(),
                    request.getIsActive()
            );
            LOGGER.debug("[{}] Initialising year={} month={} with duration={}",
                    transactionId, year, month, request.getTrainingDuration());
            applyDurationChange(summary, year, month, request.getTrainingDuration(),
                    request.getActionType(), transactionId);
        } else {
            summary = existing.get();
            LOGGER.debug("[{}] Existing record found for trainer '{}' -- updating year={} month={}",
                    transactionId, request.getTrainerUsername(), year, month);
            summary.setTrainerStatus(request.getIsActive());
            applyDurationChange(summary, year, month, request.getTrainingDuration(),
                    request.getActionType(), transactionId);
        }

        workloadRepository.save(summary);
        LOGGER.info("[{}] Trainer '{}' workload persisted to MongoDB",
                transactionId, request.getTrainerUsername());
    }

    @Override
    public TrainerWorkloadDetailsResponse getTrainerWorkload(String trainerUsername) {
        String transactionId = MDC.get("transactionId");
        LOGGER.info("[{}] Fetching trainer workload for: '{}'", transactionId, trainerUsername);

        TrainerTrainingSummary summary = workloadRepository.findByTrainerUsername(trainerUsername)
                .orElseGet(() -> {
                    LOGGER.warn("[{}] No workload found for trainer: '{}'", transactionId, trainerUsername);
                    return new TrainerTrainingSummary(trainerUsername, "N/A", "N/A", false);
                });

        TrainerWorkloadDetailsResponse response = new TrainerWorkloadDetailsResponse(
                summary.getTrainerUsername(),
                summary.getTrainerFirstName(),
                summary.getTrainerLastName(),
                summary.getTrainerStatus()
        );

        List<TrainerWorkloadDetailsResponse.YearSummary> yearList = summary.getYears().stream()
                .sorted((a, b) -> Integer.compare(a.getYear(), b.getYear()))
                .map(yearEntry -> {
                    TrainerWorkloadDetailsResponse.YearSummary yr =
                            new TrainerWorkloadDetailsResponse.YearSummary(yearEntry.getYear());
                    List<TrainerWorkloadDetailsResponse.MonthSummary> monthList = yearEntry.getMonths().stream()
                            .sorted((a, b) -> Integer.compare(a.getMonth(), b.getMonth()))
                            .map(m -> new TrainerWorkloadDetailsResponse.MonthSummary(
                                    m.getMonth(), m.getTrainingsSummaryDuration()))
                            .collect(Collectors.toList());
                    yr.setMonths(monthList);
                    return yr;
                })
                .collect(Collectors.toList());

        response.setYears(yearList);
        LOGGER.debug("[{}] Workload retrieved for trainer '{}' -- {} year(s) of data",
                transactionId, trainerUsername, yearList.size());
        return response;
    }

    private void applyDurationChange(TrainerTrainingSummary summary,
                                      int year, int month, int duration,
                                      String actionType, String transactionId) {
        TrainerTrainingSummary.YearSummary yearEntry = findOrCreateYear(summary, year);
        TrainerTrainingSummary.MonthSummary monthEntry = findOrCreateMonth(yearEntry, month);

        int currentDuration = monthEntry.getTrainingsSummaryDuration();

        if ("ADD".equalsIgnoreCase(actionType)) {
            int updated = currentDuration + duration;
            monthEntry.setTrainingsSummaryDuration(updated);
            LOGGER.info("[{}] ADD {} -> trainer='{}' year={} month={} total={}",
                    transactionId, duration, summary.getTrainerUsername(), year, month, updated);
        } else if ("DELETE".equalsIgnoreCase(actionType)) {
            int updated = Math.max(0, currentDuration - duration);
            monthEntry.setTrainingsSummaryDuration(updated);
            LOGGER.info("[{}] DELETE {} -> trainer='{}' year={} month={} total={}",
                    transactionId, duration, summary.getTrainerUsername(), year, month, updated);
        } else {
            LOGGER.warn("[{}] Unknown action type '{}' -- skipping duration change", transactionId, actionType);
        }
    }

    private TrainerTrainingSummary.YearSummary findOrCreateYear(TrainerTrainingSummary summary, int year) {
        for (TrainerTrainingSummary.YearSummary ys : summary.getYears()) {
            if (ys.getYear() == year) {
                return ys;
            }
        }
        TrainerTrainingSummary.YearSummary newYear = new TrainerTrainingSummary.YearSummary(year);
        summary.getYears().add(newYear);
        return newYear;
    }

    private TrainerTrainingSummary.MonthSummary findOrCreateMonth(TrainerTrainingSummary.YearSummary yearEntry,
                                                                   int month) {
        for (TrainerTrainingSummary.MonthSummary ms : yearEntry.getMonths()) {
            if (ms.getMonth() == month) {
                return ms;
            }
        }
        TrainerTrainingSummary.MonthSummary newMonth = new TrainerTrainingSummary.MonthSummary(month, 0);
        yearEntry.getMonths().add(newMonth);
        return newMonth;
    }
}