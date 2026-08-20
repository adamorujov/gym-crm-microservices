package com.influencer.trainer_workload.cucumber.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class WorkloadRetrievalSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WorkloadContext workloadContext;

    @When("I request workload for trainer {string}")
    public void iRequestWorkloadForTrainer(String username) throws Exception {
        workloadContext.setLastResult(
                mockMvc.perform(get("/api/trainer-workload/trainer/" + username))
        );
    }

    @When("I request the health endpoint")
    public void iRequestHealthEndpoint() throws Exception {
        workloadContext.setLastResult(
                mockMvc.perform(get("/api/trainer-workload/health"))
        );
    }

    @Then("the workload response contains trainer username {string}")
    public void theWorkloadResponseContainsTrainerUsername(String expectedUsername) throws Exception {
        workloadContext.getLastResult()
                .andExpect(jsonPath("$.trainerUsername").value(expectedUsername));
    }

    @Then("the workload response contains {int} year entries")
    public void theWorkloadResponseContainsYearEntries(int expectedCount) throws Exception {
        if (expectedCount == 0) {
            workloadContext.getLastResult()
                    .andExpect(jsonPath("$.years").isArray())
                    .andExpect(jsonPath("$.years.length()").value(0));
        } else {
            workloadContext.getLastResult()
                    .andExpect(jsonPath("$.years").isArray())
                    .andExpect(jsonPath("$.years.length()").value(expectedCount));
        }
    }
}
