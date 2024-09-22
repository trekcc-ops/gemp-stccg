package com.gempukku.stccg.rules.generic;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.RequiredType;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.generic.GenericRule;

import java.util.LinkedList;
import java.util.List;

public class RequiredTriggersRule extends GenericRule {
    public RequiredTriggersRule(DefaultGame game) {
        super(game);
    }

    @Override
    public List<? extends Action> getRequiredBeforeTriggers(Effect effect) {
        List<Action> result = new LinkedList<>();
        for (PhysicalCard card : Filters.filterCardsInPlay(_game, Filters.active)) {
            if (!card.hasTextRemoved()) {
                result.addAll(card.getBeforeTriggerActions(effect, RequiredType.REQUIRED));
            }
        }
        return result;
    }

    @Override
    public List<? extends Action> getRequiredAfterTriggers(EffectResult effectResult) {
        List<Action> result = new LinkedList<>();
        for (PhysicalCard card : Filters.filterCardsInPlay(_game, Filters.active)) {
            if (!card.hasTextRemoved()) {
                result.addAll(card.getAfterTriggerActions(effectResult, RequiredType.REQUIRED));
            }
        }
        return result;
    }
}