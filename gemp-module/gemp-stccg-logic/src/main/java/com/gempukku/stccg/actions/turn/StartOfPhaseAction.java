package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;

public class StartOfPhaseAction extends SystemQueueAction {
    public StartOfPhaseAction(DefaultGame game, Phase phase) {
        super(game);
        String message = "Start of " + phase + " phase";
        setText(message);
        appendEffect(new TriggeringResultEffect(
                new StartOfPhaseResult(_game), message));
        appendEffect(
                new Effect() {
                    @Override
                    public String getText() {
                        return null;
                    }

                    @Override
                    public String getPerformingPlayerId() { return null; }

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
                        ((DefaultActionsEnvironment) _game.getActionsEnvironment()).signalStartOfPhase(phase);
                        _game.sendMessage("\n" + message);
                    }

                    @Override
                    public DefaultGame getGame() { return _game; }
                });
    }
}