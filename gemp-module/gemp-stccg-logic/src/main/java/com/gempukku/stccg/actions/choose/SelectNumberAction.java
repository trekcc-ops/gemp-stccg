package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.IntegerAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;

public class SelectNumberAction extends MakeDecisionAction {

    private final int _minimum;
    private final int _maximum;
    private final ActionContext _actionContext;
    private final String _memoryId;

    public SelectNumberAction(ActionContext context, String choiceText, ValueSource valueSource, String memoryId) {
        super(context.getGame(), context.getPerformingPlayer(), choiceText);
        _minimum = valueSource.getMinimum(context);
        _maximum = valueSource.getMaximum(context);
        _actionContext = context;
        _memoryId = memoryId;
    }

    @Override
    protected AwaitingDecision getDecision(DefaultGame cardGame) throws PlayerNotFoundException {
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        return new IntegerAwaitingDecision(performingPlayer, _choiceText, _minimum, _maximum, cardGame) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                _actionContext.setValueToMemory(_memoryId, String.valueOf(getValidatedResult(result)));
                setAsSuccessful();
            }

        };
    }

}