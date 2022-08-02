Feature: Health Check
  This feature performs a health check to verify that the server is up and the Cucumber test properly wired

  Scenario: Performs a health check of the server
    When ping the server
    Then get successful ping response