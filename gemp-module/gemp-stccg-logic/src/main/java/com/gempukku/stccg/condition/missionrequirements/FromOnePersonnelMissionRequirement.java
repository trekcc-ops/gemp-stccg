package com.gempukku.stccg.condition.missionrequirements;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.OrFilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FromOnePersonnelMissionRequirement implements MissionRequirement {
    private final FilterBlueprint _requirement;
    private final int _numberOfPersonnelRequired;
    public FromOnePersonnelMissionRequirement(FilterBlueprint requirement, int numberOfPersonnelRequired) {
        _requirement = requirement;
        _numberOfPersonnelRequired = numberOfPersonnelRequired;
    }

    public FromOnePersonnelMissionRequirement(FilterBlueprint requirement) {
        _requirement = requirement;
        _numberOfPersonnelRequired = 1;
    }

    @Override
    public boolean canBeMetBy(Collection<PersonnelCard> personnel, DefaultGame cardGame, GameTextContext context) {
        int numberWithRequirement = 0;
        CardFilter filter = _requirement.getFilterable(cardGame, context);
        for (PersonnelCard card : personnel) {
            if (filter.accepts(cardGame, card)) {
                numberWithRequirement++;
            }
        }
        return numberWithRequirement >= _numberOfPersonnelRequired;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("personnelWith(").append(_requirement.toString()).append(")");
        if (_numberOfPersonnelRequired > 1)
            sb.append(" x").append(_numberOfPersonnelRequired);
        return sb.toString();
    }

    @Override
    public boolean requiresSkill(SkillName skillName, DefaultGame cardGame, GameTextContext context) {
        CardFilter filter = _requirement.getFilterable(cardGame, context);
        return filter.requiresSkill(skillName);
    }

    @Override
    public List<MissionRequirement> getRequirementOptionsWithoutOr() {
        List<MissionRequirement> result = new ArrayList<>();
        if (_requirement instanceof OrFilterBlueprint orBlueprint) {
            for (FilterBlueprint individualFilter : orBlueprint.getOptions()) {
                result.add(new FromOnePersonnelMissionRequirement(individualFilter));
            }
        } else {
            result.add(this);
        }
        return result;
    }

}