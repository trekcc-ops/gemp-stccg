package com.gempukku.stccg.condition.missionrequirements;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class AttributeMissionRequirement implements MissionRequirement {

    private final CardAttribute _attribute;
    private final int _value;
    public AttributeMissionRequirement(CardAttribute attribute, int value) {
        _attribute = attribute;
        _value = value;
    }

    @Override
    public boolean canBeMetBy(Collection<PersonnelCard> personnel, DefaultGame cardGame) {
        int totalAttribute = 0;
        for (PersonnelCard card : personnel) {
            totalAttribute += card.getAttribute(_attribute, cardGame);
        }
        return totalAttribute > _value;
    }

    public String toString() {
        return _attribute.name() + ">" + _value;
    }

}