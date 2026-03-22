package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.decisions.IntegerAwaitingDecision;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.game.DefaultGame;

public class SelectNumberAction extends MakeDecisionAction {

    private final int _minimum;
    private final int _maximum;
    private final String _memoryId;

    public SelectNumberAction(DefaultGame cardGame, GameTextContext context, String choiceText, ValueSource valueSource,
                              String memoryId) {
        super(cardGame, context.yourName(), choiceText);
        _minimum = valueSource.getMinimum(cardGame, context);
        _maximum = valueSource.getMaximum(cardGame, context);
        _memoryId = memoryId;
    }

    @Override
    protected AwaitingDecision getDecision(DefaultGame cardGame) {
        return new IntegerAwaitingDecision(_performingPlayerId, DecisionContext.SELECT_NUMBER, _minimum, _maximum, cardGame) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                _actionContext.setValueToMemory(_memoryId, String.valueOf(getValidatedResult(result)));
                setAsSuccessful();
            }

        };
    }

}