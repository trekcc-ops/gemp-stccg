package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class PhysicalCardSerializer extends StdSerializer<PhysicalCard> {

    public PhysicalCardSerializer() {
        this(null);
    }

    public PhysicalCardSerializer(Class<PhysicalCard> t) {
        super(t);
    }

    @Override
    public void serialize(PhysicalCard physicalCard, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("title", physicalCard.getTitle());
        jsonGenerator.writeStringField("blueprintId", physicalCard.getBlueprintId());
        jsonGenerator.writeNumberField("cardId", physicalCard.getCardId());
        jsonGenerator.writeStringField("owner", physicalCard.getOwnerName());
        jsonGenerator.writeStringField("zone", physicalCard.getZone().name());
        if (physicalCard.getAttachedTo() != null)
            jsonGenerator.writeNumberField("attachedToCardId", physicalCard.getAttachedTo().getCardId());
        if (physicalCard.getStackedOn() != null)
            jsonGenerator.writeNumberField("stackedOnCardId", physicalCard.getStackedOn().getCardId());
        if (physicalCard.getLocationZoneIndex() >= 0)
            jsonGenerator.writeNumberField("locationZoneIndex", physicalCard.getLocationZoneIndex());

        if (physicalCard instanceof AffiliatedCard affiliatedCard)
            jsonGenerator.writeStringField("affiliation", affiliatedCard.getAffiliation().name());
        if (physicalCard instanceof PhysicalShipCard shipCard) {
            if (shipCard.isDocked())
                jsonGenerator.writeNumberField("dockedAtCardId", shipCard.getDockedAtCard().getCardId());
            jsonGenerator.writeNumberField("rangeAvailable", shipCard.getRangeAvailable());
        }


        jsonGenerator.writeEndObject();
    }
}