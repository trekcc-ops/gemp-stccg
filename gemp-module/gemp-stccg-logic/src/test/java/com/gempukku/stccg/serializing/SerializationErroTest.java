package com.gempukku.stccg.serializing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.game.InvalidGameLogicException;
import org.junit.jupiter.api.Test;

public class SerializationErroTest {

    @Test
    void serializeItem() throws JsonProcessingException, InvalidGameLogicException {
        SerializingLibrary library = new SerializingLibrary();
        SerializableItem item = new SerializableItem(5, library);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.WRAP_EXCEPTIONS, false);
        mapper.setInjectableValues(new InjectableValues.Std().addValue(SerializingLibrary.class, library));
        String jsonString = mapper.writeValueAsString(item);

        deserialize(jsonString, mapper);
        deserializeWithNumber(jsonString, -99, mapper);
        deserializeWithNumber(jsonString, 11, mapper);
        deserializeWithNumber(jsonString, 200, mapper);

        try {
            deserializeWithString(jsonString, "blork", mapper);
        } catch(JsonProcessingException exp) {
            System.out.println("ERROR: Can't use a string");
        }
    }

    private void deserializeWithString(String originalJson, String newString, ObjectMapper mapper) throws JsonProcessingException {
        String jsonStringWithNegative = originalJson.replace("5", newString);
        deserialize(jsonStringWithNegative, mapper);
    }


    private void deserializeWithNumber(String originalJson, int newNumber, ObjectMapper mapper) throws JsonProcessingException {
        String jsonStringWithNegative = originalJson.replace("5", String.valueOf(newNumber));
        deserialize(jsonStringWithNegative, mapper);
    }

    private void deserialize(String jsonString, ObjectMapper mapper) throws JsonProcessingException {
        try {
            SerializableItem itemAttempt = mapper.readValue(jsonString, SerializableItem.class);
            System.out.println("SUCCESS: " + itemAttempt);
        } catch(JsonProcessingException exp) {
            if (exp.getCause() instanceof SerializableException sExp) {
                System.out.println("ERROR: " + sExp.getMessage());
            } else {
                throw exp;
            }
        }
    }

}