package io.dnpn.fundtransfer.currency.accessor.impl;

import io.dnpn.fundtransfer.currency.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaExchangeRateAccessor extends JpaRepository<JpaExchangeRateEntity, Currency> {
}
