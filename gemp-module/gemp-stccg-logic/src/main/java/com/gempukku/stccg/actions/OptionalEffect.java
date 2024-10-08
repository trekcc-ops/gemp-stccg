package com.gempukku.stccg.actions;

import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;

public class OptionalEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _playerId;
    private final Effect _optionalEffect;

    public OptionalEffect(DefaultGame game, Action action, String playerId, Effect optionalEffect) {
        super(game);
        _action = action;
        _playerId = playerId;
        _optionalEffect = optionalEffect;
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
                    new MultipleChoiceAwaitingDecision("Do you wish to " + text + "?", new String[]{"Yes", "No"}) {
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
