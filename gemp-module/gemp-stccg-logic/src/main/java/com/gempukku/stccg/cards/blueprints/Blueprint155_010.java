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
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint155_010 extends CardBlueprint {
    Blueprint155_010() {
        super("155_010"); // Pinned Down
    }

    @Override
    public List<Action> getEncounterActionsFromJava(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                                    EncounterSeedCardAction action, MissionLocation missionLocation) {
        List<Action> result = new LinkedList<>();
        Collection<PersonnelCard> attemptingPersonnel = attemptingUnit.getAttemptingPersonnel();

        /* If Away Team/crew contains one personnel, Away Team/ship and crew is stopped.
            Replace dilemma under mission to be encountered again.
         */
        if (attemptingPersonnel.size() < 2) {
            result.add(new FailDilemmaAction(game, attemptingUnit, thisCard, action));
        }

        /* Randomly select two personnel to be stopped. If any [Q] card in play when dilemma is encountered,
            randomly select a third personnel to be stopped. Discard dilemma; mission continues.
         */
        // TODO - No need to add the Q card condition yet, since Q cards are not in the current card pool
        else {
            Collection<PersonnelCard> personnelToStop = TextUtils.getRandomItemsFromList(attemptingPersonnel, 2);
            result.add(new StopCardsAction(game, thisCard.getOwnerName(), personnelToStop));
            result.add(new RemoveDilemmaFromGameAction(game, attemptingUnit.getControllerName(), thisCard));
        }
        return result;
    }

}