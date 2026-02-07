package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;

public class LastTribblePlayedCondition implements Condition {

    @JsonProperty("value")
    private final int _value;

    public LastTribblePlayedCondition(int value) {
        _value = value;
    }
    @Override
    public boolean isFulfilled(DefaultGame cardGame) {
        if (cardGame instanceof TribblesGame tribblesGame) {
            return tribblesGame.getGameState().getLastTribblePlayed() == _value;
        } else {
            return false;
        }
    }
}