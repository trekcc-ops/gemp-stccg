package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.RequiredType;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;

import java.util.List;

public abstract class TriggerActionBlueprint extends DefaultActionBlueprint {

    private final RequiredType _requiredType;

    protected TriggerActionBlueprint(RequiredType requiredType, String text, int limitPerTurn, Phase phase,
                                     TriggerChecker triggerChecker, List<Requirement> requirements,
                                     List<SubActionBlueprint> costs, List<SubActionBlueprint> effects)
            throws InvalidCardDefinitionException {
        super(text, limitPerTurn, phase);
        _requiredType = requiredType;
        addRequirement(triggerChecker);
        processRequirementsCostsAndEffects(requirements, costs, effects);
    }


    public RequiredType getRequiredType() { return _requiredType; }

}