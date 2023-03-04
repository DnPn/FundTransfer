package io.dnpn.fundtransfer.currency.service.impl.sql;

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
 * Create mock exchange rate values for a development environment.
 */
@Slf4j
@Profile(ApplicationProfile.DEV)
@Configuration
@ConditionalOnBean(SqlCurrencyConversionService.class)
@ExcludeFromJacocoGeneratedReport(reason = "This class is used only in a dev environment to generate stub data.")
public class SqlExchangeRateDevSetup {

    @Bean
    CommandLineRunner initExchangeRateDatabase(ExchangeRateRepository repository) {
        return args -> {
            createMockExchangeRates(repository);
            logCreatedExchangeRates(repository);
        };
    }

    private void createMockExchangeRates(ExchangeRateRepository repository) {
        repository.save(ExchangeRateEntity.builder()
                .currency(Currency.GBP)
                .rateToUsd(new BigDecimal("1.23"))
                .build());

        repository.save(ExchangeRateEntity.builder()
                .currency(Currency.USD)
                .rateToUsd(BigDecimal.ONE)
                .build());
    }

    private void logCreatedExchangeRates(ExchangeRateRepository repository) {
        final var accounts = repository.findAll(Pageable.unpaged())
                .get()
                .toList();
        log.info("Created the following mock exchange rates for the dev setup: {}", accounts);
    }
}
