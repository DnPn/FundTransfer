package io.dnpn.fundtransfer.currency.accessor.impl;

import io.dnpn.fundtransfer.currency.accessor.ExchangeRateAccessor;
import io.dnpn.fundtransfer.currency.accessor.ExchangeRateException;
import io.dnpn.fundtransfer.currency.accessor.ExchangeRateRequest;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DummyExchangeRateAccessor implements ExchangeRateAccessor {

    @Override
    public BigDecimal getExchangeRate(@NonNull ExchangeRateRequest request) throws ExchangeRateException {
        return BigDecimal.ONE;
    }
}
