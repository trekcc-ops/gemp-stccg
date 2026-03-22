package com.gempukku.stccg.serializing;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SerializableItem {

    @JsonProperty("number")
    final int x;

    @JsonIgnore
    final String numberName;

    @JsonCreator
    public SerializableItem(@JsonProperty("number") int number,
                            @JacksonInject SerializingLibrary library) throws SerializableException {
        if (number < 0) {
            throw new SerializableException("Can't use a negative number");
        } else {
            x = number;
            String numName = library.get(x);
            if (numName != null) {
                numberName = library.get(x);
            } else {
                throw new SerializableException("Can't find the number name for " + number);
            }
        }
    }

    public String getNumberName() {
        return numberName;
    }

    public String toString() {
        return "number: " + x + "; numberName: " + numberName;
    }

}