package com.gempukku.stccg.serializing;

import com.fasterxml.jackson.core.JsonProcessingException;

public class SerializableException extends JsonProcessingException {

    public SerializableException(String message) {
        super(message);
    }

}