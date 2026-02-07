package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectCardsFromDialogAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.discard.KillSinglePersonnelAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.condition.missionrequirements.RegularSkillMissionRequirement;
import com.gempukku.stccg.filters.EncounteringCardFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint103_014 extends CardBlueprint {

    // Ferengi Attack
    Blueprint103_014() {
        super("103_014");
    }

    @Override
    public List<Action> getEncounterActionsFromJava(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                                    EncounterSeedCardAction action, MissionLocation missionLocation)
            throws PlayerNotFoundException {
        List<Action> result = new LinkedList<>();
        int totalCunning = 0;
        int totalStrength = 0;
        for (PersonnelCard card : attemptingUnit.getAttemptingPersonnel(game)) {
            totalCunning = totalCunning + card.getCunning(game);
            totalStrength = totalStrength + card.getStrength(game);
        }
        MissionRequirement condition = new RegularSkillMissionRequirement(SkillName.GREED);

        if ((totalCunning + totalStrength <= 68) && !condition.canBeMetBy(attemptingUnit.getAttemptingPersonnel(game), game)) {
            String opponentId = game.getOpponent(attemptingUnit.getControllerName());
            SelectCardsAction selectAction =
                    new SelectCardsFromDialogAction(game, game.getPlayer(opponentId),
                            "Select a personnel to kill",
                            new EncounteringCardFilter(thisCard));
            result.add(selectAction);
            result.add(new KillSinglePersonnelAction(game, thisCard.getOwnerName(), thisCard, selectAction));
        }

        result.add(new RemoveDilemmaFromGameAction(game, attemptingUnit.getControllerName(), thisCard));
        return result;
    }

}