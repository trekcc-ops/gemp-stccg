package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.blueprints.Blueprint109_063;
import com.gempukku.stccg.cards.blueprints.Blueprint212_019;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.RequiredType;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
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
    private Map<String, List<TopLevelSelectableAction>> _optionalAfterTriggerActions = new HashMap<>();
    // TODO - In general this isn't doing a great job of assessing who actually performed the action
    protected final String _performingPlayerId;
    private boolean _initialized;
    private ActionOrder _optionalResponsePlayerOrder;
    private final List<TopLevelSelectableAction> _requiredResponses = new ArrayList<>();
    private int _passCount;
    private final Action _action;


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


    public Type getType() {
        return _type;
    }
    public void optionalTriggerUsed(Action action) {
        _triggerActionIdsUsed.add(action.getActionId());
    }
    public boolean wasOptionalTriggerUsed(Action action) {
        return _triggerActionIdsUsed.contains(action.getActionId());
    }

    public List<TopLevelSelectableAction> getOptionalAfterTriggerActions(DefaultGame cardGame, String playerName) {
        List<TopLevelSelectableAction> result = new LinkedList<>();
        if (_optionalAfterTriggerActions.get(playerName) != null) {
            for (TopLevelSelectableAction action : _optionalAfterTriggerActions.get(playerName)) {
                if (action.canBeInitiated(cardGame)) {
                    result.add(action);
                }
            }
        }
        return result;
    }


    public void createOptionalAfterTriggerActions(DefaultGame game) {
        Map<String, List<TopLevelSelectableAction>> allActions = new HashMap<>();
        for (Player player : game.getPlayers()) {
            List<TopLevelSelectableAction> playerActions = new LinkedList<>();
            for (PhysicalCard card : Filters.filterCardsInPlay(game)) {
                if (!card.hasTextRemoved(game)) {
                    final List<TopLevelSelectableAction> actions =
                            getOptionalAfterTriggerActions(game, card, player);
                    if (actions != null)
                        playerActions.addAll(actions);
                }
            }
            for (PhysicalCard card : player.getCardsInHand()) {
                final List<TopLevelSelectableAction> actions =
                        card.getOptionalResponseActionsWhileInHand(game, player, this);
                if (actions != null)
                    playerActions.addAll(actions);
            }
            allActions.put(player.getPlayerId(), playerActions);
        }
        _optionalAfterTriggerActions = allActions;
    }

    private List<TopLevelSelectableAction> getOptionalAfterTriggerActions(DefaultGame cardGame,
                                                                          PhysicalCard card, Player player) {
        CardBlueprint blueprint = card.getBlueprint();
        return switch (blueprint) {
            case Blueprint212_019 riskBlueprint ->
                    riskBlueprint.getValidResponses(card, player, this, cardGame);
            case Blueprint109_063 missionSpecBlueprint ->
                    missionSpecBlueprint.getValidResponses(card, player, this, cardGame);
            case null, default -> {
                // Pull trigger actions defined in JSON files
                assert blueprint != null;
                List<TopLevelSelectableAction> result = new LinkedList<>();
                blueprint.getTriggers(RequiredType.OPTIONAL).forEach(actionSource -> {
                    if (actionSource != null) {
                        TopLevelSelectableAction action =
                                actionSource.createAction(cardGame, player.getPlayerId(), card, this);
                        if (action != null) result.add(action);
                    }
                });
                yield result;
            }
        };
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

    public void initialize(DefaultGame cardGame) {
        if (!_initialized) {
            _initialized = true;
            createOptionalAfterTriggerActions(cardGame);
            _requiredResponses.addAll(getRequiredResponseActions(cardGame));
            _optionalResponsePlayerOrder = cardGame.getRules().getPlayerOrderForActionResponse(this, cardGame);
            _passCount = 0;
        }
    }

    public boolean canBeRespondedTo() {
        return !_requiredResponses.isEmpty() || _passCount < _optionalResponsePlayerOrder.getPlayerCount();
    }


    public Map<TopLevelSelectableAction, ActionResult> getOptionalAfterTriggers(DefaultGame cardGame,
                                                                                String activePlayerName) {
        final Map<TopLevelSelectableAction, ActionResult> gatheredActions = new HashMap<>();
        List<TopLevelSelectableAction> actions = getOptionalAfterTriggerActions(cardGame, activePlayerName);
        if (actions != null) {
            for (TopLevelSelectableAction action : actions) {
                if (!wasOptionalTriggerUsed(action)) {
                    gatheredActions.put(action, this);
                }
            }
        }
        return gatheredActions;
    }


    public List<TopLevelSelectableAction> getOptionalAfterActions(DefaultGame cardGame, String playerName) {
        Map<TopLevelSelectableAction, ActionResult> optionalAfterTriggers =
                getOptionalAfterTriggers(cardGame, playerName);
        List<TopLevelSelectableAction> result = new LinkedList<>(optionalAfterTriggers.keySet());
        for (ActionProxy actionProxy : cardGame.getAllActionProxies()) {
            for (TopLevelSelectableAction action : actionProxy.getOptionalAfterActions(cardGame, playerName, this)) {
                if (action.canBeInitiated(cardGame)) {
                    result.add(action);
                }
            }
        }
        return result;
    }

    private void markActionAsUsed(TopLevelSelectableAction action, DefaultGame cardGame, String activePlayerName) {
        Map<TopLevelSelectableAction, ActionResult> optionalAfterTriggers =
                getOptionalAfterTriggers(cardGame, activePlayerName);
        if (optionalAfterTriggers.containsKey(action)) {
            optionalAfterTriggers.get(action).optionalTriggerUsed(action);
        }
    }


    private AwaitingDecision selectOptionalResponseActionDecision(DefaultGame cardGame,
                                                                  List<TopLevelSelectableAction> possibleActions,
                                                                  String activePlayerName) {
        return new ActionSelectionDecision(activePlayerName, DecisionContext.SELECT_OPTIONAL_RESPONSE_ACTION,
                possibleActions, cardGame, false) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                try {
                    TopLevelSelectableAction action = getSelectedAction(result);
                    if (action != null) {
                        cardGame.getActionsEnvironment().addActionToStack(action);
                        markActionAsUsed(action, cardGame, activePlayerName);
                        _passCount = 0;
                    } else {
                        _passCount++;
                    }
                } catch(InvalidGameLogicException exp) {
                    throw new DecisionResultInvalidException(exp.getMessage());
                }
            }
        };
    }


    private static boolean areAllActionsTheSame(List<TopLevelSelectableAction> actions) {
        boolean result = true;
        TopLevelSelectableAction firstAction = actions.getFirst();
        if (firstAction.getPerformingCard() == null)
            result = false;
        for (TopLevelSelectableAction action : actions) {
            if (action.getPerformingCard() == null)
                result = false;
            else if (action.getPerformingCard().getBlueprint() != firstAction.getPerformingCard().getBlueprint())
                result = false;
        }
        return result;
    }

    public void addNextActionToStack(DefaultGame cardGame, Action parentAction) throws InvalidGameLogicException {
        if (!_requiredResponses.isEmpty()) {
            ActionsEnvironment environment = cardGame.getActionsEnvironment();
            if (_requiredResponses.size() == 1) {
                environment.addActionToStack(_requiredResponses.getFirst());
            } else if (areAllActionsTheSame(_requiredResponses)) {
                Action anyAction = _requiredResponses.removeFirst();
                environment.addActionToStack(anyAction);
            } else {
                cardGame.sendAwaitingDecision(
                        new ActionSelectionDecision(cardGame.getCurrentPlayerId(),
                                DecisionContext.SELECT_REQUIRED_RESPONSE_ACTION, _requiredResponses, cardGame, true) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                try {
                                    Action action = getSelectedAction(result);
                                    environment.addActionToStack(action);
                                    _requiredResponses.remove(action);
                                } catch(InvalidGameLogicException exp) {
                                    throw new DecisionResultInvalidException(exp.getMessage());
                                }
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