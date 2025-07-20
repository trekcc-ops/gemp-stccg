package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.responses.CardSelectionDecisionResponse;
import com.gempukku.stccg.decisions.responses.DecisionResponse;
import com.gempukku.stccg.decisions.responses.IntegerSelectionDecisionResponse;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class IntegerAwaitingDecision extends AbstractAwaitingDecision {

    @JsonProperty("min")
    private final int _min;

    @JsonProperty("max")
    private final int _max;

    private boolean _responded;
    protected int _selectedValue;

    public IntegerAwaitingDecision(Player performingPlayer, DecisionContext context, int min, int max,
                                   DefaultGame cardGame) {
        super(performingPlayer, context, cardGame);
        _min = min;
        _max = max;
    }

    public void setDecisionResponse(DefaultGame cardGame, DecisionResponse response) throws DecisionResultInvalidException {
        if (_responded) {
            throw new DecisionResultInvalidException("Trying to set response for an already-completed decision");
        } else if (response instanceof IntegerSelectionDecisionResponse integerResponse) {
            Integer responseValue = integerResponse.getSelectedValue();
            if (responseValue == null) {
                throw new DecisionResultInvalidException("Did not receive a valid integer as a response to decision");
            }
            if (responseValue < _min || responseValue > _max) {
                throw new DecisionResultInvalidException("Received an integer response outside the acceptable range");
            }
            _selectedValue = responseValue;
            _responded = true;
        }
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