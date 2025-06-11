package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.turn.PlayOutOptionalResponsesAction;
import com.gempukku.stccg.actions.turn.PlayOutRequiredResponsesAction;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.*;

public class ActionResult {
    private final Set<Action> _optionalTriggersUsed = new HashSet<>();

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
    private Map<Player, List<TopLevelSelectableAction>> _optionalAfterTriggerActions = new HashMap<>();
        // TODO - In general this isn't doing a great job of assessing who actually performed the action
    protected final String _performingPlayerId;
    private boolean _initialized;

    private final Stack<Action> _responseActionsToStack = new Stack<>();
    private Action _nextAction;
    private ActionOrder _optionalResponsePlayerOrder;
    private int _passCount;


    public ActionResult(Type type, String performingPlayerId) {
        _type = type;
        _performingPlayerId = performingPlayerId;
    }


    public ActionResult(Type type, Action action) {
        _type = type;
        _performingPlayerId = action.getPerformingPlayerId();
    }


    public Type getType() {
        return _type;
    }
    public void optionalTriggerUsed(Action action) {
        _optionalTriggersUsed.add(action);
    }
    public boolean wasOptionalTriggerUsed(Action action) {
        return _optionalTriggersUsed.contains(action);
    }

    public List<TopLevelSelectableAction> getOptionalAfterTriggerActions(DefaultGame cardGame, Player player) {
        if (_optionalAfterTriggerActions.get(player) == null)
            return new LinkedList<>();
        else {
            List<TopLevelSelectableAction> result = new LinkedList<>();
            for (TopLevelSelectableAction action : _optionalAfterTriggerActions.get(player)) {
                if (action.canBeInitiated(cardGame)) {
                    result.add(action);
                }
            }
            return result;
        }
    }

    public void createOptionalAfterTriggerActions(DefaultGame game) throws PlayerNotFoundException {
        Map<Player, List<TopLevelSelectableAction>> allActions = new HashMap<>();
        for (Player player : game.getPlayers()) {
            List<TopLevelSelectableAction> playerActions = new LinkedList<>();
            for (PhysicalCard card : Filters.filterActive(game)) { // cards in play
                if (!card.hasTextRemoved(game)) {
                    final List<TopLevelSelectableAction> actions =
                            card.getOptionalAfterTriggerActions(player, this);
                    if (actions != null)
                        playerActions.addAll(actions);
                }
            }
            for (PhysicalCard card : player.getCardsInHand()) {
                final List<TopLevelSelectableAction> actions = card.getOptionalResponseActionsWhileInHand(player, this);
                if (actions != null)
                    playerActions.addAll(actions);
            }
            allActions.put(player, playerActions);
        }
        _optionalAfterTriggerActions = allActions;
    }


    public String getPerformingPlayerId() { return _performingPlayerId; }

    public List<TopLevelSelectableAction> getRequiredResponseActions(DefaultGame cardGame) {
        return cardGame.getActionsEnvironment().getRequiredAfterTriggers(this);
    }

    public Action nextAction(DefaultGame cardGame) throws PlayerNotFoundException {
        if (!_initialized) {
            _initialized = true;
            createOptionalAfterTriggerActions(cardGame);
            List<TopLevelSelectableAction> requiredResponses = getRequiredResponseActions(cardGame);
            if (!requiredResponses.isEmpty()) {
                return new PlayOutRequiredResponsesAction(cardGame, this, requiredResponses);
            } else {
                _optionalResponsePlayerOrder = cardGame.getRules().getPlayerOrderForActionResponse(this, cardGame);
                _passCount = 0;
                return getResponseActionShell(cardGame);
            }
        } else {
            Action nextAction = _nextAction;
            _nextAction = null;
            return nextAction;
        }
    }

    public void addNextAction(Action action) {
        _nextAction = action;
    }

    public String getNextRespondingPlayer() {
        return _optionalResponsePlayerOrder.getNextPlayer();
    }

    public int getRespondingPlayerCount() {
        return _optionalResponsePlayerOrder.getPlayerCount();
    }

    public int getPassCount() {
        return _passCount;
    }

    public void setPassCount(int passCount) {
        _passCount = passCount;
    }

    public void incrementPassCount() {
        _passCount++;
    }

    public Map<TopLevelSelectableAction, ActionResult> getOptionalAfterTriggers(
            DefaultGame cardGame, Player activePlayer) {
        return cardGame.getActionsEnvironment().getOptionalAfterTriggers(cardGame, activePlayer, List.of(this));
    }

    public List<TopLevelSelectableAction> getOptionalAfterActions(DefaultGame cardGame, Player activePlayer) {
        Map<TopLevelSelectableAction, ActionResult> optionalAfterTriggers =
                getOptionalAfterTriggers(cardGame, activePlayer);
        List<TopLevelSelectableAction> possibleActions = new LinkedList<>(optionalAfterTriggers.keySet());
        possibleActions.addAll(
                cardGame.getActionsEnvironment().getOptionalAfterActions(cardGame, activePlayer, List.of(this))
        );
        return possibleActions;
    }

    public void markActionAsUsed(Action action, DefaultGame cardGame, Player activePlayer) {
        Map<TopLevelSelectableAction, ActionResult> optionalAfterTriggers =
                getOptionalAfterTriggers(cardGame, activePlayer);
        if (optionalAfterTriggers.containsKey(action))
            optionalAfterTriggers.get(action).optionalTriggerUsed(action);
    }
    
    public Action getNextOptionalResponseAction(DefaultGame cardGame) throws PlayerNotFoundException {
        final ActionResult thisResult = this;
        final String activePlayerName = getNextRespondingPlayer();
        Player activePlayer = cardGame.getPlayer(activePlayerName);

        List<TopLevelSelectableAction> possibleActions = getOptionalAfterActions(cardGame, activePlayer);

        if (possibleActions.isEmpty()) {
            incrementPassCount();
            if (getPassCount() < getRespondingPlayerCount()) {
                addNextAction(new PlayOutOptionalResponsesAction(cardGame, thisResult));
            }
        } else {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(activePlayer, DecisionContext.SELECT_OPTIONAL_RESPONSE_ACTION,
                            possibleActions, cardGame) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            try {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    cardGame.getActionsEnvironment().addActionToStack(action);
                                    markActionAsUsed(action, cardGame, activePlayer);
                                    setPassCount(0);
                                } else {
                                    incrementPassCount();
                                }
                                if (getPassCount() < getRespondingPlayerCount()) {
                                    addNextAction(new PlayOutOptionalResponsesAction(cardGame, thisResult));
                                }
                            } catch(InvalidGameLogicException exp) {
                                throw new DecisionResultInvalidException(exp.getMessage());
                            }
                        }
                    });
        }
        return null;
    }

    private Action getResponseActionShell(DefaultGame cardGame) {
        return new SystemQueueAction(cardGame) {
            @Override
            public Action nextAction(DefaultGame cardGame) throws PlayerNotFoundException {
                setAsSuccessful();
                return getNextOptionalResponseAction(cardGame);
            }
        };
    }

}