package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.UserFeedback;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.gamestate.GameState;

import java.util.*;

public abstract class TurnProcedure implements Snapshotable<TurnProcedure> {
    private UserFeedback _userFeedback;
    private DefaultGame _game;
    protected final Stack<Action> _actionStack;
    protected GameProcess _gameProcess;
    private boolean _playedGameProcess;
    private ActionsEnvironment _actionsEnvironment;

    @Override
    public TurnProcedure generateSnapshot(SnapshotData snapshotData) {
        TurnProcedure selfSnapshot = new TurnProcedure(_game, _userFeedback) {
            @Override
            protected GameProcess setFirstGameProcess() {
                return null; // TODO - This shouldn't be null, but it also should never be called
            }
        };

        // Set each field
//        selfSnapshot._actionStack = snapshotData.getDataForSnapshot(_actionStack); // TODO SNAPSHOT - Need to move ActionStack back out into its own class?
        selfSnapshot._gameProcess = _gameProcess;
        selfSnapshot._playedGameProcess = _playedGameProcess;
        selfSnapshot._actionsEnvironment = _actionsEnvironment;
        return selfSnapshot;
    }



    public TurnProcedure(DefaultGame game, final UserFeedback userFeedback) {
        _game = game;
        _actionsEnvironment = _game.getActionsEnvironment();
        _actionStack = _actionsEnvironment.getActionStack();
        _userFeedback = userFeedback;
    }

    protected abstract GameProcess setFirstGameProcess();

    public void carryOutPendingActionsUntilDecisionNeeded() {
        int numSinceDecision = 0;

        if (_gameProcess == null) {
            // Take game snapshot for start of game
//            _game.takeSnapshot("Start of game"); // TODO SNAPSHOT - turned it off because it's not working
            _gameProcess = setFirstGameProcess();
        }

        while (_userFeedback.hasNoPendingDecisions() && _game.getWinnerPlayerId() == null &&
                !_game.isRestoreSnapshotPending()) {
            numSinceDecision++;
            // First check for any "state-based" effects
            Set<EffectResult> effectResults = _actionsEnvironment.consumeEffectResults();
            effectResults.forEach(EffectResult::createOptionalAfterTriggerActions);
            if (!effectResults.isEmpty()) {
                _actionStack.add(new PlayOutEffectResults(_game, effectResults));
            } else {
                if (_actionStack.isEmpty()) {
                    if (_playedGameProcess) {
                        _gameProcess = _gameProcess.getNextProcess();
                        _playedGameProcess = false;
                    } else {
                        _gameProcess.process();
                        _game.getGameState().updateGameStatsAndSendIfChanged();
                        _playedGameProcess = true;
                    }
                } else {
                    Action action = _actionStack.peek();
                    try {
                        Effect effect = action.nextEffect();
                        if (effect == null) {
                            _actionStack.remove(_actionStack.lastIndexOf(action));
                        }
                        if (effect != null) {
                            if (effect.getType() == null) {
                                effect.playEffect();
                            } else
                                _actionStack.add(new PlayOutEffect(_game, effect));
                        }
                    } catch(InvalidGameLogicException exp) {
                        _game.sendMessage(exp.getMessage());
                    }
                }
            }
            _game.getGameState().updateGameStatsAndSendIfChanged();

            // Check if an unusually large number loops since user decision, which means game is probably in a loop
            if (numSinceDecision >= 5000) {
                String errorMessage = "There's been " + numSinceDecision +
                        " actions/effects since last user decision. Game is probably looping, so ending game.";
                _game.sendMessage(errorMessage);

                int actionNum = 1;
                _game.sendMessage("Action stack size: " + _actionStack.size());
                for (Action action : _actionStack) {
                    _game.sendMessage("Action " + (actionNum++) + ": " +
                            action.getClass().getSimpleName() + (action.getActionSource() != null ?
                            " Source: " + action.getActionSource().getFullName() : ""));
                }

                effectResults = _game.getActionsEnvironment().consumeEffectResults();
                int numEffectResult = 1;
                for (EffectResult effectResult : effectResults) {
                    _game.sendMessage("EffectResult " + (numEffectResult++) + ": " +
                            effectResult.getType().name());
                }
                throw new UnsupportedOperationException(errorMessage);
            }
        }
    }

    protected class PlayOutEffect extends SystemQueueAction {
        private final Effect _effect;
        private boolean _initialized;

        protected PlayOutEffect(DefaultGame game, Effect effect) {
            super(game);
            _effect = effect;
        }

        @Override
        public String getText() {
            return _effect.getText();
        }

        @Override
        public Effect nextEffect() {
            if (!_initialized) {
                _initialized = true;
                appendEffect(new PlayOutRequiredBeforeResponsesEffect(this, new HashSet<>(), _effect));
                appendEffect(new PlayOutOptionalBeforeResponsesEffect(this, new HashSet<>(),
                        _game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(
                                _game.getGameState().getCurrentPlayerId(), true), 0, _effect));
                appendEffect(new PlayEffect(_effect));
            }

            return getNextEffect();
        }
    }

    protected static class PlayEffect extends UnrespondableEffect {
        private final Effect _effect;

        private PlayEffect(Effect effect) {
            super(effect.getGame());
            _effect = effect;
        }

        @Override
        protected void doPlayEffect() {
            _effect.playEffect();
        }
    }

    protected class PlayOutEffectResults extends SystemQueueAction {
        private final Set<EffectResult> _effectResults;
        private boolean _initialized;

        protected PlayOutEffectResults(DefaultGame game, Set<EffectResult> effectResults) {
            super(game);
            _effectResults = effectResults;
        }

        @Override
        public Effect nextEffect() {
            if (!_initialized) {
                _initialized = true;
                List<Action> requiredResponses = _actionsEnvironment.getRequiredAfterTriggers(_effectResults);
                if (!requiredResponses.isEmpty())
                    appendEffect(new PlayOutAllActionsIfEffectNotCancelledEffect(this, requiredResponses));

                GameState gameState = _game.getGameState();
                appendEffect(
                        new PlayOutOptionalAfterResponsesEffect(this,
                                gameState.getPlayerOrder().getCounterClockwisePlayOrder(
                                        gameState.getCurrentPlayerId(), true
                                ), 0, _effectResults
                        )
                );
            }
            return getNextEffect();
        }

    }

    protected class PlayOutRequiredBeforeResponsesEffect extends UnrespondableEffect {
        private final SystemQueueAction _action;
        private final Set<PhysicalCard> _cardTriggersUsed;
        private final Effect _effect;

        private PlayOutRequiredBeforeResponsesEffect(SystemQueueAction action, Set<PhysicalCard> cardTriggersUsed,
                                                     Effect effect) {
            super(action.getGame());
            _action = action;
            _cardTriggersUsed = cardTriggersUsed;
            _effect = effect;
        }

        @Override
        protected void doPlayEffect() {
            final List<Action> requiredBeforeTriggers = _actionsEnvironment.getRequiredBeforeTriggers(_effect);
            // Remove triggers already resolved
            requiredBeforeTriggers.removeIf(action -> _cardTriggersUsed.contains(action.getActionSource()));
            
            if (requiredBeforeTriggers.size() == 1) {
                _actionsEnvironment.addActionToStack(requiredBeforeTriggers.getFirst());
            } else if (requiredBeforeTriggers.size() > 1) {
                _game.getUserFeedback().sendAwaitingDecision(_game.getGameState().getCurrentPlayerId(),
                        new ActionSelectionDecision(_game, 1, _effect.getText() + " - Required \"is about to\" responses", requiredBeforeTriggers) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    _actionsEnvironment.addActionToStack(action);
                                    if (requiredBeforeTriggers.contains(action))
                                        _cardTriggersUsed.add(action.getActionSource());
                                    _actionsEnvironment.addActionToStack(action);
                                    _action.insertEffect(new PlayOutRequiredBeforeResponsesEffect(
                                            _action, _cardTriggersUsed, _effect));
                                }
                            }
                        });
            }
        }
    }

    protected class PlayOutOptionalBeforeResponsesEffect extends UnrespondableEffect {
        private final SystemQueueAction _action;
        private final Set<PhysicalCard> _cardTriggersUsed;
        private final ActionOrder _actionOrder;
        private final int _passCount;
        private final Effect _effect;

        private PlayOutOptionalBeforeResponsesEffect(SystemQueueAction action, Set<PhysicalCard> cardTriggersUsed, ActionOrder actionOrder, int passCount, Effect effect) {
            super(action.getGame());
            _action = action;
            _cardTriggersUsed = cardTriggersUsed;
            _actionOrder = actionOrder;
            _passCount = passCount;
            _effect = effect;
        }

        @Override
        public void doPlayEffect() {
            final String activePlayer = _actionOrder.getNextPlayer();

            final List<Action> optionalBeforeTriggers =
                    _actionsEnvironment.getOptionalBeforeTriggers(activePlayer, _effect);
            // Remove triggers already resolved
            optionalBeforeTriggers.removeIf(action -> _cardTriggersUsed.contains(action.getActionSource()));

            final List<Action> optionalBeforeActions =
                    _actionsEnvironment.getOptionalBeforeActions(activePlayer, _effect);

            List<Action> possibleActions = new LinkedList<>(optionalBeforeTriggers);
            possibleActions.addAll(optionalBeforeActions);

            if (!possibleActions.isEmpty()) {
                _game.getUserFeedback().sendAwaitingDecision(activePlayer,
                        new CardActionSelectionDecision(1, _effect.getText() + " - Optional \"is about to\" responses", possibleActions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    _actionsEnvironment.addActionToStack(action);
                                    if (optionalBeforeTriggers.contains(action))
                                        _cardTriggersUsed.add(action.getActionSource());
                                    _action.insertEffect(new PlayOutOptionalBeforeResponsesEffect(
                                            _action, _cardTriggersUsed, _actionOrder, 0, _effect));
                                } else {
                                    if ((_passCount + 1) < _actionOrder.getPlayerCount()) {
                                        _action.insertEffect(new PlayOutOptionalBeforeResponsesEffect(
                                                _action, _cardTriggersUsed, _actionOrder, _passCount + 1, _effect));
                                    }
                                }
                            }
                        });
            } else {
                if ((_passCount + 1) < _actionOrder.getPlayerCount()) {
                    _action.insertEffect(new PlayOutOptionalBeforeResponsesEffect(_action, _cardTriggersUsed, _actionOrder, _passCount + 1, _effect));
                }
            }
        }
    }

    protected class PlayOutOptionalAfterResponsesEffect extends UnrespondableEffect {
        private final SystemQueueAction _action;
        private final ActionOrder _actionOrder;
        private final int _passCount;
        private final Collection<? extends EffectResult> _effectResults;

        private PlayOutOptionalAfterResponsesEffect(SystemQueueAction action, ActionOrder actionOrder, int passCount,
                                                    Collection<? extends EffectResult> effectResults) {
            super(action.getGame());
            _action = action;
            _actionOrder = actionOrder;
            _passCount = passCount;
            _effectResults = effectResults;
        }

        @Override
        public void doPlayEffect() {
            final String activePlayer = _actionOrder.getNextPlayer();

            final Map<Action, EffectResult> optionalAfterTriggers =
                    _actionsEnvironment.getOptionalAfterTriggers(activePlayer, _effectResults);

            List<Action> possibleActions = new LinkedList<>(optionalAfterTriggers.keySet());
            possibleActions.addAll(_actionsEnvironment.getOptionalAfterActions(activePlayer, _effectResults));

            if (!possibleActions.isEmpty()) {
                _game.getUserFeedback().sendAwaitingDecision(activePlayer,
                        new CardActionSelectionDecision(1, "Optional responses", possibleActions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                final int nextPassCount;
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    _actionsEnvironment.addActionToStack(action);
                                    if (optionalAfterTriggers.containsKey(action))
                                        optionalAfterTriggers.get(action).optionalTriggerUsed(action);
                                    nextPassCount = 0;
                                } else {
                                    nextPassCount = _passCount + 1;
                                }
                                if (nextPassCount < _actionOrder.getPlayerCount())
                                    _action.insertEffect(new PlayOutOptionalAfterResponsesEffect(
                                            _action, _actionOrder, nextPassCount, _effectResults));
                            }
                        });
            } else {
                if ((_passCount + 1) < _actionOrder.getPlayerCount()) {
                    _action.insertEffect(new PlayOutOptionalAfterResponsesEffect(
                            _action, _actionOrder, _passCount + 1, _effectResults));
                }
            }
        }
    }

    protected class PlayOutAllActionsIfEffectNotCancelledEffect extends UnrespondableEffect {
        private final SystemQueueAction _action;
        private final List<Action> _actions;

        private PlayOutAllActionsIfEffectNotCancelledEffect(SystemQueueAction action, List<Action> actions) {
            super(action.getGame());
            _action = action;
            _actions = actions;
        }

        @Override
        public void doPlayEffect() {
            if (_actions.size() == 1) {
                _actionStack.add(_actions.getFirst());
            } else if (_actions.stream().allMatch(action -> action.getActionSource() != null &&
                    action.getActionSource().getBlueprint() == _actions.getFirst().getActionSource().getBlueprint())) {
                Action anyAction = _actions.getFirst();
                _actions.remove(anyAction);
                _actionStack.add(anyAction);
                _action.insertEffect(new PlayOutAllActionsIfEffectNotCancelledEffect(_action, _actions));
            } else {
                _game.getUserFeedback().sendAwaitingDecision(_game.getGameState().getCurrentPlayerId(),
                        new ActionSelectionDecision(_game, 1, "Required responses", _actions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                _actionStack.add(action);
                                _actions.remove(action);
                                _action.insertEffect(new PlayOutAllActionsIfEffectNotCancelledEffect(_action, _actions));
                            }
                        });
            }
        }

    }


}