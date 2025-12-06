package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.missionattempt.FailDilemmaAction;
import com.gempukku.stccg.actions.modifiers.StopCardsAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Characteristic;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.*;

@SuppressWarnings("unused")
public class Blueprint109_005 extends CardBlueprint {
    Blueprint109_005() {
        super("109_005"); // Blended
    }

    @Override
    public List<Action> getEncounterActionsFromJava(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                                    EncounterSeedCardAction action, MissionLocation missionLocation) {
        List<Action> result = new LinkedList<>();
        Collection<PersonnelCard> targetPersonnel = new ArrayList<>();

        boolean hasEmpathyOrDiplomacy = attemptingUnit.hasSkill(SkillName.EMPATHY, game) ||
                attemptingUnit.hasSkill(SkillName.DIPLOMACY, game);

        for (PersonnelCard personnel : attemptingUnit.getAttemptingPersonnel(game)) {
            if (personnel.hasSkill(SkillName.EMPATHY, game) || personnel.hasSkill(SkillName.DIPLOMACY, game) ||
                    Objects.equals(personnel.getTitle(), "Morn") ||
                    personnel.hasCharacteristic(Characteristic.SCOTTY))
                targetPersonnel.add(personnel);
        }

        if (!hasEmpathyOrDiplomacy && targetPersonnel.isEmpty()) {
            result.add(new FailDilemmaAction(game, attemptingUnit, thisCard, action));
        } else {
            if (targetPersonnel.size() >= 2) {
                PersonnelCard cardToContinue = TextUtils.getRandomItemFromList(targetPersonnel);
                targetPersonnel.remove(cardToContinue);
            }
            result.add(new StopCardsAction(game, thisCard.getOwnerName(), targetPersonnel));
            result.add(new RemoveDilemmaFromGameAction(game, attemptingUnit.getControllerName(), thisCard));
        }

        return result;
    }

}