Feature: Trainer Workload Retrieval
  As a client
  I want to retrieve trainer workload summary

  Scenario: Get workload for existing trainer
    Given trainer "query.trainer" has 90 minutes for year 2024 month 4
    When I request workload for trainer "query.trainer"
    Then the response status is 200
    And the workload response contains trainer username "query.trainer"
    And the workload response contains 1 year entries

  Scenario: Get workload for non-existing trainer returns empty summary
    When I request workload for trainer "ghost.trainer"
    Then the response status is 200
    And the workload response contains trainer username "ghost.trainer"
    And the workload response contains 0 year entries

  Scenario: Health check endpoint returns OK
    When I request the health endpoint
    Then the response status is 200
    And the response message is "Trainer Workload Service is running"
