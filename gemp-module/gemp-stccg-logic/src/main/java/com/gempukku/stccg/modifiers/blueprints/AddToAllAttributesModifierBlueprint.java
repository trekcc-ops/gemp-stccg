package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ModifierTimingType;
import com.gempukku.stccg.modifiers.attributes.AttributeModifier;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.requirement.Requirement;

import java.util.ArrayList;
import java.util.List;

import static com.gempukku.stccg.common.filterable.CardAttribute.*;

public class AddToAllAttributesModifierBlueprint implements ModifierBlueprint {

    private final FilterBlueprint _modifiedCardFilterBlueprint;
    private final List<Requirement> _requirements = new ArrayList<>();
    private final int _amount;
    private final boolean _isCumulative;

    AddToAllAttributesModifierBlueprint(@JsonProperty(value = "modifiedCards", required = true)
                              FilterBlueprint modifiedCardFilterBlueprint,
                                        @JsonProperty(value = "amount", required = true)
                             int amount,
                                        @JsonProperty(value = "ifCondition")
                                Requirement ifRequirement,
                                        @JsonProperty(value = "cumulative")
                                boolean isCumulative) {
        _amount = amount;
        _modifiedCardFilterBlueprint = modifiedCardFilterBlueprint;
        if (ifRequirement != null) {
            _requirements.add(ifRequirement);
        }
        _isCumulative = isCumulative;
    }

    public Modifier createModifier(DefaultGame cardGame, PhysicalCard thisCard, GameTextContext actionContext) {
        CardFilter affectFilter = _modifiedCardFilterBlueprint.getFilterable(cardGame, actionContext);
        Condition ifCondition = convertRequirementListToCondition(_requirements, actionContext, thisCard, cardGame);
        return new AttributeModifier(thisCard, affectFilter, ifCondition,
                new ConstantEvaluator(_amount),
                List.of(INTEGRITY, CUNNING, STRENGTH),
                ModifierTimingType.WHILE_IN_PLAY,
                _isCumulative);
    }


}