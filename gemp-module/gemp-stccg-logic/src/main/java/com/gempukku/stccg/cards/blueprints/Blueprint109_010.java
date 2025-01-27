package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.missionattempt.FailDilemmaAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint109_010 extends CardBlueprint {
    Blueprint109_010() {
        super("109_010"); // Maglock
    }

    @Override
    public List<Action> getEncounterActions(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                            EncounterSeedCardAction action, MissionLocation missionLocation) {

        List<Action> result = new LinkedList<>();
        Collection<PersonnelCard> attemptingPersonnel = attemptingUnit.getAttemptingPersonnel();
        Collection<PersonnelCard> officers = new LinkedList<>();

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.hasSkill(SkillName.OFFICER) && personnel.getAttribute(CardAttribute.STRENGTH) > 5) {
                officers.add(personnel);
            }
        }

        if (officers.size() >= 3) {
            result.add(new RemoveDilemmaFromGameAction(attemptingUnit.getPlayer(), thisCard));
        } else {
            result.add(new FailDilemmaAction(attemptingUnit, thisCard, action));
        }

        return result;
    }

}