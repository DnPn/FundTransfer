package io.dnpn.fundtransfer.account;

import io.dnpn.fundtransfer.common.ApplicationProfile;
import io.dnpn.fundtransfer.common.annotation.ExcludeFromJacocoGeneratedReport;
import io.dnpn.fundtransfer.currency.Currency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

/**
 * Create mock accounts for a development environment.
 */
@Slf4j
@Profile(ApplicationProfile.DEV)
@Configuration
@ConditionalOnBean(AccountService.class)
@ExcludeFromJacocoGeneratedReport(reason = "This class is used only in a dev environment to generate stub data.")
public class SqlAccountDevSetup {

    @Bean
    CommandLineRunner initAccountDatabase(AccountRepository repository) {
        return args -> {
            createMockAccounts(repository);
            logCreatedAccounts(repository);
        };
    }

    private void createMockAccounts(AccountRepository repository) {
        repository.save(AccountEntity.builder()
                .id(123)
                .balance(new BigDecimal("1234.56"))
                .currency(Currency.USD)
                .build());

        repository.save(AccountEntity.builder()
                .id(456)
                .balance(new BigDecimal("450001.78"))
                .currency(Currency.GBP)
                .build());

        repository.save(AccountEntity.builder()
                .id(789)
                .balance(new BigDecimal("1000000.00"))
                .currency(Currency.JPY)
                .build());

        repository.save(AccountEntity.builder()
                .id(101)
                .balance(new BigDecimal("41200"))
                .currency(Currency.JPY)
                .build());

    }

    private void logCreatedAccounts(AccountRepository repository) {
        final var accounts = repository.findAll(Pageable.unpaged())
                .get()
                .toList();
        log.info("Created the following mock accounts for the dev setup: {}", accounts);
    }
}
