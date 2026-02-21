package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.AndCondition;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.requirement.Requirement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConditionTriggerChecker implements TriggerChecker {

    private final List<Requirement> _requirements;

    ConditionTriggerChecker(
            @JsonProperty(value = "requires", required = true)
            List<Requirement> requirements
    ) {
        _requirements = requirements;
    }
    public boolean accepts(GameTextContext actionContext, DefaultGame cardGame) {
        return actionContext.acceptsAllRequirements(cardGame, _requirements);
    }

    @Override
    public Condition getCondition(GameTextContext context, PhysicalCard thisCard, DefaultGame cardGame) {
        Collection<Condition> conditions = new ArrayList<>();
        for (Requirement requirement : _requirements) {
            conditions.add(requirement.getCondition(context, thisCard, cardGame));
        }
        return new AndCondition(conditions);
    }


}