package com.influencer.trainer_workload.cucumber.steps;

import com.influencer.trainer_workload.messaging.TrainerWorkloadMessageListener;
import com.influencer.trainer_workload.model.TrainerTrainingSummary;
import com.influencer.trainer_workload.repository.TrainerWorkloadRepository;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;

public class WorkloadIntegrationSteps {

    @Autowired
    private TrainerWorkloadMessageListener messageListener;

    @Autowired
    private TrainerWorkloadRepository repository;

    @Autowired
    private WorkloadContext workloadContext;

    @When("a JMS message arrives with trainer {string} action {string} duration {int} date {string}")
    public void jmsMessageArrives(String username, String action, int duration, String date) {
        String payload = String.format(
                "{\"trainerUsername\":\"%s\",\"trainerFirstName\":\"%s\",\"trainerLastName\":\"Trainer\","
                        + "\"actionType\":\"%s\",\"trainingDuration\":%d,\"trainingDate\":\"%s\",\"isActive\":true}",
                username, username, action, duration, date);
        messageListener.consumeWorkloadEvent(payload, null);
        workloadContext.setLastResult(null);
        workloadContext.setLastStatusCode(200);
    }

    @When("an invalid JMS message arrives with payload {string}")
    public void invalidJmsMessageArrives(String payload) {
        messageListener.consumeWorkloadEvent(payload, null);
        workloadContext.setLastResult(null);
        workloadContext.setLastStatusCode(200);
    }

    @Then("the workload record for {string} is not created")
    public void theWorkloadRecordIsNotCreated(String username) {
        Optional<TrainerTrainingSummary> record = repository.findByTrainerUsername(username);
        assertThat(record).isEmpty();
    }
}
