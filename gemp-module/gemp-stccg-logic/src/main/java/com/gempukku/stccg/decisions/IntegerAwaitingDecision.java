package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;

public abstract class IntegerAwaitingDecision extends AbstractAwaitingDecision {

    @JsonProperty("min")
    private final int _min;

    @JsonProperty("max")
    private final int _max;

    public IntegerAwaitingDecision(String performingPlayerName, DecisionContext context, int min, int max,
                                   DefaultGame cardGame) {
        super(performingPlayerName, context, cardGame);
        _min = min;
        _max = max;
    }


    protected int getValidatedResult(String result) throws DecisionResultInvalidException {
        try {
            int value = Integer.parseInt(result);
            if (_min > value || _max < value)
                throw new DecisionResultInvalidException();
            return value;
        } catch (NumberFormatException exp) {
            throw new DecisionResultInvalidException();
        }
    }

    public String getElementType() { return "INTEGER"; }

}