package com.gempukku.stccg.condition.missionrequirements;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.CardAttribute;

import java.util.Collection;

public class AttributeMissionRequirement implements MissionRequirement {

    private final CardAttribute _attribute;
    private final int _value;
    public AttributeMissionRequirement(CardAttribute attribute, int value) {
        _attribute = attribute;
        _value = value;
    }
    @Override
    public boolean canBeMetBy(PersonnelCard personnel) {
        return personnel.getAttribute(_attribute) > _value;
    }

    @Override
    public boolean canBeMetBy(Collection<PersonnelCard> personnel) {
        int totalAttribute = 0;
        for (PersonnelCard card : personnel) {
            totalAttribute += card.getAttribute(_attribute);
        }
        return totalAttribute > _value;
    }

    public String toString() {
        return _attribute.name() + ">" + _value;
    }

}