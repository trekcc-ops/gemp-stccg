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
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.condition.missionrequirements.OrMissionRequirement;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint139_013 extends CardBlueprint {
    Blueprint139_013() {
        super("139_013"); // Picking Up the Pieces
    }

    @Override
    public List<Action> getEncounterActionsFromJava(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                                    EncounterSeedCardAction action, MissionLocation missionLocation) {

        List<Action> result = new LinkedList<>();
        MissionRequirement condition = new OrMissionRequirement(SkillName.GEOLOGY, SkillName.COMPUTER_SKILL);
        Collection<PersonnelCard> attemptingPersonnel = attemptingUnit.getAttemptingPersonnel();

        if (condition.canBeMetBy(attemptingUnit)) {
            Collection<PersonnelCard> allSuchPersonnel = new LinkedList<>();
            for (PersonnelCard card : attemptingPersonnel) {
                if (card.hasSkill(SkillName.GEOLOGY) || card.hasSkill(SkillName.COMPUTER_SKILL)) {
                    allSuchPersonnel.add(card);
                }
            }
            if (allSuchPersonnel.size() >= 2) {
                PersonnelCard personnelToContinue = TextUtils.getRandomItemFromList(allSuchPersonnel);
                allSuchPersonnel.remove(personnelToContinue);
            }
            result.add(new StopCardsAction(game, thisCard.getOwnerName(), allSuchPersonnel));
            result.add(new RemoveDilemmaFromGameAction(game, attemptingUnit.getControllerName(), thisCard));
        } else {
            result.add(new FailDilemmaAction(game, attemptingUnit, thisCard, action, false));
        }

        return result;
    }

}