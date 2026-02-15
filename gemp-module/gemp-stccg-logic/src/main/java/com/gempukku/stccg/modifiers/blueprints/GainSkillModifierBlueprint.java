package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.GainSkillModifier;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.requirement.Requirement;

import java.util.ArrayList;
import java.util.List;

public class GainSkillModifierBlueprint implements ModifierBlueprint {

    private final SkillName[] _skillsGained;
    private final FilterBlueprint _modifiedCardFilterBlueprint;
    private final List<Requirement> _requirements = new ArrayList<>();

    GainSkillModifierBlueprint(@JsonProperty(value = "skills", required = true)
                               @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                              SkillName[] skills,
                              @JsonProperty(value = "modifiedCards", required = true)
                              FilterBlueprint modifiedCardFilterBlueprint,
                               @JsonProperty(value = "ifCondition")
                               Requirement ifRequirement) {
        _skillsGained = skills;
        _modifiedCardFilterBlueprint = modifiedCardFilterBlueprint;
        if (ifRequirement != null) {
            _requirements.add(ifRequirement);
        }
    }

    public Modifier createModifier(DefaultGame cardGame, PhysicalCard thisCard, ActionContext actionContext) {
        Filterable affectFilter = _modifiedCardFilterBlueprint.getFilterable(cardGame, actionContext);
        Condition ifCondition = convertRequirementListToCondition(_requirements, actionContext, thisCard, cardGame);
        return new GainSkillModifier(thisCard, affectFilter, ifCondition, _skillsGained);
    }

}