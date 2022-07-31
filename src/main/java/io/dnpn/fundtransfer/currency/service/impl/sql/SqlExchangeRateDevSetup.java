package io.dnpn.fundtransfer.currency.service.impl.sql;

import io.dnpn.fundtransfer.common.ApplicationProfile;
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
 * Create mock exchange rate values for a development environment.
 */
@Slf4j
@Profile(ApplicationProfile.DEV)
@Configuration
public class SqlExchangeRateDevSetup {

    @Bean
    CommandLineRunner initExchangeRateDatabase(JpaExchangeRateAccessor accessor) {
        return args -> {
            createMockExchangeRates(accessor);
            logCreatedExchangeRates(accessor);
        };
    }

    private void createMockExchangeRates(JpaExchangeRateAccessor accessor) {
        accessor.save(JpaExchangeRateEntity.builder()
                .currency(Currency.EUR)
                .rateToUsd(new BigDecimal("1.02"))
                .build());

        accessor.save(JpaExchangeRateEntity.builder()
                .currency(Currency.JPY)
                .rateToUsd(new BigDecimal("0.0073"))
                .build());

        accessor.save(JpaExchangeRateEntity.builder()
                .currency(Currency.USD)
                .rateToUsd(BigDecimal.ONE)
                .build());

        accessor.save(JpaExchangeRateEntity.builder()
                .currency(Currency.CAD)
                .rateToUsd(new BigDecimal("0.77"))
                .build());

        accessor.save(JpaExchangeRateEntity.builder()
                .currency(Currency.CNY)
                .rateToUsd(new BigDecimal("0.15"))
                .build());
    }

    private void logCreatedExchangeRates(JpaExchangeRateAccessor accessor) {
        final List<JpaExchangeRateEntity> accounts = accessor.findAll(Pageable.unpaged())
                .get()
                .toList();
        log.info("Created the following mock exchange rates for the dev setup: {}", accounts);
    }
}