package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectCardsFromDialogAction;
import com.gempukku.stccg.actions.modifiers.KillSinglePersonnelAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.condition.missionrequirements.RegularSkillMissionRequirement;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint103_014 extends CardBlueprint {

    // Ferengi Attack
    Blueprint103_014() {
        super("103_014");
    }

    @Override
    public List<Action> getEncounterActions(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                            EncounterSeedCardAction action, MissionLocation missionLocation) {
        List<Action> result = new LinkedList<>();
        int totalCunning = 0;
        int totalStrength = 0;
        for (PersonnelCard card : attemptingUnit.getAttemptingPersonnel()) {
            totalCunning = totalCunning + card.getAttribute(CardAttribute.CUNNING);
            totalStrength = totalStrength + card.getAttribute(CardAttribute.STRENGTH);
        }
        MissionRequirement condition = new RegularSkillMissionRequirement(SkillName.GREED);

        if ((totalCunning + totalStrength <= 68) && !condition.canBeMetBy(attemptingUnit)) {
            String opponentId = game.getOpponent(attemptingUnit.getPlayer().getPlayerId());
            SelectCardsAction selectAction =
                    new SelectCardsFromDialogAction(thisCard, game.getPlayer(opponentId),
                            "Select a personnel to kill",
                    attemptingUnit.getAttemptingPersonnel());
            result.add(selectAction);
            result.add(new KillSinglePersonnelAction(thisCard.getOwner(), thisCard, selectAction));
        }

        result.add(new RemoveDilemmaFromGameAction(attemptingUnit.getPlayer(), thisCard, missionLocation));
        return result;
    }

}