package io.dnpn.fundtransfer;

import io.dnpn.fundtransfer.common.annotation.ExcludeFromJacocoGeneratedReport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

/**
 * Entry point for the Spring Boot application.
 */
@SpringBootApplication
@ExcludeFromJacocoGeneratedReport(reason = "This class is excluded from the JaCoCo report as there is nothing " +
        "meaningful to test. Therefore it is strongly advised not to modify this class so the testable logic is " +
        "located in other classes.")
public class FundTransferApplication {

    public static void main(String[] args) {
        SpringApplication.run(FundTransferApplication.class, args);
    }

    /**
     * Exposes the clock as a bean so other beans relying on time are more testable.
     *
     * @return the default clock.
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
