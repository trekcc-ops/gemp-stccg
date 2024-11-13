package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.condition.missionrequirements.AndMissionRequirement;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint101_014 extends CardBlueprint {
    Blueprint101_014() {
        super("101_012"); // Archer
    }

    @Override
    public List<Action> getEncounterActions(PhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                            MissionCard mission, EncounterSeedCardAction action) {
        List<Action> result = new LinkedList<>();
        MissionRequirement conditions = new AndMissionRequirement(SkillName.MEDICAL, SkillName.SECURITY);
        if (conditions.canBeMetBy(attemptingUnit.getAttemptingPersonnel())) {
            result.add(new SystemQueueAction(game) {

                @Override
                public Action nextAction(DefaultGame cardGame) {
                    mission.removeSeedCard(thisCard);
                    game.getGameState().removeCardFromZone(thisCard);
                    game.getGameState().addCardToZone(thisCard, Zone.REMOVED);
                    return getNextAction();
                }

            });

        } else {
            // TODO
/*            Collection<PersonnelCard> females = Filters.filter(attemptingUnit.getAttemptingPersonnel(), Filters.female);
            Collection<PersonnelCard> highestFemales = Filters.highestTotalAttributes(females);
            if (!highestFemales.isEmpty()) {
                SelectCardInPlayAction selectPersonnelAction = new SelectCardInPlayAction(thisCard, thisCard.getOwner(),
                        "Select personnel to be killed", highestFemales);
                KillAction killAction = new KillAction(thisCard.getOwner(), thisCard, selectPersonnelAction);
                result.add(killAction);
            }
            result.add(new StopAction(attemptingUnit)); */
        }
        return result;
    }

}