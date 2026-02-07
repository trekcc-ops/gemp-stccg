package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;

public abstract class MakeDecisionAction extends ActionyAction {
    protected final String _choiceText;

    public MakeDecisionAction(DefaultGame cardGame, String performingPlayerName, String choiceText) {
        super(cardGame, performingPlayerName, ActionType.MAKE_DECISION);
        _choiceText = choiceText;
    }

    public MakeDecisionAction(DefaultGame cardGame, String performingPlayerName, String choiceText,
                              ActionContext context) {
        super(cardGame, performingPlayerName, ActionType.MAKE_DECISION, context);
        _choiceText = choiceText;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    protected abstract AwaitingDecision getDecision(DefaultGame cardGame);

    protected void processEffect(DefaultGame cardGame) {
        AwaitingDecision decision = getDecision(cardGame);
        cardGame.sendAwaitingDecision(decision);
        setAsSuccessful();
    }

}