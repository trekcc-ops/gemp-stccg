package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class MissionSpecialistFilter implements CardFilter {

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        if (physicalCard instanceof PersonnelCard personnel) {
            return personnel.getSkills(game).size() == 1 &&
                    personnel.getSkills(game).getFirst() instanceof RegularSkill;
        } else {
            return false;
        }
    }
}