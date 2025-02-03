package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class UpdateTribbleSequenceGameEvent extends GameEvent {

    @JacksonXmlProperty(localName = "message", isAttribute = true)
    private final String _tribbleSequence;

    public UpdateTribbleSequenceGameEvent(String tribbleSequence) {
        super(Type.TRIBBLE_SEQUENCE_UPDATE);
        _tribbleSequence = tribbleSequence;
        _eventAttributes.put(Attribute.message, tribbleSequence);
    }

}