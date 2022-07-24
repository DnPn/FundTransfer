package io.dnpn.fundtransfer.currency.accessor;

/**
 * Exception while getting the exchange rate of 2 currencies.
 */
public class ExchangeRateException extends Exception {

    public ExchangeRateException(String message) {
        super(message);
    }
}
