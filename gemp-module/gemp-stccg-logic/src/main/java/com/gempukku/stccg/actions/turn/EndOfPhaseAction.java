package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;

public class EndOfPhaseAction extends SystemQueueAction {
    public EndOfPhaseAction(DefaultGame game) {
        super(game);
        Phase phase = game.getGameState().getCurrentPhase();
        String phaseString = game.getCurrentPhaseString();
        String message = "End of " + phaseString + " phase";
        setText(message);
        appendEffect(new TriggeringResultEffect(new EndOfPhaseResult(game), message));
        appendEffect(
                new Effect() {
                    @Override
                    public String getText() {
                        return null;
                    }
                    @Override
                    public String getPerformingPlayerId() { return null; }
                    @Override
                    public boolean isPlayableInFull() {
                        return true;
                    }

                    @Override
                    public boolean wasCarriedOut() {
                        return true;
                    }

                    @Override
                    public EffectType getType() {
                        return null;
                    }

                    @Override
                    public void playEffect() {
                        _game.getActionsEnvironment().signalEndOfPhase();
                        _game.sendMessage(message);
                    }

                    @Override
                    public DefaultGame getGame() { return _game; }
                });
    }

}