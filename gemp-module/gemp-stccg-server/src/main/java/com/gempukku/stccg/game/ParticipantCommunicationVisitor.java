package com.gempukku.stccg.game;

import java.util.Map;

public interface ParticipantCommunicationVisitor {
    void visitChannelNumber(int channelNumber);

    void visitClock(Map<String, Integer> secondsLeft);

    void visitGameEvents(GameCommunicationChannel channel);
}