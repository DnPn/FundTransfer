package integration;

import io.cucumber.spring.CucumberContextConfiguration;
import io.dnpn.fundtransfer.FundTransferApplication;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = FundTransferApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {
}
