package com.gempukku.stccg.condition.missionrequirements;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringJoiner;

public class MultipleAttributeMissionRequirement implements MissionRequirement {

    private final Collection<CardAttribute> _attributes;
    private final int _value;
    public MultipleAttributeMissionRequirement(Collection<CardAttribute> attributes, int value) {
        _attributes = new ArrayList<>(attributes);
        _value = value;
    }

    @Override
    public boolean canBeMetBy(Collection<PersonnelCard> personnel, DefaultGame cardGame, GameTextContext context) {
        int totalAttribute = 0;
        for (PersonnelCard card : personnel) {
            for (CardAttribute attribute : _attributes) {
                totalAttribute += card.getAttribute(attribute, cardGame);
            }
        }
        return totalAttribute > _value;
    }

    public String toString() {
        StringJoiner sj = new StringJoiner("+");
        for (CardAttribute attribute : _attributes) {
            sj.add(attribute.toString());
        }
        return sj.toString() + _value;
    }

}