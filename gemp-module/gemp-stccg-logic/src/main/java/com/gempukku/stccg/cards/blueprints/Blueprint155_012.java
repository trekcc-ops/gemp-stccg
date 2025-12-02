package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectCardsFromDialogAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.missionattempt.FailDilemmaAction;
import com.gempukku.stccg.actions.modifiers.StopCardsAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.filters.InCardListFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint155_012 extends CardBlueprint {
    Blueprint155_012() {
        super("155_012"); // Tense Negotiations
    }

    @Override
    public List<Action> getEncounterActionsFromJava(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                                    EncounterSeedCardAction action, MissionLocation missionLocation) {
        List<Action> result = new LinkedList<>();
        Collection<PersonnelCard> attemptingPersonnel = attemptingUnit.getAttemptingPersonnel();
        boolean nullified = false;

        // TODO - Be more explicit that this is a nullify condition
        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.getSkillLevel(SkillName.DIPLOMACY) >= 3 ||
                    personnel.getSkillLevel(SkillName.LEADERSHIP) >= 3) {
                nullified = true;
                result.add(new RemoveDilemmaFromGameAction(attemptingUnit.getControllerName(), thisCard));
            }
        }

        if (!nullified) {
            Collection<PersonnelCard> eligiblePersonnelToStop = new LinkedList<>();
            for (PersonnelCard personnel : attemptingPersonnel) {
                if (personnel.hasSkill(SkillName.DIPLOMACY) || personnel.hasSkill(SkillName.LEADERSHIP)) {
                    eligiblePersonnelToStop.add(personnel);
                }
            }
            if (eligiblePersonnelToStop.isEmpty()) {
                result.add(new FailDilemmaAction(attemptingUnit, thisCard, action));
            } else {
                SelectCardsAction selectAction = new SelectCardsFromDialogAction(game, thisCard.getOwnerName(),
                        "Select personnel to stop",
                        new InCardListFilter(eligiblePersonnelToStop));
                result.add(selectAction);
                result.add(new StopCardsAction(game, thisCard.getOwnerName(), selectAction));
            }
        }
        return result;
    }

}