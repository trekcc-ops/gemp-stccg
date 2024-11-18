package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class ST1ELocationSerializer extends StdSerializer<ST1ELocation> {

    public ST1ELocationSerializer() {
        this(null);
    }

    public ST1ELocationSerializer(Class<ST1ELocation> t) {
        super(t);
    }

    @Override
    public void serialize(ST1ELocation location, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("quadrant", location.getQuadrant().name());
        if (location.getRegion() != null)
            jsonGenerator.writeStringField("region", location.getRegion().name());
        jsonGenerator.writeStringField("locationName", location.getLocationName());
        jsonGenerator.writeNumberField("locationZoneIndex", location.getLocationZoneIndex());
        jsonGenerator.writeEndObject();
    }
}