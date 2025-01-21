package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.*;
import com.gempukku.stccg.actions.modifiers.KillSinglePersonnelAction;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.modifiers.StopCardsAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.evaluator.SkillDotCountEvaluator;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint152_003 extends CardBlueprint {
    // Dedication to Duty
    Blueprint152_003() {
        super("152_003");
    }

    @Override
    public List<Action> getEncounterActions(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                            EncounterSeedCardAction action, MissionLocation missionLocation) {
        List<Action> result = new ArrayList<>();
        String opponentId = game.getOpponent(attemptingUnit.getPlayer().getPlayerId());

        // One unique personnel is "stopped" (random selection).
        List<PersonnelCard> uniquePersonnel = new ArrayList<>();
        for (PersonnelCard personnel : attemptingUnit.getAttemptingPersonnel()) {
            if (personnel.isUnique())
                uniquePersonnel.add(personnel);
        }

        SelectCardAction randomSelection =
                new SelectRandomCardAction(thisCard.getOwner(), "Choose a personnel to be stopped",
                        uniquePersonnel);
        Action stopAction = new StopCardsAction(thisCard.getOwner(), randomSelection);
        TopLevelSelectableAction action1 =
                new KillSinglePersonnelAction(thisCard.getOwner(), thisCard, randomSelection);
        SkillDotCountEvaluator skillDotEvaluator = new SkillDotCountEvaluator(randomSelection);
        TopLevelSelectableAction action2 =
                new DrawCardsAction(thisCard, game.getPlayer(opponentId), skillDotEvaluator);
        Action multipleChoiceDecision = new SelectAndInsertAction(action, attemptingUnit.getPlayer(),
                action1, action2);

        result.add(randomSelection);
        result.add(stopAction);
        result.add(multipleChoiceDecision);

        return result;
    }

}