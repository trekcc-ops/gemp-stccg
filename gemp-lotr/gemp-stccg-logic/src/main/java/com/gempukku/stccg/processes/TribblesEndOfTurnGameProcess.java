package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.SystemQueueAction;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.TriggeringResultEffect;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.results.EndOfTurnResult;

public class TribblesEndOfTurnGameProcess extends GameProcess {
    private GameProcess _nextProcess;
    private final TribblesGame _game;
    public TribblesEndOfTurnGameProcess(TribblesGame game) {
        super();
        _game = game;
    }
    @Override
    public void process() {
//        game.getGameState().sendMessage("DEBUG: Beginning TribblesEndOfTurnGameProcess");
        SystemQueueAction action = new SystemQueueAction();
        action.setText("End of turn");
        action.appendEffect(
                new TriggeringResultEffect(_game, new EndOfTurnResult(), "End of turn"));
        action.appendEffect(
                new Effect() {
                    @Override
                    public String getText() {
                        return null;
                    }

                    @Override
                    public EffectType getType() {
                        return null;
                    }
                    @Override
                    public boolean isPlayableInFull() {
                        return true;
                    }

                    @Override
                    public boolean wasCarriedOut() {
                        return true;
                    }

                    @Override
                    public void playEffect() {
                        ((ModifiersLogic) _game.getModifiersEnvironment()).signalEndOfTurn();
                        ((DefaultActionsEnvironment) _game.getActionsEnvironment()).signalEndOfTurn();
                        _game.getGameState().stopAffectingCardsForCurrentPlayer();
                    }
                });
        boolean playerWentOut = false;
        for (String playerId : _game.getPlayerIds()) {
            if (_game.getGameState().getHand(playerId).isEmpty()) {
                playerWentOut = true;
            }
        }
        if (playerWentOut) {
            _nextProcess = new TribblesEndOfRoundGameProcess(_game);
        } else {
            _nextProcess = new TribblesBetweenTurnsProcess(_game);
        }
        _game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
