package io.dnpn.fundtransfer.currency.service.impl.sql;

import io.dnpn.fundtransfer.currency.Currency;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.jpa.repository.JpaRepository;

@ConditionalOnBean(SqlCurrencyConversionService.class)
interface JpaExchangeRateAccessor extends JpaRepository<JpaExchangeRateEntity, Currency> {
}
