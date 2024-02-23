package com.gempukku.stccg.actions;

import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.PlayOutDecisionEffect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Iterator;

public class PreventSubAction extends SubAction {
    private final Effect _effectToExecute;
    private final Iterator<String> _choicePlayers;
    private final PreventionCost _preventionCost;
    private final Effect _insteadEffect;

    private Effect _playerPreventionCost;

    public PreventSubAction(DefaultGame game, Action action, Effect effectToExecute, Iterator<String> choicePlayers, PreventionCost preventionCost, Effect insteadEffect) {
        super(action, game);
        _effectToExecute = effectToExecute;
        _choicePlayers = choicePlayers;
        _preventionCost = preventionCost;
        _insteadEffect = insteadEffect;
        appendEffect(new DecideIfPossible());
    }

    private class DecideIfPossible extends UnrespondableEffect {
        @Override
        protected void doPlayEffect() {
            if (_choicePlayers.hasNext()) {
                String nextPlayer = _choicePlayers.next();
                _playerPreventionCost = _preventionCost.createPreventionCostForPlayer(PreventSubAction.this, nextPlayer);
                if (_playerPreventionCost.isPlayableInFull()) {
                    appendEffect(
                            new PlayOutDecisionEffect(_game, nextPlayer,
                                    new YesNoDecision("Would you like to - " + _playerPreventionCost.getText() + " to prevent - " + _effectToExecute.getText()) {
                                        @Override
                                        protected void yes() {
                                            startPrevention();
                                        }

                                        @Override
                                        protected void no() {
                                            appendEffect(new DecideIfPossible());
                                        }
                                    }));
                } else {
                    appendEffect(new DecideIfPossible());
                }
            } else {
                appendEffect(_effectToExecute);
            }
        }
    }

    private void startPrevention() {
        appendEffect(_playerPreventionCost);
        appendEffect(new CheckIfPreventingCostWasSuccessful());
    }

    private class CheckIfPreventingCostWasSuccessful extends UnrespondableEffect {
        @Override
        protected void doPlayEffect() {
            if (!_playerPreventionCost.wasCarriedOut())
                appendEffect(new DecideIfPossible());
            else if (_insteadEffect != null)
                appendEffect(_insteadEffect);
        }
    }

    public interface PreventionCost {
        Effect createPreventionCostForPlayer(CostToEffectAction subAction, String playerId);
    }
}
