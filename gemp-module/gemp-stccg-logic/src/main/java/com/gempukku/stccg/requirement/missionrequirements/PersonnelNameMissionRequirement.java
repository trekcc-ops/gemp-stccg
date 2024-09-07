package com.gempukku.stccg.requirement.missionrequirements;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.PersonnelName;

import java.util.Collection;
import java.util.Objects;

public class PersonnelNameMissionRequirement implements MissionRequirement {

    private final PersonnelName _name;
    public PersonnelNameMissionRequirement(PersonnelName name) {
        _name = name;
    }

    @Override
    public boolean canBeMetBy(PersonnelCard personnel) {
        return Objects.equals(personnel.getTitle(), _name.getHumanReadable());
    }

    @Override
    public boolean canBeMetBy(Collection<PersonnelCard> personnel) {
        return personnel.stream().anyMatch(card -> Objects.equals(card.getTitle(), _name.getHumanReadable()));
    }

}