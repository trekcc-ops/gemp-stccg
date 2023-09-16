package com.gempukku.lotro.game;

import com.gempukku.lotro.gamestate.GameEvent;

import java.util.Map;

public interface ParticipantCommunicationVisitor {
    void visitChannelNumber(int channelNumber);

    void visitClock(Map<String, Integer> secondsLeft);

    void visitGameEvent(GameEvent gameEvent);
}
