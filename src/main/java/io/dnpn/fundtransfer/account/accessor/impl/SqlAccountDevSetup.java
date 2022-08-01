package io.dnpn.fundtransfer.account.accessor.impl;

import io.dnpn.fundtransfer.common.ApplicationProfile;
import io.dnpn.fundtransfer.common.annotation.ExcludeFromJacocoGeneratedReport;
import io.dnpn.fundtransfer.currency.Currency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Create mock accounts for a development environment.
 */
@Slf4j
@Profile(ApplicationProfile.DEV)
@Configuration
@ExcludeFromJacocoGeneratedReport(reason = "This class is used only in a dev environment to generate stub data.")
public class SqlAccountDevSetup {

    @Bean
    CommandLineRunner initAccountDatabase(JpaAccountAccessor accessor) {
        return args -> {
            createMockAccounts(accessor);
            logCreatedAccounts(accessor);
        };
    }

    private void createMockAccounts(JpaAccountAccessor accessor) {
        accessor.save(JpaAccountEntity.builder()
                .id(123)
                .balance(new BigDecimal("1234.56"))
                .currency(Currency.EUR)
                .build());

        accessor.save(JpaAccountEntity.builder()
                .id(456)
                .balance(new BigDecimal("450001.78"))
                .currency(Currency.AUD)
                .build());

        accessor.save(JpaAccountEntity.builder()
                .id(789)
                .balance(new BigDecimal("1000000.00"))
                .currency(Currency.JPY)
                .build());

        accessor.save(JpaAccountEntity.builder()
                .id(101)
                .balance(new BigDecimal("41200"))
                .currency(Currency.USD)
                .build());
    }

    private void logCreatedAccounts(JpaAccountAccessor accessor) {
        final List<JpaAccountEntity> accounts = accessor.findAll(Pageable.unpaged())
                .get()
                .toList();
        log.info("Created the following mock accounts for the dev setup: {}", accounts);
    }
}
