package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class ActionResult {

    @JsonProperty("type")
    private final ActionResultType _type;
    private final Set<Integer> _triggerActionIdsUsed = new HashSet<>();

    // Actions that can be initiated as optional responses. The key of this map is player name.
    private final Map<String, List<Action>> _optionalAfterTriggerActions = new HashMap<>();

    protected final String _performingPlayerId;
    private boolean _initialized;
    private ActionOrder _optionalResponsePlayerOrder;
    private final List<Action> _requiredResponses = new ArrayList<>();
    private int _passCount;
    protected final Action _action;

    private final ZonedDateTime _timestamp;

    @JsonProperty("resultId")
    private final int _resultId;

    public ActionResult(DefaultGame cardGame, ActionResultType type, String performingPlayerId, Action action) {
        _type = type;
        _performingPlayerId = performingPlayerId;
        _action = action;
        _passCount = 0;
        _timestamp = ZonedDateTime.now(ZoneId.of("UTC"));
        _resultId = cardGame.getActionsEnvironment().getNextResultIdAndIncrement();
        cardGame.getActionsEnvironment().logActionResult(this);
    }

    public ActionResult(DefaultGame cardGame, ActionResultType type, Action action) {
        this(cardGame, type, action.getPerformingPlayerId(), action);
    }

    public void initialize(DefaultGame cardGame) {
        if (!_initialized) {
            _initialized = true;
            createOptionalAfterTriggerActions(cardGame);
            _requiredResponses.addAll(getRequiredResponseActions(cardGame));
            _optionalResponsePlayerOrder = cardGame.getRules().getPlayerOrderForActionResponse(this, cardGame);
        }
    }

    public boolean hasType(ActionResultType type) {
        return _type == type;
    }
    public boolean hasAnyType(List<ActionResultType> types) {
        return types.contains(_type);
    }

    public void createOptionalAfterTriggerActions(DefaultGame game) {
        for (Player player : game.getPlayers()) {
            String playerName = player.getPlayerId();
            List<Action> playerActions = new LinkedList<>();
            for (PhysicalCard card : game.getAllCardsInPlay()) {
                playerActions.addAll(card.getOptionalResponseActionsWhileInPlay(game, player));
            }
            for (PhysicalCard card : player.getCardsInHand()) {
                playerActions.addAll(card.getOptionalResponseActionsWhileInHand(game, player));
            }
            _optionalAfterTriggerActions.put(playerName, playerActions);
        }
    }


    @JsonProperty("performingPlayerId")
    public String getPerformingPlayerId() { return _performingPlayerId; }

    public List<Action> getRequiredResponseActions(DefaultGame cardGame) {
        List<Action> gatheredActions = new LinkedList<>();
        for (ActionProxy actionProxy : cardGame.getAllActionProxies()) {
            List<Action> actions = actionProxy.getRequiredAfterTriggers(cardGame, this);
            if (actions != null) {
                gatheredActions.addAll(actions);
            }
        }
        return gatheredActions;
    }

    @JsonIgnore
    public boolean canBeRespondedTo() {
        return !_requiredResponses.isEmpty() || _passCount < _optionalResponsePlayerOrder.getPlayerCount();
    }


    public List<Action> getOptionalAfterActions(DefaultGame cardGame, String playerName) {
        List<Action> result = new LinkedList<>();
        if (_optionalAfterTriggerActions.get(playerName) != null) {
            for (Action action : _optionalAfterTriggerActions.get(playerName)) {
                if (action.canBeInitiated(cardGame) && !_triggerActionIdsUsed.contains(action.getActionId())) {
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


    private AwaitingDecision selectOptionalResponseActionDecision(DefaultGame cardGame, List<Action> possibleActions,
                                                                  String activePlayerName) {
        return new ActionSelectionDecision(activePlayerName, DecisionContext.SELECT_OPTIONAL_RESPONSE_ACTION,
                possibleActions, cardGame, false) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                Action action = getSelectedAction(result);
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
        for (List<Action> optionalActions : _optionalAfterTriggerActions.values()) {
            optionalActions.removeIf(action -> !action.canBeInitiated(cardGame));
        }
        _requiredResponses.removeIf(nextAction -> !nextAction.canBeInitiated(cardGame));
    }


    public void addNextActionToStack(DefaultGame cardGame) {
        refreshActions(cardGame);
        if (!_requiredResponses.isEmpty()) {
            ActionsEnvironment environment = cardGame.getActionsEnvironment();
            if (_requiredResponses.size() == 1 && _requiredResponses.getFirst().canBeInitiated(cardGame)) {
                cardGame.addActionToStack(_requiredResponses.getFirst());
            } else {
                String currentPlayerName = cardGame.getCurrentPlayerId();
                cardGame.sendAwaitingDecision(
                        new ActionSelectionDecision(currentPlayerName, DecisionContext.SELECT_REQUIRED_RESPONSE_ACTION,
                                _requiredResponses, cardGame, true) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                environment.addActionToStack(action);
                                _requiredResponses.remove(action);
                            }
                        });
            }
        } else {
            _optionalResponsePlayerOrder.advancePlayer();
            final String activePlayerName = _optionalResponsePlayerOrder.getCurrentPlayerName();
            List<Action> possibleActions = getOptionalAfterActions(cardGame, activePlayerName);
            if (possibleActions.isEmpty()) {
                _passCount++;
            } else {
                cardGame.sendAwaitingDecision(
                        selectOptionalResponseActionDecision(cardGame, possibleActions, activePlayerName));
            }
        }
    }

    @JsonIgnore
    public Action getAction() {
        return _action;
    }

    @JsonProperty("timestamp")
    public String getTimestamp() {
        return _timestamp.toString();
    }

}