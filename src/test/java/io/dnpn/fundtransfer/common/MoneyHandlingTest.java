package io.dnpn.fundtransfer.common;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoneyHandlingTest {

    private static final String SCALE_TO_MONEY_CSV_SOURCE = """
            1.23,   1.23
            1.2,    1.20
            1,      1.00
            1.234,  1.23
            1.235,  1.24
            """;

    @ParameterizedTest
    @CsvSource(textBlock = SCALE_TO_MONEY_CSV_SOURCE)
    void WHEN_deserialize_THEN_setProperScale(String json, String expected) {
        var actual = deserialize(json);
        var parsedExpected = new BigDecimal(expected);

        assertEquals(parsedExpected, actual);
    }

    @SneakyThrows
    private BigDecimal deserialize(String json) {
        try (var inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
            var mapper = new ObjectMapper();
            var deserializer = new MoneyHandling.Deserializer();
            var parser = mapper.getFactory().createParser(inputStream);
            var context = mapper.getDeserializationContext();
            parser.nextToken();

            return deserializer.deserialize(parser, context);
        }
    }

    @ParameterizedTest
    @CsvSource(textBlock = SCALE_TO_MONEY_CSV_SOURCE)
    void WHEN_serialize_THEN_useProperScale(String value, String expected) {
        var parsedValue = new BigDecimal(value);
        var actual = serialize(parsedValue);

        assertEquals(expected, actual);
    }

    @SneakyThrows
    private String serialize(BigDecimal value) {
        StringWriter jsonWriter = null;

        try {
            jsonWriter = new StringWriter();
            serializeInWriter(value, jsonWriter);
            return jsonWriter.toString();

        } finally {
            if (jsonWriter != null) {
                jsonWriter.close();
            }
        }
    }

    @SneakyThrows
    private void serializeInWriter(BigDecimal value, StringWriter writer) {
        var serializer = new MoneyHandling.Serializer();
        var jsonGenerator = new JsonFactory().createGenerator(writer);
        var serializerProvider = new ObjectMapper().getSerializerProvider();

        serializer.serialize(value, jsonGenerator, serializerProvider);
        jsonGenerator.flush();
    }
}