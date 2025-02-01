package com.gempukku.stccg.condition.missionrequirements;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.Characteristic;

import java.util.Collection;

public class CharacteristicMissionRequirement implements MissionRequirement {

    private final Characteristic _characteristic;
    public CharacteristicMissionRequirement(Characteristic characteristic) {
        _characteristic = characteristic;
    }
    @Override
    public boolean canBeMetBy(PersonnelCard personnel) {
        return personnel.hasCharacteristic(_characteristic);
    }

    @Override
    public boolean canBeMetBy(Collection<PersonnelCard> personnel) {
        boolean result = false;
        for (PersonnelCard card : personnel) {
            if (card.hasCharacteristic(_characteristic)) {
                result = true;
            }
        }
        return result;
    }

    public String toString() {
        return "any " + _characteristic.getHumanReadable();
    }

}