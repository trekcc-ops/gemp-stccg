package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class PersonnelInAttemptingUnitFilter implements CardFilter {

    private final AttemptingUnit _unit;
    public PersonnelInAttemptingUnitFilter(AttemptingUnit unit) {
        _unit = unit;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        Collection<PersonnelCard> personnel = _unit.getAttemptingPersonnel();
        return physicalCard instanceof PersonnelCard personnelCard && personnel.contains(personnelCard);
    }
}