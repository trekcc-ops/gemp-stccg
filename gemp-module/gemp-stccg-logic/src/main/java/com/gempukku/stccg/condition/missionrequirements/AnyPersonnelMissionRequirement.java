package com.gempukku.stccg.condition.missionrequirements;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AnyPersonnelMissionRequirement implements MissionRequirement {

    @Override
    public boolean canBeMetBy(Collection<PersonnelCard> personnel, DefaultGame cardGame, GameTextContext context) {
        return !personnel.isEmpty();
    }

    public String toString() {
        return "Any personnel";
    }

    @Override
    public List<MissionRequirement> getRequirementOptionsWithoutOr() {
        return new ArrayList<>(List.of(this));
    }

}