package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class AndCondition implements Condition {

    @JsonProperty("conditions")
    private final Collection<Condition> _conditions;

    public AndCondition(Collection<Condition> conditions) {
        _conditions = conditions;
    }
    @Override
    public boolean isFulfilled(DefaultGame cardGame) {
        for (Condition condition : _conditions) {
            if (!condition.isFulfilled(cardGame)) {
                return false;
            }
        }
        return true;
    }
}