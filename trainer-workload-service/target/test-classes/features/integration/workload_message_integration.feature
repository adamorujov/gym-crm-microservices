Feature: Trainer Workload JMS Message Integration
  As the trainer workload service
  I want to receive and process JMS messages from Gym CRM
  So that the workload service is updated when trainings are created

  Scenario: JMS message with ADD action creates trainer workload record
    When a JMS message arrives with trainer "jms.trainer" action "ADD" duration 75 date "2024-07-10"
    Then trainer "jms.trainer" has 75 minutes for year 2024 month 7

  Scenario: JMS message with DELETE action reduces trainer workload
    Given trainer "jms.del.trainer" has 100 minutes for year 2024 month 8
    When a JMS message arrives with trainer "jms.del.trainer" action "DELETE" duration 25 date "2024-08-15"
    Then trainer "jms.del.trainer" has 75 minutes for year 2024 month 8

  Scenario: Invalid JMS message is sent to dead-letter queue
    When an invalid JMS message arrives with payload "{invalid-json}"
    Then the workload record for "invalid" is not created
