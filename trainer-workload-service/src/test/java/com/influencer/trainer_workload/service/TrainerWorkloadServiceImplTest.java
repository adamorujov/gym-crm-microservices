package com.influencer.trainer_workload.service;

import com.influencer.trainer_workload.dto.request.TrainerWorkloadRequest;
import com.influencer.trainer_workload.dto.response.TrainerWorkloadDetailsResponse;
import com.influencer.trainer_workload.model.TrainerTrainingSummary;
import com.influencer.trainer_workload.repository.TrainerWorkloadRepository;
import com.influencer.trainer_workload.service.impl.TrainerWorkloadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceImplTest {

    @Mock
    private TrainerWorkloadRepository workloadRepository;

    @InjectMocks
    private TrainerWorkloadServiceImpl service;

    private static final String USERNAME = "john.doe";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 3, 15);

    @BeforeEach
    void setUp() {
        MDC.put("transactionId", "test-tx-001");
    }

    // ─── processTrainerWorkload ───────────────────────────────────────────────

    @Test
    void processTrainerWorkload_newTrainer_createsDocumentWithDuration() {
        when(workloadRepository.findByTrainerUsername(USERNAME)).thenReturn(Optional.empty());
        when(workloadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TrainerWorkloadRequest request = buildRequest("ADD", 60);
        service.processTrainerWorkload(request);

        ArgumentCaptor<TrainerTrainingSummary> captor = ArgumentCaptor.forClass(TrainerTrainingSummary.class);
        verify(workloadRepository).save(captor.capture());

        TrainerTrainingSummary saved = captor.getValue();
        assertThat(saved.getTrainerUsername()).isEqualTo(USERNAME);
        assertThat(saved.getTrainerFirstName()).isEqualTo(FIRST_NAME);
        assertThat(saved.getTrainerLastName()).isEqualTo(LAST_NAME);
        assertThat(saved.getTrainerStatus()).isTrue();

        int duration = getMonthDuration(saved, 2024, 3);
        assertThat(duration).isEqualTo(60);
    }

    @Test
    void processTrainerWorkload_existingTrainer_addsDurationToExistingMonth() {
        TrainerTrainingSummary existing = buildExistingTrainer(2024, 3, 100);
        when(workloadRepository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(existing));
        when(workloadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processTrainerWorkload(buildRequest("ADD", 50));

        ArgumentCaptor<TrainerTrainingSummary> captor = ArgumentCaptor.forClass(TrainerTrainingSummary.class);
        verify(workloadRepository).save(captor.capture());

        int duration = getMonthDuration(captor.getValue(), 2024, 3);
        assertThat(duration).isEqualTo(150);
    }

    @Test
    void processTrainerWorkload_deleteAction_subtractsDuration() {
        TrainerTrainingSummary existing = buildExistingTrainer(2024, 3, 120);
        when(workloadRepository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(existing));
        when(workloadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processTrainerWorkload(buildRequest("DELETE", 30));

        ArgumentCaptor<TrainerTrainingSummary> captor = ArgumentCaptor.forClass(TrainerTrainingSummary.class);
        verify(workloadRepository).save(captor.capture());

        int duration = getMonthDuration(captor.getValue(), 2024, 3);
        assertThat(duration).isEqualTo(90);
    }

    @Test
    void processTrainerWorkload_deleteMoreThanExisting_clampsToZero() {
        TrainerTrainingSummary existing = buildExistingTrainer(2024, 3, 20);
        when(workloadRepository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(existing));
        when(workloadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processTrainerWorkload(buildRequest("DELETE", 999));

        ArgumentCaptor<TrainerTrainingSummary> captor = ArgumentCaptor.forClass(TrainerTrainingSummary.class);
        verify(workloadRepository).save(captor.capture());

        int duration = getMonthDuration(captor.getValue(), 2024, 3);
        assertThat(duration).isZero();
    }

    @Test
    void processTrainerWorkload_newYearAndMonth_createsNestedEntries() {
        TrainerTrainingSummary existing = buildExistingTrainer(2023, 1, 40);
        when(workloadRepository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(existing));
        when(workloadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Request for a different year
        TrainerWorkloadRequest request = new TrainerWorkloadRequest()
                .setTrainerUsername(USERNAME)
                .setTrainerFirstName(FIRST_NAME)
                .setTrainerLastName(LAST_NAME)
                .setIsActive(true)
                .setTrainingDate(LocalDate.of(2024, 5, 1))
                .setTrainingDuration(75)
                .setActionType("ADD");
        service.processTrainerWorkload(request);

        ArgumentCaptor<TrainerTrainingSummary> captor = ArgumentCaptor.forClass(TrainerTrainingSummary.class);
        verify(workloadRepository).save(captor.capture());

        TrainerTrainingSummary saved = captor.getValue();
        assertThat(saved.getYears()).hasSize(2);
        assertThat(getMonthDuration(saved, 2023, 1)).isEqualTo(40);
        assertThat(getMonthDuration(saved, 2024, 5)).isEqualTo(75);
    }

    @Test
    void processTrainerWorkload_unknownAction_doesNotChangeData() {
        TrainerTrainingSummary existing = buildExistingTrainer(2024, 3, 50);
        when(workloadRepository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(existing));
        when(workloadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processTrainerWorkload(buildRequest("UNKNOWN", 10));

        ArgumentCaptor<TrainerTrainingSummary> captor = ArgumentCaptor.forClass(TrainerTrainingSummary.class);
        verify(workloadRepository).save(captor.capture());

        int duration = getMonthDuration(captor.getValue(), 2024, 3);
        assertThat(duration).isEqualTo(50); // unchanged
    }

    @Test
    void processTrainerWorkload_savesExactlyOnce() {
        when(workloadRepository.findByTrainerUsername(anyString())).thenReturn(Optional.empty());
        when(workloadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processTrainerWorkload(buildRequest("ADD", 30));

        verify(workloadRepository, times(1)).save(any());
    }

    // ─── getTrainerWorkload ───────────────────────────────────────────────────

    @Test
    void getTrainerWorkload_trainerExists_returnsCorrectResponse() {
        TrainerTrainingSummary existing = buildExistingTrainer(2024, 3, 90);
        when(workloadRepository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(existing));

        TrainerWorkloadDetailsResponse response = service.getTrainerWorkload(USERNAME);

        assertThat(response.getTrainerUsername()).isEqualTo(USERNAME);
        assertThat(response.getTrainerFirstName()).isEqualTo(FIRST_NAME);
        assertThat(response.getTrainerLastName()).isEqualTo(LAST_NAME);
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getYears()).hasSize(1);
        assertThat(response.getYears().get(0).getYear()).isEqualTo(2024);
        assertThat(response.getYears().get(0).getMonths()).hasSize(1);
        assertThat(response.getYears().get(0).getMonths().get(0).getMonth()).isEqualTo(3);
        assertThat(response.getYears().get(0).getMonths().get(0).getTrainingHours()).isEqualTo(90);
    }

    @Test
    void getTrainerWorkload_trainerNotFound_returnsEmptyPlaceholder() {
        when(workloadRepository.findByTrainerUsername(USERNAME)).thenReturn(Optional.empty());

        TrainerWorkloadDetailsResponse response = service.getTrainerWorkload(USERNAME);

        assertThat(response.getTrainerUsername()).isEqualTo(USERNAME);
        assertThat(response.getTrainerFirstName()).isEqualTo("N/A");
        assertThat(response.getYears()).isEmpty();
    }

    @Test
    void getTrainerWorkload_multipleYearsAndMonths_sortedAscending() {
        TrainerTrainingSummary summary = new TrainerTrainingSummary(USERNAME, FIRST_NAME, LAST_NAME, true);
        summary.setYears(new ArrayList<>());

        TrainerTrainingSummary.YearSummary y2024 = new TrainerTrainingSummary.YearSummary(2024);
        y2024.setMonths(new ArrayList<>());
        y2024.getMonths().add(new TrainerTrainingSummary.MonthSummary(6, 30));
        y2024.getMonths().add(new TrainerTrainingSummary.MonthSummary(2, 20));

        TrainerTrainingSummary.YearSummary y2023 = new TrainerTrainingSummary.YearSummary(2023);
        y2023.setMonths(new ArrayList<>());
        y2023.getMonths().add(new TrainerTrainingSummary.MonthSummary(12, 50));

        summary.getYears().add(y2024);
        summary.getYears().add(y2023);

        when(workloadRepository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(summary));

        TrainerWorkloadDetailsResponse response = service.getTrainerWorkload(USERNAME);

        assertThat(response.getYears().get(0).getYear()).isEqualTo(2023);
        assertThat(response.getYears().get(1).getYear()).isEqualTo(2024);
        // months within 2024 should be sorted
        assertThat(response.getYears().get(1).getMonths().get(0).getMonth()).isEqualTo(2);
        assertThat(response.getYears().get(1).getMonths().get(1).getMonth()).isEqualTo(6);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private TrainerWorkloadRequest buildRequest(String actionType, int duration) {
        return new TrainerWorkloadRequest()
                .setTrainerUsername(USERNAME)
                .setTrainerFirstName(FIRST_NAME)
                .setTrainerLastName(LAST_NAME)
                .setIsActive(true)
                .setTrainingDate(TRAINING_DATE)
                .setTrainingDuration(duration)
                .setActionType(actionType);
    }

    private TrainerTrainingSummary buildExistingTrainer(int year, int month, int duration) {
        TrainerTrainingSummary summary = new TrainerTrainingSummary(USERNAME, FIRST_NAME, LAST_NAME, true);
        summary.setYears(new ArrayList<>());
        TrainerTrainingSummary.YearSummary ys = new TrainerTrainingSummary.YearSummary(year);
        ys.setMonths(new ArrayList<>());
        ys.getMonths().add(new TrainerTrainingSummary.MonthSummary(month, duration));
        summary.getYears().add(ys);
        return summary;
    }

    private int getMonthDuration(TrainerTrainingSummary summary, int year, int month) {
        return summary.getYears().stream()
                .filter(y -> y.getYear() == year)
                .flatMap(y -> y.getMonths().stream())
                .filter(m -> m.getMonth() == month)
                .mapToInt(TrainerTrainingSummary.MonthSummary::getTrainingsSummaryDuration)
                .findFirst()
                .orElse(-1);
    }
}
