Feature: Fund Transfer
  This feature is about performing fund transfers

  Scenario: A valid fund transfer updates the accounts
    When execute valid fund transfer
    Then the transfer succeeds
    And the source account is debited of the amount
    And the target account is credited of the converted amount

  Scenario: A transfer from a not existing account is denied
    When execute transfer from not existing account
    Then the transfer is denied

  Scenario: A transfer to a not existing account is denied
    When execute transfer to not existing account
    Then the transfer is denied

  Scenario: A transfer fails if the exchange rate cannot be retrieved
    Given the exchange rate cannot be retrieved
    When execute valid fund transfer
    Then the transfer fails

  Scenario: A transfer bigger than the source account balance is denied
    When execute fund transfer bigger than the source account balance
    Then the transfer is denied