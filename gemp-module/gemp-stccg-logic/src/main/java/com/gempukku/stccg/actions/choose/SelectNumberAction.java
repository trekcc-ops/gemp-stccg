package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.IntegerAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class SelectNumberAction extends MakeDecisionAction {

    private final int _minimum;
    private final int _maximum;
    private final ActionContext _actionContext;
    private final String _memoryId;

    public SelectNumberAction(ActionContext context, String choiceText, ValueSource valueSource, String memoryId) {
        super(context.getGame(), context.getPerformingPlayer(), choiceText);
        _minimum = (int) valueSource.getMinimum(context);
        _maximum = (int) valueSource.getMaximum(context);
        _actionContext = context;
        _memoryId = memoryId;
    }

    @Override
    protected AwaitingDecision getDecision(DefaultGame cardGame) throws PlayerNotFoundException {
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        return new IntegerAwaitingDecision(performingPlayer, DecisionContext.SELECT_NUMBER, _minimum, _maximum, cardGame) {
            @Override
            public void followUp() {
                _actionContext.setValueToMemory(_memoryId, String.valueOf(_selectedValue));
                setAsSuccessful();
            }
        };
    }

}