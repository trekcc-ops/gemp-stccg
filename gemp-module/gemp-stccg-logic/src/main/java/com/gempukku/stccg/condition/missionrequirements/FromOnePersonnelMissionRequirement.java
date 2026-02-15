package com.gempukku.stccg.condition.missionrequirements;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.List;

public class FromOnePersonnelMissionRequirement implements MissionRequirement {

    private final MissionRequirement _requirement;
    private final int _numberOfPersonnelRequired;
    public FromOnePersonnelMissionRequirement(MissionRequirement requirement, int numberOfPersonnelRequired) {
        _requirement = requirement;
        _numberOfPersonnelRequired = numberOfPersonnelRequired;
    }

    @Override
    public boolean canBeMetBy(Collection<PersonnelCard> personnel, DefaultGame cardGame) {
        int numberWithRequirement = 0;
        for (PersonnelCard card : personnel) {
            if (_requirement.canBeMetBy(List.of(card), cardGame)) {
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

}