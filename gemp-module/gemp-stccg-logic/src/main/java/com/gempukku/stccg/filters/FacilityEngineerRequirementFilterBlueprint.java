package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;

public class FacilityEngineerRequirementFilterBlueprint implements FilterBlueprint {

    private final FilterBlueprint _engineerBlueprint;

    public FacilityEngineerRequirementFilterBlueprint(FilterBlueprint engineerBlueprint) {
        _engineerBlueprint = engineerBlueprint;
    }
    @Override
    public CardFilter getFilterable(DefaultGame cardGame, ActionContext actionContext) {
        CardFilter engineerFilter = Filters.and(
                Filters.changeToFilter(SkillName.ENGINEER),
                _engineerBlueprint.getFilterable(cardGame, actionContext),
                Filters.your(actionContext.getPerformingPlayerId())
        );
        return new FacilityEngineerRequirementLocationFilter(engineerFilter);
    }
}