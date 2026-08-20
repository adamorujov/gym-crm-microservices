package com.influencer.trainer_workload.cucumber.steps;

import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WorkloadCommonSteps {

    @Autowired
    private WorkloadContext workloadContext;

    @Then("the response status is {int}")
    public void theResponseStatusIs(int expectedStatus) throws Exception {
        if (workloadContext.getLastResult() != null) {
            workloadContext.getLastResult().andExpect(status().is(expectedStatus));
        } else {
            assertThat(workloadContext.getLastStatusCode()).isEqualTo(expectedStatus);
        }
    }

    @Then("the response message is {string}")
    public void theResponseMessageIs(String message) throws Exception {
        workloadContext.getLastResult().andExpect(jsonPath("$.message").value(message));
    }
}
