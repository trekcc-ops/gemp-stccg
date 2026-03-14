package com.gempukku.stccg.actions;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public interface ActionResultTest {

    default void assertSerializedFields(JsonNode resultJson, String... resultSpecificFields) {
        assertNotNull(resultJson);
        boolean verified = true;
        Set<String> fieldsToFind = new HashSet<>();
        fieldsToFind.addAll(List.of("resultId", "performingPlayerId", "type", "timestamp"));
        fieldsToFind.addAll(List.of(resultSpecificFields));

        assertEquals(fieldsToFind.size(), resultJson.size());

        for (String fieldName : fieldsToFind) {
            if (!resultJson.has(fieldName)) {
                System.out.println("Serialized action result does not have expected field '" + fieldName + "'");
                verified = false;
            }
        }
        assertTrue(verified);
    }

}