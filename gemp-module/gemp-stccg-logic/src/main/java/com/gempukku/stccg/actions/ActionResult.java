package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.ActionsEnvironment;
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
    private Action _nextAction;
    private ActionOrder _optionalResponsePlayerOrder;
    private final List<TopLevelSelectableAction> _requiredResponses = new ArrayList<>();
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

    public void initialize(DefaultGame cardGame) throws PlayerNotFoundException {
        if (!_initialized) {
            _initialized = true;
            createOptionalAfterTriggerActions(cardGame);
            _requiredResponses.addAll(getRequiredResponseActions(cardGame));
            if (!_requiredResponses.isEmpty()) {
                addNextRequiredResponseSystemAction(cardGame);
            } else {
                _optionalResponsePlayerOrder = cardGame.getRules().getPlayerOrderForActionResponse(this, cardGame);
                _passCount = 0;
                addNextOptionalResponseSystemActionIfPassesRemaining(cardGame);
            }
        }
    }

    public boolean canBeRespondedTo() {
        return !_requiredResponses.isEmpty() || _passCount < _optionalResponsePlayerOrder.getPlayerCount();
    }

    public Action nextAction() throws PlayerNotFoundException {
        Action nextAction = _nextAction;
        _nextAction = null;
        return nextAction;
    }

    public void addNextAction(Action action) {
        _nextAction = action;
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

    private void markActionAsUsed(Action action, DefaultGame cardGame, Player activePlayer) {
        Map<TopLevelSelectableAction, ActionResult> optionalAfterTriggers =
                getOptionalAfterTriggers(cardGame, activePlayer);
        if (optionalAfterTriggers.containsKey(action))
            optionalAfterTriggers.get(action).optionalTriggerUsed(action);
    }

    private AwaitingDecision selectOptionalResponseActionDecision(DefaultGame cardGame, List<TopLevelSelectableAction> possibleActions, Player activePlayer) {
        return new CardActionSelectionDecision(activePlayer, DecisionContext.SELECT_OPTIONAL_RESPONSE_ACTION,
                possibleActions, cardGame) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                try {
                    Action action = getSelectedAction(result);
                    if (action != null) {
                        cardGame.getActionsEnvironment().addActionToStack(action);
                        markActionAsUsed(action, cardGame, activePlayer);
                        _passCount = 0;
                    } else {
                        incrementPassCount();
                    }
                    addNextOptionalResponseSystemActionIfPassesRemaining(cardGame);
                } catch(InvalidGameLogicException exp) {
                    throw new DecisionResultInvalidException(exp.getMessage());
                }
            }
        };
    }

    private void addNextOptionalResponseSystemActionIfPassesRemaining(DefaultGame cardGame) {
        if (canBeRespondedTo()) {
            Action nextAction = new SystemQueueAction(cardGame) {
                @Override
                public void processEffect(DefaultGame cardGame) throws PlayerNotFoundException {
                    setAsSuccessful();
                    _optionalResponsePlayerOrder.advancePlayer();
                    final String activePlayerName = _optionalResponsePlayerOrder.getCurrentPlayerName();
                    Player activePlayer = cardGame.getPlayer(activePlayerName);
                    List<TopLevelSelectableAction> possibleActions = getOptionalAfterActions(cardGame, activePlayer);
                    if (possibleActions.isEmpty()) {
                        // increment pass count and prompt the next player with possible actions
                        incrementPassCount();
                        addNextOptionalResponseSystemActionIfPassesRemaining(cardGame);
                    } else {
                        cardGame.getUserFeedback().sendAwaitingDecision(
                                selectOptionalResponseActionDecision(cardGame, possibleActions, activePlayer));
                    }
                }
            };
            addNextAction(nextAction);
        }
    }

    private void addNextRequiredResponseSystemAction(DefaultGame cardGame) {
        Action nextAction = new SystemQueueAction(cardGame) {
            @Override
            public void processEffect(DefaultGame cardGame) throws PlayerNotFoundException, CardNotFoundException, InvalidGameLogicException {
                processRequiredResponseEffect(cardGame);
                setAsSuccessful();
            }
        };
        addNextAction(nextAction);
    }

    private void processRequiredResponseEffect(DefaultGame cardGame)
            throws PlayerNotFoundException, CardNotFoundException, InvalidGameLogicException {
        ActionsEnvironment environment = cardGame.getActionsEnvironment();
        List<TopLevelSelectableAction> _responses = _requiredResponses;
        if (_responses.size() == 1) {
            environment.addActionToStack(_responses.getFirst());
        } else if (areAllActionsTheSame(_responses)) {
            Action anyAction = _responses.removeFirst();
            environment.addActionToStack(anyAction);
            addNextRequiredResponseSystemAction(cardGame);
        } else {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new ActionSelectionDecision(cardGame.getCurrentPlayer(),
                            DecisionContext.SELECT_REQUIRED_RESPONSE_ACTION, _responses, cardGame) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            try {
                                Action action = getSelectedAction(result);
                                environment.addActionToStack(action);
                                _responses.remove(action);
                                addNextRequiredResponseSystemAction(cardGame);
                            } catch(InvalidGameLogicException exp) {
                                throw new DecisionResultInvalidException(exp.getMessage());
                            }
                        }

                        @Override
                        public String[] getCardIds() {
                            return getDecisionParameters().get("cardId");
                        }


                    });
        }
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

    public void addNextActionToStack(DefaultGame cardGame) throws InvalidGameLogicException {
        cardGame.getActionsEnvironment().addActionToStack(_nextAction);
    }

}