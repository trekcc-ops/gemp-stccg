package com.gempukku.stccg.game;

import com.gempukku.stccg.gamestate.GameEvent;

import java.util.Map;

public interface ParticipantCommunicationVisitor {
    void visitChannelNumber(int channelNumber);

    void visitClock(Map<String, Integer> secondsLeft);

    void visitGameEvents(GameCommunicationChannel channel);
}
