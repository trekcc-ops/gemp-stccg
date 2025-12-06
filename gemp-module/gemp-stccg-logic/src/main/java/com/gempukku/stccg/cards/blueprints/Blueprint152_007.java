package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.discard.RemoveCardFromPlayAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint152_007 extends CardBlueprint {
    Blueprint152_007() {
        super("152_007"); // Fractured Time
    }

    @Override
    public List<Action> getEncounterActionsFromJava(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                                    EncounterSeedCardAction action, MissionLocation missionLocation) {
        List<Action> result = new ArrayList<>();

        int personnelCount = attemptingUnit.getAttemptingPersonnel(game).size();

        if (personnelCount > 9) {
            int removeCount = personnelCount - 9;
            List<PersonnelCard> cardsToRemove =
                    TextUtils.getRandomItemsFromList(attemptingUnit.getAttemptingPersonnel(game), removeCount);
            for (PersonnelCard personnel : cardsToRemove)
                result.add(new RemoveCardFromPlayAction(game, thisCard.getOwnerName(), personnel));
        }
        result.add(new RemoveDilemmaFromGameAction(game, attemptingUnit.getControllerName(), thisCard));
        return result;
    }

}