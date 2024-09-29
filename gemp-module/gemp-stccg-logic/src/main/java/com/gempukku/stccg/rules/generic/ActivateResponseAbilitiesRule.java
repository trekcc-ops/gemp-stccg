package com.gempukku.stccg.rules.generic;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class ActivateResponseAbilitiesRule extends GenericRule {
    public ActivateResponseAbilitiesRule(DefaultGame game) {
        super(game);
    }

    @Override
    public List<? extends Action> getOptionalBeforeActions(String playerId, Effect effect) {
        List<Action> result = new LinkedList<>();
        for (PhysicalCard card : Filters.filterCardsInPlay(_game, Filters.and(Filters.owner(playerId), Filters.active))) {
            if (!card.hasTextRemoved())
                result.addAll(card.getOptionalInPlayActions(effect, TriggerTiming.BEFORE));
        }
        return result;
    }

    @Override
    public List<? extends Action> getOptionalAfterActions(String playerId, EffectResult effectResult) {
        List<Action> result = new LinkedList<>();
        for (PhysicalCard card : Filters.filterCardsInPlay(_game, Filters.and(Filters.owner(playerId), Filters.active))) {
            if (!card.hasTextRemoved())
                result.addAll(card.getOptionalInPlayActions(effectResult, TriggerTiming.AFTER));
        }
        return result;
    }
}