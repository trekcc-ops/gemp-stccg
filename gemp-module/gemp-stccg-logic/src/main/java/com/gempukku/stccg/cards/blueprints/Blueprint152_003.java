package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint152_003 extends CardBlueprint {
    Blueprint152_003() {
        super("152_003"); // Dedication to Duty
    }

    @Override
    public List<Action> getEncounterActions(PhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                            MissionCard mission, EncounterSeedCardAction action) {
        List<Action> result = new ArrayList<>();
/*
        // One unique personnel is "stopped" (random selection).
        List<PersonnelCard> uniquePersonnel = new ArrayList<>();
        for (PersonnelCard personnel : attemptingUnit.getAttemptingPersonnel()) {
            if (personnel.isUnique())
                uniquePersonnel.add(personnel);
        }
        SelectCardsInPlayAction randomSelection =
                new SelectCardsInPlayAction(thisCard, thisCard.getOwner(), "Choose a personnel to be stopped",
                        uniquePersonnel, true);
        Action stopAction = new StopPersonnelAction(thisCard.getOwner(), randomSelection);
        Action action1 = new KillAction(thisCard.getOwner(), thisCard, randomSelection);
        Action action2 = new DrawCardsAction(thisCard.getOwner(), new SkillDotEvaluator(randomSelection));
        Action multipleChoiceDecision = new ChooseAnActionFromMultipleChoice(action1, action2);

        Collection<PersonnelCard> targetPersonnel = new ArrayList<>();
*/

        return result;
    }

}