package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.Characteristic;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.missionrequirements.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint152_004 extends CardBlueprint {
    Blueprint152_004() {
        super("152_004"); // Dignitaries and Witnesses
    }

    @Override
    public List<Action> getEncounterActionsFromJava(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                                    EncounterSeedCardAction action, MissionLocation missionLocation) {
        MissionRequirement condition1 = new AndMissionRequirement(
                new AttributeMissionRequirement(CardAttribute.INTEGRITY, 20),
                new CharacteristicMissionRequirement(Characteristic.ADMIRAL)
        );
        MissionRequirement condition2 = new AndMissionRequirement(
                new AttributeMissionRequirement(CardAttribute.STRENGTH, 30),
                new CharacteristicMissionRequirement(Characteristic.GENERAL)
        );
        MissionRequirement condition3 = new AndMissionRequirement(
                new AttributeMissionRequirement(CardAttribute.CUNNING, 30),
                new RegularSkillMissionRequirement(SkillName.LEADERSHIP, 3)
        );
        MissionRequirement condition4 = new AndMissionRequirement(
                new AttributeMissionRequirement(CardAttribute.STRENGTH, 20),
                new CharacteristicMissionRequirement(Characteristic.MAJE)
        );
        MissionRequirement fullCondition = new OrMissionRequirement(condition1, condition2, condition3, condition4);

/*        Action overcomeAction =
                new OvercomeDilemmaConditionAction(game, thisCard, action, fullCondition, attemptingUnit); */
        return new ArrayList<>();
    }

}