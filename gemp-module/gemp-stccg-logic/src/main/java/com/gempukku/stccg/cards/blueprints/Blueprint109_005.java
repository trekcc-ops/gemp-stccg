package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.missionattempt.StopPersonnelAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Characteristic;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;

import java.util.*;

@SuppressWarnings("unused")
public class Blueprint109_005 extends CardBlueprint {
    Blueprint109_005() {
        super("109_005"); // Blended
    }

    @Override
    public List<Action> getEncounterActions(PhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                            MissionCard mission, EncounterSeedCardAction action) {
        List<Action> result = new LinkedList<>();
        Collection<PersonnelCard> targetPersonnel = new ArrayList<>();

        boolean hasEmpathyOrDiplomacy = attemptingUnit.hasSkill(SkillName.EMPATHY) ||
                attemptingUnit.hasSkill(SkillName.DIPLOMACY);

        for (PersonnelCard personnel : attemptingUnit.getAttemptingPersonnel()) {
            if (personnel.hasSkill(SkillName.EMPATHY) || personnel.hasSkill(SkillName.DIPLOMACY) ||
                    Objects.equals(personnel.getTitle(), "Morn") ||
                    personnel.hasCharacteristic(Characteristic.SCOTTY))
                targetPersonnel.add(personnel);
        }

        if (!hasEmpathyOrDiplomacy && targetPersonnel.isEmpty()) {
//            result.add(new FailMissionAction());
            result.add(new StopPersonnelAction(thisCard.getOwner(), attemptingUnit.getAttemptingPersonnel()));
            // fail, unit stopped, replace dilemma
        } else {
            if (targetPersonnel.size() >= 2) {
                PersonnelCard cardToContinue = TextUtils.getRandomItemFromList(targetPersonnel);
                targetPersonnel.remove(cardToContinue);
            }
            result.add(new StopPersonnelAction(thisCard.getOwner(), targetPersonnel));
            result.add(new RemoveDilemmaFromGameAction(attemptingUnit.getPlayer(), thisCard, mission));
        }

        return result;
    }

}