package com.influencer.trainer_workload.cucumber.steps;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;

@Component
@ScenarioScope
public class WorkloadContext {

    private ResultActions lastResult;
    private int lastStatusCode = 200;

    public ResultActions getLastResult() {
        return lastResult;
    }

    public void setLastResult(ResultActions lastResult) {
        this.lastResult = lastResult;
    }

    public int getLastStatusCode() {
        return lastStatusCode;
    }

    public void setLastStatusCode(int lastStatusCode) {
        this.lastStatusCode = lastStatusCode;
    }
}
