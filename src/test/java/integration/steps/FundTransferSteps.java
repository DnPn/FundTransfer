package integration.steps;

import integration.helper.ApplicationApiAccessor;
import integration.helper.CurrencyConversionApiMock;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.dnpn.fundtransfer.account.accessor.impl.JpaAccountAccessor;
import io.dnpn.fundtransfer.account.accessor.impl.JpaAccountEntity;
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

    private static final JpaAccountEntity SOURCE_ACCOUNT = JpaAccountEntity.builder()
            .id(123)
            .balance(new BigDecimal("50.21"))
            .currency(Currency.GBP)
            .build();
    private static final JpaAccountEntity TARGET_ACCOUNT = JpaAccountEntity.builder()
            .id(456)
            .balance(new BigDecimal("10000.19"))
            .currency(Currency.JPY)
            .build();
    private static final Collection<JpaAccountEntity> ACCOUNTS = List.of(SOURCE_ACCOUNT, TARGET_ACCOUNT);

    private static final BigDecimal VALID_TRANSFER_AMOUNT = new BigDecimal("25");

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

    @When("execute valid transfer fund")
    public void executeValidTransferFund() {
        mockSuccessfulCurrencyConversionApiCall();

        final var request = TransferApiRequest.builder()
                .amount(VALID_TRANSFER_AMOUNT)
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
        assertBigDecimalEquals(expectedAmount, actualAmount);
    }

    @Then("the target account is credited of the converted amount")
    public void theTargetAccountIsCreditedOfTheConvertedAmount() {
        final var expectedAmount = TARGET_ACCOUNT.getBalance().add(CONVERTED_AMOUNT);
        final var actualAmount = getUpdatedAccount(TARGET_ACCOUNT).getBalance();
        assertBigDecimalEquals(expectedAmount, actualAmount);
    }

    private void mockSuccessfulCurrencyConversionApiCall() {
        final var response = new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .addHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)
                .setBody(CURRENCY_CONVERSION_API_RESPONSE);

        currencyConversionApiMock.mockNextResponse(response);
    }

    private JpaAccountEntity getUpdatedAccount(JpaAccountEntity account) {
        return accountAccessor.findById(account.getId()).get();
    }

    /**
     * Asserts that 2 BigDecimal values are equal. This method ensures that both values use the same scale
     * solving the problem of different trailing zeros (for example, we want 25 to be considered equal to 25.00).
     *
     * @param expected the expected value
     * @param actual   the actual value
     */
    private void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        int targetScale = Math.max(expected.scale(), actual.scale());
        expected = expected.setScale(targetScale);
        actual = actual.setScale(targetScale);

        assertEquals(expected, actual);
    }
}
