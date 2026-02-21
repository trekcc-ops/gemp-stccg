package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;

public class FacilityEngineerRequirementFilterBlueprint implements FilterBlueprint {

    private final FilterBlueprint _engineerBlueprint;

    public FacilityEngineerRequirementFilterBlueprint(FilterBlueprint engineerBlueprint) {
        _engineerBlueprint = engineerBlueprint;
    }
    @Override
    public CardFilter getFilterable(DefaultGame cardGame, GameTextContext actionContext) {
        CardFilter engineerFilter = Filters.and(
                Filters.changeToFilter(SkillName.ENGINEER),
                _engineerBlueprint.getFilterable(cardGame, actionContext),
                Filters.your(actionContext.yourName())
        );
        return new FacilityEngineerRequirementLocationFilter(engineerFilter);
    }
}