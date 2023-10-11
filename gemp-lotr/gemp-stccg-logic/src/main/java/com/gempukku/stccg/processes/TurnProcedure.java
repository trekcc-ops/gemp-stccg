package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.OptionalTriggerAction;
import com.gempukku.stccg.actions.SystemQueueAction;
import com.gempukku.stccg.adventure.InvalidSoloAdventureException;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.results.EffectResult;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayOrder;
import com.gempukku.stccg.game.PlayerOrderFeedback;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.GameStats;
import com.gempukku.stccg.gamestate.UserFeedback;

import java.util.*;

// Action generates multiple Effects, both costs and result of an action are Effects.

// Decision is also an Effect.
public class TurnProcedure<AbstractGame extends DefaultGame> {
    protected final UserFeedback _userFeedback;
    protected final AbstractGame _game;
    protected final Stack<Action> _actionStack;
    protected GameProcess _gameProcess;
    private boolean _playedGameProcess;
    protected final GameStats _gameStats;

    public TurnProcedure(AbstractGame game, Set<String> players, final UserFeedback userFeedback,
                         DefaultActionsEnvironment actionsEnvironment, final PlayerOrderFeedback playerOrderFeedback) {
        _userFeedback = userFeedback;
        _game = game;
        _actionStack = actionsEnvironment.getActionStack();

        _gameStats = new GameStats();
        _gameProcess = setFirstGameProcess(game, players, playerOrderFeedback);
    }

    protected GameProcess setFirstGameProcess(AbstractGame game, Set<String> players, PlayerOrderFeedback playerOrderFeedback) {
        return game.getFormat().getStartingGameProcess(players, playerOrderFeedback);
    }

    public GameStats getGameStats() { return _gameStats; }

    public void carryOutPendingActionsUntilDecisionNeeded() {
        while (_userFeedback.hasNoPendingDecisions() && _game.getWinnerPlayerId() == null) {
            // First check for any "state-based" effects
            Set<EffectResult> effectResults = ((DefaultActionsEnvironment) _game.getActionsEnvironment()).consumeEffectResults();
            if (effectResults.size() > 0) {
                _actionStack.add(new PlayOutEffectResults(effectResults));
            } else {
                if (_actionStack.isEmpty()) {
                    if (_playedGameProcess) {
                        _gameProcess = _gameProcess.getNextProcess();
                        _playedGameProcess = false;
                    } else {
                        _gameProcess.process(_game);
                        if (_gameStats.updateGameStats(_game))
                            _game.getGameState().sendGameStats(_gameStats);
                        _playedGameProcess = true;
                    }
                } else {
                    Action action = _actionStack.peek();
                    Effect effect = action.nextEffect(_game);
                    if (effect == null) {
                        _actionStack.remove(_actionStack.lastIndexOf(action));
                    }
                    if (effect != null) {
                        if (effect.getType() == null) {
                            try {
                                effect.playEffect();
                            } catch (InvalidSoloAdventureException exp) {
                                _game.playerLost(_game.getGameState().getCurrentPlayerId(), exp.getMessage());
                            }
                        } else
                            _actionStack.add(new PlayOutEffect(effect));
                    }
                }
            }

            if (_gameStats.updateGameStats(_game))
                _game.getGameState().sendGameStats(_gameStats);
        }
    }

    protected class PlayOutEffect extends SystemQueueAction {
        private final Effect _effect;
        private boolean _initialized;

        protected PlayOutEffect(Effect effect) {
            _effect = effect;
        }

        @Override
        public String getText() {
            return _effect.getText();
        }

        @Override
        public Effect nextEffect(DefaultGame game) {
            if (!_initialized) {
                _initialized = true;
                appendEffect(new PlayOutRequiredBeforeResponsesEffect(this, new HashSet<>(), _effect));
                appendEffect(new PlayOutOptionalBeforeResponsesEffect(this, new HashSet<>(), _game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_game.getGameState().getCurrentPlayerId(), true), 0, _effect));
                appendEffect(new PlayEffect(_effect));
            }

            return getNextEffect();
        }
    }

    protected class PlayEffect extends UnrespondableEffect {
        private final Effect _effect;

        private PlayEffect(Effect effect) {
            _effect = effect;
        }

        @Override
        protected void doPlayEffect() {
            try {
                _effect.playEffect();
            } catch (InvalidSoloAdventureException exp) {
                _game.playerLost(_game.getGameState().getCurrentPlayerId(), exp.getMessage());
            }
        }
    }

    protected class PlayOutEffectResults extends SystemQueueAction {
        private final Set<EffectResult> _effectResults;
        private boolean _initialized;

        protected PlayOutEffectResults(Set<EffectResult> effectResults) {
            _effectResults = effectResults;
        }

        @Override
        public Effect nextEffect(DefaultGame game) {
            if (!_initialized) {
                _initialized = true;
                List<Action> requiredResponses = _game.getActionsEnvironment().getRequiredAfterTriggers(_effectResults);
                if (requiredResponses.size() > 0)
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

        private PlayOutRequiredBeforeResponsesEffect(SystemQueueAction action, Set<PhysicalCard> cardTriggersUsed, Effect effect) {
            _action = action;
            _cardTriggersUsed = cardTriggersUsed;
            _effect = effect;
        }

        @Override
        protected void doPlayEffect() {
            final List<Action> requiredBeforeTriggers = _game.getActionsEnvironment().getRequiredBeforeTriggers(_effect);
            // Remove triggers already resolved
            requiredBeforeTriggers.removeIf(action -> _cardTriggersUsed.contains(action.getActionSource()));
            
            if (requiredBeforeTriggers.size() == 1) {
                _game.getActionsEnvironment().addActionToStack(requiredBeforeTriggers.get(0));
            } else if (requiredBeforeTriggers.size() > 1) {
                _game.getUserFeedback().sendAwaitingDecision(_game.getGameState().getCurrentPlayerId(),
                        new ActionSelectionDecision(_game, 1, _effect.getText() + " - Required \"is about to\" responses", requiredBeforeTriggers) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    _game.getActionsEnvironment().addActionToStack(action);
                                    if (requiredBeforeTriggers.contains(action))
                                        _cardTriggersUsed.add(action.getActionSource());
                                    _game.getActionsEnvironment().addActionToStack(action);
                                    _action.insertEffect(new PlayOutRequiredBeforeResponsesEffect(_action, _cardTriggersUsed, _effect));
                                }
                            }
                        });
            }
        }
    }

    protected class PlayOutOptionalBeforeResponsesEffect extends UnrespondableEffect {
        private final SystemQueueAction _action;
        private final Set<PhysicalCard> _cardTriggersUsed;
        private final PlayOrder _playOrder;
        private final int _passCount;
        private final Effect _effect;

        private PlayOutOptionalBeforeResponsesEffect(SystemQueueAction action, Set<PhysicalCard> cardTriggersUsed, PlayOrder playOrder, int passCount, Effect effect) {
            _action = action;
            _cardTriggersUsed = cardTriggersUsed;
            _playOrder = playOrder;
            _passCount = passCount;
            _effect = effect;
        }

        @Override
        public void doPlayEffect() {
            final String activePlayer = _playOrder.getNextPlayer();

            final List<Action> optionalBeforeTriggers = _game.getActionsEnvironment().getOptionalBeforeTriggers(activePlayer, _effect);
            // Remove triggers already resolved
            optionalBeforeTriggers.removeIf(action -> _cardTriggersUsed.contains(action.getActionSource()));

            final List<Action> optionalBeforeActions = _game.getActionsEnvironment().getOptionalBeforeActions(activePlayer, _effect);

            List<Action> possibleActions = new LinkedList<>(optionalBeforeTriggers);
            possibleActions.addAll(optionalBeforeActions);

            if (possibleActions.size() > 0) {
                _game.getUserFeedback().sendAwaitingDecision(activePlayer,
                        new CardActionSelectionDecision(1, _effect.getText() + " - Optional \"is about to\" responses", possibleActions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    _game.getActionsEnvironment().addActionToStack(action);
                                    if (optionalBeforeTriggers.contains(action))
                                        _cardTriggersUsed.add(action.getActionSource());
                                    _action.insertEffect(new PlayOutOptionalBeforeResponsesEffect(_action, _cardTriggersUsed, _playOrder, 0, _effect));
                                } else {
                                    if ((_passCount + 1) < _playOrder.getPlayerCount()) {
                                        _action.insertEffect(new PlayOutOptionalBeforeResponsesEffect(_action, _cardTriggersUsed, _playOrder, _passCount + 1, _effect));
                                    }
                                }
                            }
                        });
            } else {
                if ((_passCount + 1) < _playOrder.getPlayerCount()) {
                    _action.insertEffect(new PlayOutOptionalBeforeResponsesEffect(_action, _cardTriggersUsed, _playOrder, _passCount + 1, _effect));
                }
            }
        }
    }

    protected class PlayOutOptionalAfterResponsesEffect extends UnrespondableEffect {
        private final SystemQueueAction _action;
        private final PlayOrder _playOrder;
        private final int _passCount;
        private final Collection<? extends EffectResult> _effectResults;

        private PlayOutOptionalAfterResponsesEffect(SystemQueueAction action, PlayOrder playOrder, int passCount, Collection<? extends EffectResult> effectResults) {
            _action = action;
            _playOrder = playOrder;
            _passCount = passCount;
            _effectResults = effectResults;
        }

        @Override
        public void doPlayEffect() {
            final String activePlayer = _playOrder.getNextPlayer();

            final Map<OptionalTriggerAction, EffectResult> optionalAfterTriggers = _game.getActionsEnvironment().getOptionalAfterTriggers(activePlayer, _effectResults);

            final List<Action> optionalAfterActions = _game.getActionsEnvironment().getOptionalAfterActions(activePlayer, _effectResults);

            List<Action> possibleActions = new LinkedList<>(optionalAfterTriggers.keySet());
            possibleActions.addAll(optionalAfterActions);

            if (possibleActions.size() > 0) {
                _game.getUserFeedback().sendAwaitingDecision(activePlayer,
                        new CardActionSelectionDecision(1, "Optional responses", possibleActions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    _game.getActionsEnvironment().addActionToStack(action);
                                    if (optionalAfterTriggers.containsKey(action))
                                        optionalAfterTriggers.get(action).optionalTriggerUsed((OptionalTriggerAction) action);

                                    _action.insertEffect(new PlayOutOptionalAfterResponsesEffect(_action, _playOrder, 0, _effectResults));
                                } else {
                                    if ((_passCount + 1) < _playOrder.getPlayerCount()) {
                                        _action.insertEffect(new PlayOutOptionalAfterResponsesEffect(_action, _playOrder, _passCount + 1, _effectResults));
                                    }
                                }
                            }
                        });
            } else {
                if ((_passCount + 1) < _playOrder.getPlayerCount()) {
                    _action.insertEffect(new PlayOutOptionalAfterResponsesEffect(_action, _playOrder, _passCount + 1, _effectResults));
                }
            }
        }
    }

    protected class PlayOutAllActionsIfEffectNotCancelledEffect extends UnrespondableEffect {
        private final SystemQueueAction _action;
        private final List<Action> _actions;

        private PlayOutAllActionsIfEffectNotCancelledEffect(SystemQueueAction action, List<Action> actions) {
            _action = action;
            _actions = actions;
        }

        @Override
        public void doPlayEffect() {
            if (_actions.size() == 1) {
                _game.getActionsEnvironment().addActionToStack(_actions.get(0));
            } else if (areAllActionsTheSame()) {
                Action anyAction = _actions.get(0);
                _actions.remove(anyAction);
                _game.getActionsEnvironment().addActionToStack(anyAction);
                _action.insertEffect(new PlayOutAllActionsIfEffectNotCancelledEffect(_action, _actions));
            } else {
                _game.getUserFeedback().sendAwaitingDecision(_game.getGameState().getCurrentPlayerId(),
                        new ActionSelectionDecision(_game, 1, "Required responses", _actions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                _game.getActionsEnvironment().addActionToStack(action);
                                _actions.remove(action);
                                _action.insertEffect(new PlayOutAllActionsIfEffectNotCancelledEffect(_action, _actions));
                            }
                        });
            }
        }

        private boolean areAllActionsTheSame() {
            Iterator<Action> actionIterator = _actions.iterator();

            Action firstAction = actionIterator.next();
            if (firstAction.getActionSource() == null)
                return false;

            while (actionIterator.hasNext()) {
                Action otherAction = actionIterator.next();
                if (otherAction.getActionSource() == null || otherAction.getActionSource().getBlueprint() != firstAction.getActionSource().getBlueprint())
                    return false;
            }
            return true;
        }
    }
}
