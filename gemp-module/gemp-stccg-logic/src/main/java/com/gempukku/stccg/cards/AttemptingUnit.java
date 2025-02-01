package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.Collection;
import java.util.LinkedList;

public interface AttemptingUnit {
    Collection<PersonnelCard> getAllPersonnel();
    Player getPlayer();
    default Collection<PersonnelCard> getAttemptingPersonnel() {
        // TODO - Does not include a check for infiltrators
        Collection<PersonnelCard> personnelAttempting = new LinkedList<>();
        for (PhysicalCard card : getAllPersonnel()) {
            if (card instanceof PersonnelCard personnel)
                if (!personnel.isStopped() && !personnel.isDisabled() && !personnel.isInStasis() &&
                        personnel.getCurrentAffiliation() != Affiliation.BORG)
                    personnelAttempting.add(personnel);
        }
        return personnelAttempting;
    }

    default boolean hasSkill(SkillName skillName) {
        boolean result = false;
        for (PersonnelCard personnel : getAttemptingPersonnel()) {
            if (personnel.hasSkill(skillName))
                result = true;
        }
        return result;
    }

}