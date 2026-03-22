package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.List;

public class AndRequirement implements Requirement {

    private final List<Requirement> _requirements;

    @JsonCreator
    private AndRequirement(@JsonProperty("requirements")List<Requirement> requirements) {
        _requirements = requirements;
    }
    @Override
    public boolean accepts(GameTextContext context, DefaultGame cardGame) {
        return getCondition(context, context.card(), cardGame).isFulfilled(cardGame);
    }

    @Override
    public Condition getCondition(GameTextContext context, PhysicalCard thisCard, DefaultGame cardGame) {
        List<Condition> conditions = new ArrayList<>();
        for (Requirement requirement : _requirements) {
            conditions.add(requirement.getCondition(context, thisCard, cardGame));
        }
        return new AndCondition(conditions);
    }
}