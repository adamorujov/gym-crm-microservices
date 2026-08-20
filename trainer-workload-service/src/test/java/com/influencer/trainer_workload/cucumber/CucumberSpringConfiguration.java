package com.influencer.trainer_workload.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import jakarta.jms.ConnectionFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("cucumbertest")
@AutoConfigureMockMvc
public class CucumberSpringConfiguration {

    @MockBean
    public JmsTemplate jmsTemplate;

    @MockBean
    public ConnectionFactory connectionFactory;
}
