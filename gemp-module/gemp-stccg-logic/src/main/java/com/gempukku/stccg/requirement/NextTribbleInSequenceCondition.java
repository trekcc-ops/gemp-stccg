package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;

public class NextTribbleInSequenceCondition implements Condition {

    @JsonProperty("nextTribbleInSequence")
    private final int _value;

    public NextTribbleInSequenceCondition(int value) {
        _value = value;
    }
    @Override
    public boolean isFulfilled(DefaultGame cardGame) {
        if (cardGame instanceof TribblesGame tribblesGame) {
            return tribblesGame.getGameState().getNextTribbleInSequence() == _value;
        } else {
            return false;
        }
    }
}