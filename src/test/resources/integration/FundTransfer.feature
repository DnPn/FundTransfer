Feature: Health Check
  This feature is about performing fund transfers

  Scenario: A valid fund transfer updates the accounts
    When execute valid transfer fund
    Then the transfer succeeds
    And the source account is debited of the amount
    And the target account is credited of the converted amount

  Scenario: A transfer from a not existing account fails
    When execute transfer from not existing account
    Then the transfer is denied

  Scenario: A transfer to a not existing account fails
    When execute transfer to not existing account
    Then the transfer is denied