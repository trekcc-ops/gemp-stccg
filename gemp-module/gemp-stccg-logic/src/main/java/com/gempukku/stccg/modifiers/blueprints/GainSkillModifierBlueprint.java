package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.condition.RequirementCondition;
import com.gempukku.stccg.condition.TrueCondition;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.*;
import com.gempukku.stccg.requirement.Requirement;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class GainSkillModifierBlueprint implements ModifierBlueprint {

    private final SkillName[] _skillsGained;
    private final FilterBlueprint _modifiedCardFilterBlueprint;
    private final Condition _condition;

    GainSkillModifierBlueprint(@JsonProperty(value = "skills", required = true)
                               @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                              SkillName[] skills,
                              @JsonProperty(value = "modifiedCards", required = true)
                              FilterBlueprint modifiedCardFilterBlueprint,
                              @JsonProperty(value = "if")
                              Condition condition) {
        _skillsGained = skills;
        _modifiedCardFilterBlueprint = modifiedCardFilterBlueprint;
        _condition = Objects.requireNonNullElse(condition, new TrueCondition());
    }

    public Modifier getModifier(ActionContext actionContext) {
        PhysicalCard thisCard = actionContext.getSource();
        Filterable affectFilter = _modifiedCardFilterBlueprint.getFilterable(actionContext);
        return new GainSkillModifier(thisCard, affectFilter, _condition, _skillsGained);
    }
}