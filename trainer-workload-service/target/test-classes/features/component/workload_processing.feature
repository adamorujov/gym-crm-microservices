Feature: Trainer Workload Processing
  As the trainer workload service
  I want to process incoming workload events
  So that trainer training summaries are maintained correctly

  Scenario: Create new trainer record on first ADD event
    When a workload event arrives for trainer "new.trainer" firstName "New" lastName "Trainer" with action "ADD" duration 60 on date "2024-03-15"
    Then the response status is 200
    And trainer "new.trainer" has 60 minutes for year 2024 month 3

  Scenario: Add duration to existing trainer record
    Given trainer "exist.trainer" has 60 minutes for year 2024 month 3
    When a workload event arrives for trainer "exist.trainer" firstName "Exist" lastName "Trainer" with action "ADD" duration 30 on date "2024-03-20"
    Then the response status is 200
    And trainer "exist.trainer" has 90 minutes for year 2024 month 3

  Scenario: Delete reduces duration from trainer record
    Given trainer "delete.trainer" has 120 minutes for year 2024 month 5
    When a workload event arrives for trainer "delete.trainer" firstName "Delete" lastName "Trainer" with action "DELETE" duration 40 on date "2024-05-10"
    Then the response status is 200
    And trainer "delete.trainer" has 80 minutes for year 2024 month 5

  Scenario: Delete cannot reduce duration below zero
    Given trainer "zero.trainer" has 20 minutes for year 2024 month 6
    When a workload event arrives for trainer "zero.trainer" firstName "Zero" lastName "Trainer" with action "DELETE" duration 100 on date "2024-06-01"
    Then the response status is 200
    And trainer "zero.trainer" has 0 minutes for year 2024 month 6

  Scenario: Fail to process workload event with missing trainer username
    When a workload event arrives with missing username firstName "Bad" lastName "Data" action "ADD" duration 60 on date "2024-03-15"
    Then the response status is 200

  Scenario: Add training creates new year entry for existing trainer
    Given trainer "newyear.trainer" has 30 minutes for year 2023 month 1
    When a workload event arrives for trainer "newyear.trainer" firstName "NewYear" lastName "Trainer" with action "ADD" duration 45 on date "2024-01-15"
    Then the response status is 200
    And trainer "newyear.trainer" has 30 minutes for year 2023 month 1
    And trainer "newyear.trainer" has 45 minutes for year 2024 month 1
