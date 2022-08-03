package integration.steps;

import integration.helper.ApplicationApiAccessor;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HealthCheckSteps {

    @Autowired
    private ApplicationApiAccessor accessor;

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
