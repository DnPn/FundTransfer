package io.dnpn.fundtransfer.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Defines how to handle money. Specifically enforces the scale of {@link BigDecimal} values used to represent money.
 */
public final class MoneyHandling {

    private MoneyHandling() {
    }

    /**
     * The scale is set to 2 decimals (cents).
     */
    public static final int SCALE_FOR_MONEY = 2;
    /**
     * If we consider that no amount will ever exceed 1,000 trillions (10^15) - 1 cent, then the below precision
     * allow to represent exactly all the amounts.
     */
    public static final int PRECISION_FOR_MONEY = 15 + SCALE_FOR_MONEY;
    /**
     * When we credit an amount to a client, in case we need to decrease the scale we round to floor. This ensures
     * that the bank never looses money on crediting amount with wrong scaling (for example after a currency
     * conversion).
     */
    public static final RoundingMode ROUNDING_MODE_FOR_CLIENT_CREDIT = RoundingMode.FLOOR;

    /**
     * Custom deserializer to enforce the scale of the deserialized {@link BigDecimal} values.
     */
    public static class Deserializer extends NumberDeserializers.BigDecimalDeserializer {
        @Override
        public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return super.deserialize(jsonParser, deserializationContext)
                    .setScale(SCALE_FOR_MONEY, RoundingMode.HALF_UP);
        }
    }

    /**
     * Custom serializer to enforce the scale of the serialized {@link BigDecimal} values.
     */
    public static class Serializer extends JsonSerializer<BigDecimal> {
        @Override
        public void serialize(BigDecimal value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            final var scaledValue = value.setScale(SCALE_FOR_MONEY, RoundingMode.HALF_UP);
            jsonGenerator.writeNumber(scaledValue);
        }
    }
}
