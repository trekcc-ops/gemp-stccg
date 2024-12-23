package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.io.IOException;

public class MissionLocationSerializer extends StdSerializer<MissionLocation> {

    public MissionLocationSerializer() {
        this(null);
    }

    public MissionLocationSerializer(Class<MissionLocation> t) {
        super(t);
    }

    @Override
    public void serialize(MissionLocation location, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("quadrant", location.getQuadrant().name());
        if (location.getRegion() != null)
            jsonGenerator.writeStringField("region", location.getRegion().name());
        jsonGenerator.writeStringField("locationName", location.getLocationName());
        jsonGenerator.writeNumberField("locationZoneIndex", location.getLocationZoneIndex());
        jsonGenerator.writeBooleanField("isCompleted", location.isCompleted());

        if (!location.getCardsSeededUnderneath().isEmpty()) {
            jsonGenerator.writeFieldName("cardsSeededUnderneath");
            jsonGenerator.writeStartArray();
            for (PhysicalCard card : location.getCardsSeededUnderneath())
                jsonGenerator.writeNumber(card.getCardId());
            jsonGenerator.writeEndArray();
        }

        jsonGenerator.writeEndObject();
    }
}