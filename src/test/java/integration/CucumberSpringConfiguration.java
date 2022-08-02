package integration;

import io.cucumber.spring.CucumberContextConfiguration;
import io.dnpn.fundtransfer.FundTransferApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Configuration to start the Spring Boot application for the tests.
 */
@ActiveProfiles("integ")
@CucumberContextConfiguration
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {FundTransferApplication.class, CucumberComponentScan.class}
)
public class CucumberSpringConfiguration {
}
