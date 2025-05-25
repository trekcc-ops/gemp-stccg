package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.RequiredType;
import com.gempukku.stccg.requirement.PlayPhaseRequirement;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;

import java.util.List;

public abstract class TriggerActionBlueprint extends DefaultActionBlueprint {

    protected TriggerActionBlueprint(String text, int limitPerTurn,
                                     TriggerChecker triggerChecker, List<Requirement> requirements,
                                     List<SubActionBlueprint> costs, List<SubActionBlueprint> effects,
                                     boolean triggerDuringSeed)
            throws InvalidCardDefinitionException {
        super(text, limitPerTurn);
        if (requirements != null) {
            _requirements.addAll(requirements);
        }
        if (triggerChecker != null) {
            _requirements.add(triggerChecker);
        }
        if (!triggerDuringSeed) {
            _requirements.add(new PlayPhaseRequirement());
        }
        addCostsAndEffects(costs, effects);
    }

}