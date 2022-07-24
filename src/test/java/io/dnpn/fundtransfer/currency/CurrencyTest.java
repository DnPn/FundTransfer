package io.dnpn.fundtransfer.currency;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CurrencyTest {

    /**
     * Currency being the simplest form of the enum class, there is no test covering all the values. This "dumb"
     * test is used for the JaCoCo report so we are not distracted in the report by false positives.
     *
     * @param currency the currency to cover.
     */
    @ParameterizedTest
    @EnumSource(Currency.class)
    void dumbTestToCoverValues(Currency currency) {
        currency.name();
    }

}