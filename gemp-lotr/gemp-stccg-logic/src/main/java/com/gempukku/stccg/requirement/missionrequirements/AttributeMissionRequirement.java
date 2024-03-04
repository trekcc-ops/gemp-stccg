package com.gempukku.stccg.requirement.missionrequirements;

import com.gempukku.stccg.cards.physicalcard.PhysicalPersonnelCard;
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
    public boolean canBeMetBy(PhysicalPersonnelCard personnel) {
        return personnel.getAttribute(_attribute) > _value;
    }

    @Override
    public boolean canBeMetBy(Collection<PhysicalPersonnelCard> personnel) {
        int totalAttribute = 0;
        for (PhysicalPersonnelCard card : personnel) {
            totalAttribute += card.getAttribute(_attribute);
        }
        return totalAttribute > _value;
    }

}