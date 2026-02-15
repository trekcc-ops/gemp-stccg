package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.LinkedList;

public abstract class ActionWithSubActions extends ActionyAction {

    protected final LinkedList<Action> _actionEffects = new LinkedList<>();
    protected final LinkedList<Action> _processedActions = new LinkedList<>();

    public ActionWithSubActions(int actionId, ActionType actionType, String performingPlayerName,
                                ActionStatus status) {
        super(actionId, actionType, performingPlayerName, status);
    }

    public ActionWithSubActions(DefaultGame cardGame, String performingPlayerId, ActionType actionType,
                                ActionContext context) {
        super(cardGame, performingPlayerId, actionType, context);
    }

    public ActionWithSubActions(DefaultGame cardGame, String performingPlayerId, ActionType actionType) {
        super(cardGame, performingPlayerId, actionType);
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        if (!_actionEffects.isEmpty()) {
            Action subAction = _actionEffects.getFirst();
            if (subAction.wasSuccessful()) {
                _actionEffects.remove(subAction);
                _processedActions.add(subAction);
            } else if (subAction.wasFailed()) {
                this.setAsFailed();
            } else {
                cardGame.getActionsEnvironment().addActionToStack(subAction);
            }
        } else {
            setAsSuccessful();
        }
    }

    public final void appendEffect(Action action) {
        _actionEffects.add(action);
    }

    public final void insertActions(Collection<Action> actions) {
        _actionEffects.addAll(0, actions);
    }

    public abstract PhysicalCard getPerformingCard();

}