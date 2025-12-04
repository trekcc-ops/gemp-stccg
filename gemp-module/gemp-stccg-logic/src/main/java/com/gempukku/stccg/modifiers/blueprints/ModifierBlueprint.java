package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.requirement.AndCondition;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.TrueCondition;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AddCunningModifierBlueprint.class, name = "addCunning"),
        @JsonSubTypes.Type(value = AddStrengthModifierBlueprint.class, name = "addStrength"),
        @JsonSubTypes.Type(value = GainIconModifierBlueprint.class, name = "gainIcon"),
        @JsonSubTypes.Type(value = GainSkillModifierBlueprint.class, name = "gainSkill")
})
public interface ModifierBlueprint {
    Modifier createModifier(DefaultGame cardGame, PhysicalCard thisCard, ActionContext actionContext);

    default Modifier createModifierWithNewContext(DefaultGame cardGame, PhysicalCard thisCard) {
        ActionContext newContext = new ActionContext(thisCard, thisCard.getControllerName());
        return createModifier(cardGame, thisCard, newContext);
    }

    default Condition convertRequirementListToCondition(List<Requirement> requirements, ActionContext actionContext,
                                                        PhysicalCard thisCard, DefaultGame cardGame) {
        if (requirements == null || requirements.isEmpty()) {
            return new TrueCondition();
        }
        List<Condition> conditions = new ArrayList<>();
        for (Requirement requirement : requirements) {
            conditions.add(requirement.getCondition(actionContext, thisCard, cardGame));
        }
        if (conditions.size() == 1) {
            return conditions.getFirst();
        } else {
            return new AndCondition(conditions);
        }
    }

}