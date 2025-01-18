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
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint155_009 extends CardBlueprint {

    // Microbrain
    Blueprint155_009() {
        super("155_009");
    }

    @Override
    public List<Action> getEncounterActions(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                            EncounterSeedCardAction action, MissionLocation missionLocation) {
        List<Action> result = new LinkedList<>();
        Collection<PersonnelCard> attemptingPersonnel = attemptingUnit.getAttemptingPersonnel();
        boolean nullified = false;

        // TODO - Be more explicit that this is a nullify condition
        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.getSkillLevel(SkillName.GEOLOGY) >= 3 ||
                    personnel.getSkillLevel(SkillName.EXOBIOLOGY) >= 3) {
                nullified = true;
                result.add(new RemoveDilemmaFromGameAction(attemptingUnit.getPlayer(), thisCard));
            }
        }

        if (!nullified) {
            Collection<PersonnelCard> eligiblePersonnelToStop = new LinkedList<>();
            for (PersonnelCard personnel : attemptingPersonnel) {
                if (personnel.hasSkill(SkillName.EXOBIOLOGY) || personnel.hasSkill(SkillName.GEOLOGY)) {
                    eligiblePersonnelToStop.add(personnel);
                }
            }
            if (eligiblePersonnelToStop.isEmpty()) {
                result.add(new FailDilemmaAction(attemptingUnit, thisCard));
            } else {
                SelectCardsAction selectAction = new SelectCardsFromDialogAction(thisCard.getOwner(),
                        "Select personnel to stop", Filters.in(eligiblePersonnelToStop)
                );
                result.add(selectAction);
                result.add(new StopCardsAction(thisCard.getOwner(), selectAction));
            }
        }
        return result;
    }

}