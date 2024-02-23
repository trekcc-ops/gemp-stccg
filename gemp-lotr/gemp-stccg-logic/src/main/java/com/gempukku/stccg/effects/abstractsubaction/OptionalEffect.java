package com.gempukku.stccg.effects.abstractsubaction;

import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.effects.AbstractSubActionEffect;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.Action;

public class OptionalEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _playerId;
    private final Effect _optionalEffect;
    private final DefaultGame _game;

    public OptionalEffect(DefaultGame game, Action action, String playerId, Effect optionalEffect) {
        _action = action;
        _playerId = playerId;
        _optionalEffect = optionalEffect;
        _game = game;
    }

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
    public void playEffect() {
        if (_optionalEffect.isPlayableInFull()) {
            String text = _optionalEffect.getText();
            if(text != null)
                text = text.toLowerCase();
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new MultipleChoiceAwaitingDecision(1, "Do you wish to " + text + "?", new String[]{"Yes", "No"}) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            if (index == 0) {
                                SubAction subAction = _action.createSubAction();
                                subAction.appendEffect(_optionalEffect);
                                processSubAction(_game, subAction);
                            }
                        }
                    });
        }
    }
}
