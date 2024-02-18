package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.SystemQueueAction;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.TriggeringResultEffect;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.results.EndOfTurnResult;

public class EndOfTurnGameProcess extends GameProcess {
    private DefaultGame _game;
    public EndOfTurnGameProcess(DefaultGame game) {
        _game = game;
    }
    @Override
    public void process() {
        _game.getGameState().sendMessage("DEBUG: Beginning EndOfTurnGameProcess");
        SystemQueueAction action = new SystemQueueAction();
        action.setText("End of turn");
        action.appendEffect(
                new TriggeringResultEffect(null, new EndOfTurnResult(), "End of turn"));
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
                        _game.getGameState().setCurrentPhase(Phase.BETWEEN_TURNS);
                        _game.getGameState().sendMessage("End of turn, players swap roles.");
                    }
                });
        _game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess() {
        return new BetweenTurnsProcess(_game, new StartOfPhaseGameProcess(Phase.PLAY, null, _game));
    }
}
