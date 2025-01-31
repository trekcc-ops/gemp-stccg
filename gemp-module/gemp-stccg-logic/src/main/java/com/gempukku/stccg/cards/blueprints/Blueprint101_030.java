package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.missionattempt.OvercomeDilemmaConditionAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.condition.missionrequirements.RegularSkillMissionRequirement;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.List;

@SuppressWarnings("unused")
public class Blueprint101_030 extends CardBlueprint {

    // Impassable Door
    Blueprint101_030() {
        super("101_030");
    }

    @Override
    public List<Action> getEncounterActionsFromJava(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                                    EncounterSeedCardAction action, MissionLocation missionLocation) {
        MissionRequirement condition = new RegularSkillMissionRequirement(SkillName.COMPUTER_SKILL);
        Action overcomeAction = new OvercomeDilemmaConditionAction(thisCard, action, condition, attemptingUnit);
        return List.of(overcomeAction);
    }

}