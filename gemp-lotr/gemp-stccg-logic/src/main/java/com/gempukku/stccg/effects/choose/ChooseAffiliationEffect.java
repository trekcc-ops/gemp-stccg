package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;

import java.util.*;

public abstract class ChooseAffiliationEffect extends DefaultEffect {
    private final String _playerId;
    private final List<Affiliation> _affiliationOptions;
    private final DefaultGame _game;

    public ChooseAffiliationEffect(DefaultGame game, String playerId,
                                   List<Affiliation> affiliationOptions) {
        _playerId = playerId;
        _affiliationOptions = affiliationOptions;
        _game = game;
    }

    @Override
    public boolean isPlayableInFull() {
        return !_affiliationOptions.isEmpty();
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (!isPlayableInFull())
            return new FullEffectResult(false);
        else {
            Map<String, Affiliation> affiliationStringMap = new HashMap<>();
            List<String> affiliationStrings = new ArrayList<>();
            for (Affiliation affiliation : _affiliationOptions) {
                affiliationStringMap.put(affiliation.getHumanReadable(), affiliation);
                affiliationStrings.add(affiliation.getHumanReadable());
            }
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new MultipleChoiceAwaitingDecision(1, "Choose an affiliation", affiliationStrings) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            affiliationChosen(affiliationStringMap.get(result));
                        }
                    });
        }
        return new FullEffectResult(true);
    }

    protected abstract void affiliationChosen(Affiliation affiliation);
}
