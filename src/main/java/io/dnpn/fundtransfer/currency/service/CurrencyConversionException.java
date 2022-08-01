package io.dnpn.fundtransfer.currency.service;

/**
 * Exception while converting an amount from one currency to the other.
 */
public class CurrencyConversionException extends Exception {

    public CurrencyConversionException(String message) {
        super(message);
    }

    public CurrencyConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
