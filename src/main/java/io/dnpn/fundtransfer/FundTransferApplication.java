package io.dnpn.fundtransfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Spring Boot application.
 *
 * This class is excluded from the JaCoCo report as there is nothing useful to test. Therefore it is strongly advised
 * not to modify this class so the testable logic is located in other classes.
 */
@SpringBootApplication
public class FundTransferApplication {

    public static void main(String[] args) {
        SpringApplication.run(FundTransferApplication.class, args);
    }

}
