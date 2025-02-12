package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateTribbleSequenceGameEvent extends GameEvent {

    @JsonProperty("tribbleSequence")
    private final String _tribbleSequence;

    public UpdateTribbleSequenceGameEvent(String tribbleSequence) {
        super(Type.TRIBBLE_SEQUENCE_UPDATE);
        _tribbleSequence = tribbleSequence;
        _eventAttributes.put(Attribute.message, tribbleSequence);
    }

}