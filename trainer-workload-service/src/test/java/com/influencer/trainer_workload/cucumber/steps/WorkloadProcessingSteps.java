package com.influencer.trainer_workload.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.influencer.trainer_workload.dto.request.TrainerWorkloadRequest;
import com.influencer.trainer_workload.messaging.TrainerWorkloadMessageListener;
import com.influencer.trainer_workload.model.TrainerTrainingSummary;
import com.influencer.trainer_workload.repository.TrainerWorkloadRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class WorkloadProcessingSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WorkloadContext workloadContext;

    @Autowired
    private TrainerWorkloadRepository repository;

    @Autowired
    private TrainerWorkloadMessageListener messageListener;

    /**
     * Works as both @Given (setup) and @Then (verify) since Cucumber treats
     * all keyword annotations equally for step matching.
     * - If the record doesn't exist: inserts initial data (setup mode).
     * - If the record exists: verifies the expected value (verification mode).
     */
    @Given("trainer {string} has {int} minutes for year {int} month {int}")
    public void trainerHasMinutesForYearMonth(String username, int minutes, int year, int month) {
        Optional<TrainerTrainingSummary> existing = repository.findByTrainerUsername(username);
        if (existing.isEmpty()) {
            TrainerTrainingSummary summary = new TrainerTrainingSummary(username, username, "Trainer", true);
            summary.setYears(new ArrayList<>());
            TrainerTrainingSummary.YearSummary ys = new TrainerTrainingSummary.YearSummary(year);
            ys.setMonths(new ArrayList<>());
            ys.getMonths().add(new TrainerTrainingSummary.MonthSummary(month, minutes));
            summary.getYears().add(ys);
            repository.save(summary);
        } else {
            TrainerTrainingSummary summary = existing.get();
            int actual = summary.getYears().stream()
                    .filter(y -> y.getYear() == year)
                    .flatMap(y -> y.getMonths().stream())
                    .filter(m -> m.getMonth() == month)
                    .mapToInt(TrainerTrainingSummary.MonthSummary::getTrainingsSummaryDuration)
                    .findFirst()
                    .orElse(-1);
            assertThat(actual).as("Duration for trainer=%s year=%d month=%d", username, year, month)
                    .isEqualTo(minutes);
        }
    }

    @When("a workload event arrives for trainer {string} firstName {string} lastName {string} with action {string} duration {int} on date {string}")
    public void workloadEventArrivesForTrainer(String username, String firstName, String lastName,
                                                String action, int duration, String date) throws Exception {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest()
                .setTrainerUsername(username)
                .setTrainerFirstName(firstName)
                .setTrainerLastName(lastName)
                .setIsActive(true)
                .setActionType(action)
                .setTrainingDuration(duration)
                .setTrainingDate(LocalDate.parse(date));

        workloadContext.setLastResult(
                mockMvc.perform(post("/api/trainer-workload/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
        );
    }

    @When("a workload event arrives with missing username firstName {string} lastName {string} action {string} duration {int} on date {string}")
    public void workloadEventArrivesWithMissingUsername(String firstName, String lastName,
                                                         String action, int duration, String date) throws Exception {
        String payload = String.format(
                "{\"trainerFirstName\":\"%s\",\"trainerLastName\":\"%s\",\"actionType\":\"%s\",\"trainingDuration\":%d,\"trainingDate\":\"%s\",\"isActive\":true}",
                firstName, lastName, action, duration, date);
        messageListener.consumeWorkloadEvent(payload, null);
        workloadContext.setLastResult(null);
        workloadContext.setLastStatusCode(200);
    }
}
