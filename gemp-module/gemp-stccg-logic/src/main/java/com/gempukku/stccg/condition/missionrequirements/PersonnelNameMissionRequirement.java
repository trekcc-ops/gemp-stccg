package com.gempukku.stccg.condition.missionrequirements;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.PersonnelName;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.Objects;

public class PersonnelNameMissionRequirement implements MissionRequirement {

    private final String _name;
    public PersonnelNameMissionRequirement(PersonnelName name) {
        _name = name.getHumanReadable();
    }
    public PersonnelNameMissionRequirement(String name) {
        _name = name;
    }

    @Override
    public boolean canBeMetBy(Collection<PersonnelCard> personnel, DefaultGame cardGame) {
        return personnel.stream().anyMatch(card -> Objects.equals(card.getTitle(), _name));
    }

    public String toString() {
        return _name;
    }

}