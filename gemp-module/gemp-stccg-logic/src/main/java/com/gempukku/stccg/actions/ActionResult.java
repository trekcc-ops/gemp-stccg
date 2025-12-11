package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.ActionProxy;
import com.gempukku.stccg.gamestate.ActionsEnvironment;
import com.gempukku.stccg.player.Player;

import java.util.*;

public class ActionResult {

    public enum Type {
        ACTIVATE,
        ACTIVATE_TRIBBLE_POWER,
        DRAW_CARD_OR_PUT_INTO_HAND,
        END_OF_TURN,
        FOR_EACH_DISCARDED_FROM_DECK,
        FOR_EACH_DISCARDED_FROM_HAND,
        FOR_EACH_DISCARDED_FROM_PLAY,
        FOR_EACH_DISCARDED_FROM_PLAY_PILE,
        FOR_EACH_RETURNED_TO_HAND,
        FOR_EACH_REVEALED_FROM_HAND,
        FOR_EACH_REVEALED_FROM_TOP_OF_DECK,
        PLAY_CARD,
        PLAY_CARD_INITIATION,
        PLAYER_WENT_OUT,
        START_OF_MISSION_ATTEMPT,
        START_OF_PHASE,
        START_OF_TURN,
        DRAW_CARD, KILL_CARD, WHEN_MOVE_FROM
    }

    private final Type _type;
    private final Set<Integer> _triggerActionIdsUsed = new HashSet<>();

    // Actions that can be initiated as optional responses. The key of this map is player name.
    private final Map<String, List<TopLevelSelectableAction>> _optionalAfterTriggerActions = new HashMap<>();

    // TODO - In general this isn't doing a great job of assessing who actually performed the action
    protected final String _performingPlayerId;
    private boolean _initialized;
    private ActionOrder _optionalResponsePlayerOrder;
    private final List<TopLevelSelectableAction> _requiredResponses = new ArrayList<>();
    private int _passCount;
    protected final Action _action;


    public ActionResult(Type type, String performingPlayerId, Action action) {
        _type = type;
        _performingPlayerId = performingPlayerId;
        _action = action;
    }



    public ActionResult(Type type, Action action) {
        _type = type;
        _action = action;
        _performingPlayerId = action.getPerformingPlayerId();
    }

    public void initialize(DefaultGame cardGame) {
        if (!_initialized) {
            _initialized = true;
            createOptionalAfterTriggerActions(cardGame);
            _requiredResponses.addAll(getRequiredResponseActions(cardGame));
            _optionalResponsePlayerOrder =
                    cardGame.getRules().getPlayerOrderForActionResponse(this, cardGame);
            _passCount = 0;
        }
    }

    public Type getType() {
        return _type;
    }
    public void optionalTriggerUsed(Action action) {
        _triggerActionIdsUsed.add(action.getActionId());
    }
    public boolean wasOptionalTriggerUsed(Action action) {
        return _triggerActionIdsUsed.contains(action.getActionId());
    }

    public void createOptionalAfterTriggerActions(DefaultGame game) {
        for (Player player : game.getPlayers()) {
            String playerName = player.getPlayerId();
            List<TopLevelSelectableAction> playerActions = new LinkedList<>();
            for (PhysicalCard card : game.getAllCardsInPlay()) {
                playerActions.addAll(card.getOptionalResponseActionsWhileInPlay(game, player));
            }
            for (PhysicalCard card : player.getCardsInHand()) {
                playerActions.addAll(card.getOptionalResponseActionsWhileInHand(game, player, this));
            }
            _optionalAfterTriggerActions.put(playerName, playerActions);
        }
    }


    public String getPerformingPlayerId() { return _performingPlayerId; }

    public List<TopLevelSelectableAction> getRequiredResponseActions(DefaultGame cardGame) {
        List<TopLevelSelectableAction> gatheredActions = new LinkedList<>();
        for (ActionProxy actionProxy : cardGame.getAllActionProxies()) {
            List<TopLevelSelectableAction> actions = actionProxy.getRequiredAfterTriggers(cardGame, this);
            if (actions != null) {
                gatheredActions.addAll(actions);
            }
        }
        return gatheredActions;
    }

    public boolean canBeRespondedTo() {
        return !_requiredResponses.isEmpty() || _passCount < _optionalResponsePlayerOrder.getPlayerCount();
    }


    public List<TopLevelSelectableAction> getOptionalAfterActions(DefaultGame cardGame, String playerName) {
        List<TopLevelSelectableAction> result = new LinkedList<>();
        if (_optionalAfterTriggerActions.get(playerName) != null) {
            for (TopLevelSelectableAction action : _optionalAfterTriggerActions.get(playerName)) {
                if (action.canBeInitiated(cardGame) && !wasOptionalTriggerUsed(action)) {
                    result.add(action);
                }
            }
        }
        for (ActionProxy actionProxy : cardGame.getAllActionProxies()) {
            actionProxy.getOptionalAfterActions(cardGame, playerName, this).forEach(action -> {
                if (action.canBeInitiated(cardGame)) result.add(action);
            });
        }
        return result;
    }


    private AwaitingDecision selectOptionalResponseActionDecision(DefaultGame cardGame,
                                                                  List<TopLevelSelectableAction> possibleActions,
                                                                  String activePlayerName) {
        return new ActionSelectionDecision(activePlayerName, DecisionContext.SELECT_OPTIONAL_RESPONSE_ACTION,
                possibleActions, cardGame, false) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                TopLevelSelectableAction action = getSelectedAction(result);
                if (action != null) {
                    _passCount = 0;
                    cardGame.getActionsEnvironment().addActionToStack(action);
                    _triggerActionIdsUsed.add(action.getActionId());
                } else {
                    _passCount++;
                }
            }
        };
    }

    private void refreshActions(DefaultGame cardGame) {
        for (List<TopLevelSelectableAction> optionalActions : _optionalAfterTriggerActions.values()) {
            optionalActions.removeIf(action -> !action.canBeInitiated(cardGame) || action.wasInitiated());
        }
        _requiredResponses.removeIf(nextAction -> !nextAction.canBeInitiated(cardGame) || nextAction.wasInitiated());
    }


    public void addNextActionToStack(DefaultGame cardGame, Action parentAction) {
        refreshActions(cardGame);
        if (!_requiredResponses.isEmpty()) {
            ActionsEnvironment environment = cardGame.getActionsEnvironment();
            if (_requiredResponses.size() == 1 && _requiredResponses.getFirst().canBeInitiated(cardGame)) {
                environment.addActionToStack(_requiredResponses.getFirst());
            } else {
                cardGame.sendAwaitingDecision(
                        new ActionSelectionDecision(cardGame.getCurrentPlayerId(),
                                DecisionContext.SELECT_REQUIRED_RESPONSE_ACTION, _requiredResponses, cardGame, true) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                environment.addActionToStack(action);
                                _requiredResponses.remove(action);
                            }
                        });
            }
        } else if (_passCount < _optionalResponsePlayerOrder.getPlayerCount()) {
            _optionalResponsePlayerOrder.advancePlayer();
            final String activePlayerName = _optionalResponsePlayerOrder.getCurrentPlayerName();
            List<TopLevelSelectableAction> possibleActions = getOptionalAfterActions(cardGame, activePlayerName);
            if (possibleActions.isEmpty()) {
                _passCount++;
            } else {
                cardGame.sendAwaitingDecision(
                        selectOptionalResponseActionDecision(cardGame, possibleActions, activePlayerName));
            }
        } else {
            parentAction.clearResult();
        }
    }

    public Action getAction() {
        return _action;
    }

}