package com.gempukku.stccg.condition.missionrequirements;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.Species;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class SpeciesMissionRequirement implements MissionRequirement {

    private final Species _species;

    public SpeciesMissionRequirement(Species species) {
        _species = species;
    }

    @Override
    public boolean canBeMetBy(Collection<PersonnelCard> personnel, DefaultGame cardGame, GameTextContext context) {
        for (PersonnelCard personnelCard : personnel) {
            if (personnelCard.isSpecies(_species)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return _species.getHumanReadable();
    }

}