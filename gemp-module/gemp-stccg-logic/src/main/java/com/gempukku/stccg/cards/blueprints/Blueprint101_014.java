package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.modifiers.KillSinglePersonnelAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.missionattempt.OvercomeDilemmaConditionAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.missionrequirements.AndMissionRequirement;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint101_014 extends CardBlueprint {
    Blueprint101_014() {
        super("101_014"); // Archer
    }

    @Override
    public List<Action> getEncounterActionsFromJava(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                                    EncounterSeedCardAction action, MissionLocation missionLocation)
            throws PlayerNotFoundException {
        List<Action> result = new LinkedList<>();
        MissionRequirement conditions = new AndMissionRequirement(SkillName.MEDICAL, SkillName.SECURITY);

        Collection<PersonnelCard> highestPersonnel =
                Filters.highestTotalAttributes(attemptingUnit.getAttemptingPersonnel(game), game);
        SelectVisibleCardAction selectAction =
                new SelectVisibleCardAction(game, game.getOpponent(attemptingUnit.getControllerName()),
                        "Select a personnel to kill", highestPersonnel);
        result.add(selectAction);
        Action killAction = new KillSinglePersonnelAction(game, thisCard.getOwnerName(), thisCard, selectAction);
        Action overcomeAction =
                new OvercomeDilemmaConditionAction(game, thisCard, action, conditions, attemptingUnit, killAction);
        result.add(overcomeAction);
        return result;
    }

}