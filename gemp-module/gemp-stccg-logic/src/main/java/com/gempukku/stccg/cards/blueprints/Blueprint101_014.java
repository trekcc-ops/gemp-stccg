package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.KillSinglePersonnelAction;
import com.gempukku.stccg.actions.choose.SelectCardInPlayAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.missionattempt.FailDilemmaAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.missionrequirements.AndMissionRequirement;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint101_014 extends CardBlueprint {
    Blueprint101_014() {
        super("101_014"); // Archer
    }

    @Override
    public List<Action> getEncounterActions(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                            MissionCard mission, EncounterSeedCardAction action) {
        List<Action> result = new LinkedList<>();
        MissionRequirement conditions = new AndMissionRequirement(SkillName.MEDICAL, SkillName.SECURITY);
        if (!conditions.canBeMetBy(attemptingUnit)) {
            Collection<PersonnelCard> highestPersonnel =
                    Filters.highestTotalAttributes(attemptingUnit.getAttemptingPersonnel());
            SelectCardInPlayAction selectAction =
                    new SelectCardInPlayAction(action, thisCard.getOwner(), "Select a personnel to kill",
                            highestPersonnel);
            result.add(selectAction);
            result.add(new KillSinglePersonnelAction(thisCard.getOwner(), thisCard, selectAction));
            result.add(new FailDilemmaAction(attemptingUnit, thisCard, action));
        }
        result.add(new RemoveDilemmaFromGameAction(attemptingUnit.getPlayer(), thisCard, mission));
        return result;
    }

}