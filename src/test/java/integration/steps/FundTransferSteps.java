package integration.steps;

import integration.helper.ApplicationApiAccessor;
import integration.helper.CurrencyConversionApiMock;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.dnpn.fundtransfer.account.accessor.impl.JpaAccountAccessor;
import io.dnpn.fundtransfer.account.accessor.impl.JpaAccountEntity;
import io.dnpn.fundtransfer.common.MoneyHandling;
import io.dnpn.fundtransfer.currency.Currency;
import io.dnpn.fundtransfer.transfer.api.TransferApiRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class FundTransferSteps {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";

    private static final long NOT_EXITING_ACCOUNT_ID = 999;
    private static final JpaAccountEntity SOURCE_ACCOUNT = JpaAccountEntity.builder()
            .id(123)
            .balance(new BigDecimal("47.21"))
            .currency(Currency.GBP)
            .build();
    private static final JpaAccountEntity TARGET_ACCOUNT = JpaAccountEntity.builder()
            .id(456)
            .balance(new BigDecimal("10000.19"))
            .currency(Currency.JPY)
            .build();
    private static final Collection<JpaAccountEntity> ACCOUNTS = List.of(SOURCE_ACCOUNT, TARGET_ACCOUNT);

    private static final BigDecimal VALID_TRANSFER_AMOUNT = new BigDecimal("25");
    private static final BigDecimal TOO_BIG_TRANSFER_AMOUNT = new BigDecimal("50");

    private static final BigDecimal CONVERTED_AMOUNT = new BigDecimal("3724.305775");
    private static final String CURRENCY_CONVERSION_API_RESPONSE = """
            {
              "date": "2018-02-22",
              "historical": "",
              "info": {
                "rate": 148.972231,
                "timestamp": 1519328414
              },
              "query": {
                "amount": %f,
                "from": "GBP",
                "to": "JPY"
              },
              "result": %f,
              "success": true
            }
            """.formatted(
            VALID_TRANSFER_AMOUNT,
            CONVERTED_AMOUNT
    );
    private static final String INVALID_CURRENCY_API_RESPONSE = """
            {
                "error": {
                    "code": "invalid_to_currency",
                    "message": "You have entered an invalid \\"to\\" property. [Example: to=GBP]"
                }
            }
            """;


    @Autowired
    private CurrencyConversionApiMock currencyConversionApiMock;
    @Autowired
    private ApplicationApiAccessor apiAccessor;
    @Autowired
    private JpaAccountAccessor accountAccessor;


    @Before
    public void beforeEachScenario() {
        log.debug("Deleting all the accounts...");
        accountAccessor.deleteAll();
        log.debug("Creating the accounts for the scenario...");
        accountAccessor.saveAll(ACCOUNTS);
    }

    @Given("the exchange rate cannot be retrieved")
    public void theExchangeRateCannotBeRetrieved() {
        mockExchangeRateCannotBeRetrieved();
    }

    @When("execute valid fund transfer")
    public void executeValidTransferFund() {
        // the default behaviour of mock server is to successfully respond to a currency conversion request. It can be
        // overridden in a @Given step since the returned response will be the first registered.
        mockSuccessfulCurrencyConversionApiCall();

        final var request = TransferApiRequest.builder()
                .amount(VALID_TRANSFER_AMOUNT)
                .fromAccount(SOURCE_ACCOUNT.getId())
                .toAccount(TARGET_ACCOUNT.getId())
                .build();
        apiAccessor.transfer(request);
    }

    @When("execute transfer from not existing account")
    public void executeTransferFromNotExistingAccount() {
        final var request = TransferApiRequest.builder()
                .amount(VALID_TRANSFER_AMOUNT)
                .fromAccount(NOT_EXITING_ACCOUNT_ID)
                .toAccount(TARGET_ACCOUNT.getId())
                .build();
        apiAccessor.transfer(request);
    }

    @When("execute transfer to not existing account")
    public void executeTransferToNotExistingAccount() {
        final var request = TransferApiRequest.builder()
                .amount(VALID_TRANSFER_AMOUNT)
                .fromAccount(SOURCE_ACCOUNT.getId())
                .toAccount(NOT_EXITING_ACCOUNT_ID)
                .build();
        apiAccessor.transfer(request);
    }

    @When("execute fund transfer bigger than the source account balance")
    public void executeFundTransferBiggerThanTheSourceAccountBalance() {
        final var request = TransferApiRequest.builder()
                .amount(TOO_BIG_TRANSFER_AMOUNT)
                .fromAccount(SOURCE_ACCOUNT.getId())
                .toAccount(TARGET_ACCOUNT.getId())
                .build();
        apiAccessor.transfer(request);
    }

    @Then("the transfer succeeds")
    public void theTransferSucceeds() {
        final var response = apiAccessor.getLatestResponse();
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }


    @Then("the source account is debited of the amount")
    public void theSourceAccountIsDebitedOfTheAmount() {
        final var expectedAmount = SOURCE_ACCOUNT.getBalance().subtract(VALID_TRANSFER_AMOUNT);
        final var actualAmount = getUpdatedAccount(SOURCE_ACCOUNT).getBalance();
        assertEquals(expectedAmount, actualAmount);
    }

    @Then("the target account is credited of the converted amount")
    public void theTargetAccountIsCreditedOfTheConvertedAmount() {
        final var scaledConvertedAmount = CONVERTED_AMOUNT.setScale(MoneyHandling.SCALE_FOR_MONEY,
                MoneyHandling.ROUNDING_MODE_FOR_CLIENT_CREDIT);
        final var expectedAmount = TARGET_ACCOUNT.getBalance().add(scaledConvertedAmount);
        final var actualAmount = getUpdatedAccount(TARGET_ACCOUNT).getBalance();
        assertEquals(expectedAmount, actualAmount);
    }

    @Then("the transfer is denied")
    public void theTransferIsDenied() {
        final var response = apiAccessor.getLatestResponse();
        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Then("the transfer fails")
    public void theTransferFails() {
        final var response = apiAccessor.getLatestResponse();
        assertTrue(response.getStatusCode().is5xxServerError());
    }

    private void mockSuccessfulCurrencyConversionApiCall() {
        final var response = new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .addHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)
                .setBody(CURRENCY_CONVERSION_API_RESPONSE);

        currencyConversionApiMock.mockNextResponse(response);
    }

    private void mockExchangeRateCannotBeRetrieved() {
        final var response = new MockResponse()
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .addHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)
                .setBody(INVALID_CURRENCY_API_RESPONSE);

        currencyConversionApiMock.mockNextResponse(response);
    }

    private JpaAccountEntity getUpdatedAccount(JpaAccountEntity account) {
        return accountAccessor.findById(account.getId()).get();
    }
}
