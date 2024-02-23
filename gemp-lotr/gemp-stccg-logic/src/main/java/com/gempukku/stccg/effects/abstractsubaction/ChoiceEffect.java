package com.gempukku.stccg.effects.abstractsubaction;

import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.effects.AbstractSubActionEffect;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.Action;

import java.util.LinkedList;
import java.util.List;

public class ChoiceEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _choicePlayerId;
    private final List<Effect> _possibleEffects;
    private final DefaultGame _game;

    public ChoiceEffect(DefaultGame game, Action action, String choicePlayerId, List<Effect> possibleEffects) {
        _action = action;
        _choicePlayerId = choicePlayerId;
        _possibleEffects = possibleEffects;
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
            subAction.appendEffect(possibleEffects.get(0));
            processSubAction(_game, subAction);
        } else if (!possibleEffects.isEmpty()) {
            _game.getUserFeedback().sendAwaitingDecision(_choicePlayerId,
                    new MultipleChoiceAwaitingDecision(
                            1, "Choose effect to use", getEffectsText(possibleEffects)
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
}