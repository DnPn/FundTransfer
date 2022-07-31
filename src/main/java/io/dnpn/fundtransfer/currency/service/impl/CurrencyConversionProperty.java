package io.dnpn.fundtransfer.currency.service.impl;

/**
 * Currency conversion properties (used in the file `application.properties`).
 */
public class CurrencyConversionProperty {

    /**
     * Prefix of all properties related to the currency conversion.
     */
    private static final String PROPERTY_PREFIX = "currencyConversion.";

    public static final String MODE = PROPERTY_PREFIX + "mode";

    public static final String SQL_MODE = "sql";
    public static final String API_MODE = "api";
}
