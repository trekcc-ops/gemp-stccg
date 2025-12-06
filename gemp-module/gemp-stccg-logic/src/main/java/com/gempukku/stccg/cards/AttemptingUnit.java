package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.LinkedList;

public interface AttemptingUnit {
    Collection<PersonnelCard> getAllPersonnel(DefaultGame cardGame);

    String getControllerName();

    default Collection<PersonnelCard> getAttemptingPersonnel(DefaultGame cardGame) {
        // TODO - Does not include a check for infiltrators
        Collection<PersonnelCard> personnelAttempting = new LinkedList<>();
        for (PhysicalCard card : getAllPersonnel(cardGame)) {
            if (card instanceof PersonnelCard personnel)
                if (!personnel.isStopped() && !personnel.isDisabled() && !personnel.isInStasis() &&
                        !personnel.isAffiliation(Affiliation.BORG))
                    personnelAttempting.add(personnel);
        }
        return personnelAttempting;
    }

    default boolean hasSkill(SkillName skillName, DefaultGame cardGame) {
        boolean result = false;
        for (PersonnelCard personnel : getAttemptingPersonnel(cardGame)) {
            if (personnel.hasSkill(skillName, cardGame))
                result = true;
        }
        return result;
    }

}