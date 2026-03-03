package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.blueprints.SubActionBlueprint;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.LinkedList;

public abstract class ActionWithSubActions extends ActionyAction {

    protected final LinkedList<SubActionBlueprint> _queuedSubActions = new LinkedList<>();
    protected Action _currentSubAction;
    protected final LinkedList<Action> _processedSubActions = new LinkedList<>();

    public ActionWithSubActions(int actionId, ActionType actionType, String performingPlayerName,
                                ActionStatus status) {
        super(actionId, actionType, performingPlayerName, status);
    }

    public ActionWithSubActions(DefaultGame cardGame, String performingPlayerId, ActionType actionType,
                                GameTextContext context) {
        super(cardGame, performingPlayerId, actionType, context);
    }

    public ActionWithSubActions(DefaultGame cardGame, String performingPlayerId, ActionType actionType) {
        super(cardGame, actionType, performingPlayerId, null);
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        if (_currentSubAction == null && !_queuedSubActions.isEmpty()) {
            try {
                SubActionBlueprint nextSubAction = _queuedSubActions.getFirst();
                _queuedSubActions.removeFirst();
                if (nextSubAction.isPlayableInFull(cardGame, _actionContext)) {
                    _currentSubAction = nextSubAction.createAction(cardGame, this, _actionContext);
                    cardGame.getActionsEnvironment().addActionToStack(_currentSubAction);
                } else {
                    setAsFailed();
                }
            } catch(Exception exp) {
                cardGame.sendErrorMessage(exp);
                setAsFailed();
            }
        } else if (_currentSubAction != null && _currentSubAction.wasSuccessful()) {
            _processedSubActions.add(_currentSubAction);
            _currentSubAction = null;
        } else if (_currentSubAction != null && _currentSubAction.wasFailed()) {
            _processedSubActions.add(_currentSubAction);
            _currentSubAction = null;
            setAsFailed();
        } else {
            setAsSuccessful();
        }
    }

    public final void appendSubAction(SubActionBlueprint subAction) {
        _queuedSubActions.add(subAction);
    }


    public abstract PhysicalCard getPerformingCard();

    public void insertSubActions(Collection<SubActionBlueprint> subActions) {
        _queuedSubActions.addAll(0, subActions);
    }

    public void removeRemainingSubActions() {
        _queuedSubActions.clear();
    }

}