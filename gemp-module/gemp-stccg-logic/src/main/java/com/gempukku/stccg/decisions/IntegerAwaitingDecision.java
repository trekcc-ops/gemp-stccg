package com.gempukku.stccg.decisions;

import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

public abstract class IntegerAwaitingDecision extends AbstractAwaitingDecision {
    private final int _min;
    private final int _max;

    public IntegerAwaitingDecision(Player performingPlayer, String choiceText, int min, int max,
                                   DefaultGame cardGame) {
        super(performingPlayer, choiceText, AwaitingDecisionType.INTEGER, cardGame);
        _min = min;
        _max = max;
        setParam("min", _min);
        setParam("max", _max);
    }


    public void setDefaultValue(int defaultValue) {
        setParam("defaultValue", defaultValue);
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

}