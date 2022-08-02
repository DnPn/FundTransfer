package integration.steps;

import integration.helper.RestAccessor;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HealthCheckSteps {

    @Autowired
    private RestAccessor accessor;

    @When("ping the server")
    public void sendPing() {
        accessor.ping();
    }

    @Then("get successful ping response")
    public void pingIsSuccessful() {
        var response = accessor.getLatestResponse();
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }
}
