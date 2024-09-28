package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.AbstractSubActionEffect;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.actions.EffectType;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.Action;

import java.util.LinkedList;
import java.util.List;

public class ChoiceEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _choicePlayerId;
    private final List<Effect> _possibleEffects;

    public ChoiceEffect(DefaultGame game, Action action, String choicePlayerId, List<Effect> possibleEffects) {
        super(game);
        _action = action;
        _choicePlayerId = choicePlayerId;
        _possibleEffects = possibleEffects;
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
        for (Effect effect : _possibleEffects) {
            if (effect.isPlayableInFull())
                return true;
        }
        return false;
    }

    @Override
    public void playEffect() {
        final List<Effect> possibleEffects = new LinkedList<>();
        for (Effect effect : _possibleEffects) {
            if (effect.isPlayableInFull())
                possibleEffects.add(effect);
        }

        if (possibleEffects.size() == 1) {
            SubAction subAction = _action.createSubAction();
            subAction.appendEffect(possibleEffects.getFirst());
            processSubAction(_game, subAction);
        } else if (!possibleEffects.isEmpty()) {
            _game.getUserFeedback().sendAwaitingDecision(_choicePlayerId,
                    new MultipleChoiceAwaitingDecision(
                            "Choose effect to use", getEffectsText(possibleEffects)
                    ) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            SubAction subAction = _action.createSubAction();
                            subAction.appendEffect(possibleEffects.get(index));
                            processSubAction(_game, subAction);
                        }
                    });
        }
    }

    private String[] getEffectsText(List<Effect> possibleEffects) {
        String[] result = new String[possibleEffects.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = possibleEffects.get(i).getText();
        return result;
    }

    public DefaultGame getGame() { return _game; }
}