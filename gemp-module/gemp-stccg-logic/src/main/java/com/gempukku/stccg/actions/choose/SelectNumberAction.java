package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.IntegerAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class SelectNumberAction extends MakeDecisionAction {

    private final int _minimum;
    private final int _maximum;
    private final ActionContext _actionContext;
    private final String _memoryId;

    public SelectNumberAction(DefaultGame cardGame, ActionContext context, String choiceText, ValueSource valueSource,
                              String memoryId) throws InvalidGameLogicException {
        super(cardGame, context.getPerformingPlayerId(), choiceText);
        _minimum = (int) valueSource.getMinimum(cardGame, context);
        _maximum = (int) valueSource.getMaximum(cardGame, context);
        _actionContext = context;
        _memoryId = memoryId;
    }

    @Override
    protected AwaitingDecision getDecision(DefaultGame cardGame) throws PlayerNotFoundException {
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        return new IntegerAwaitingDecision(performingPlayer, DecisionContext.SELECT_NUMBER, _minimum, _maximum, cardGame) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                _actionContext.setValueToMemory(_memoryId, String.valueOf(getValidatedResult(result)));
                setAsSuccessful();
            }

        };
    }

}