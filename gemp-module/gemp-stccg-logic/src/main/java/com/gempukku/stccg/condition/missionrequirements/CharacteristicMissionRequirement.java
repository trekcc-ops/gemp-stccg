package com.gempukku.stccg.condition.missionrequirements;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.Characteristic;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class CharacteristicMissionRequirement implements MissionRequirement {

    private final Characteristic _characteristic;
    public CharacteristicMissionRequirement(Characteristic characteristic) {
        _characteristic = characteristic;
    }

    @Override
    public boolean canBeMetBy(Collection<PersonnelCard> personnel, DefaultGame cardGame) {
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