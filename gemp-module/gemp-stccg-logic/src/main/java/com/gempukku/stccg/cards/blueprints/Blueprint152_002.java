package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.missionattempt.FailDilemmaAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.missionrequirements.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint152_002 extends CardBlueprint {
    Blueprint152_002() {
        super("152_002"); // Dangerous Climb
    }

    @Override
    public List<Action> getEncounterActions(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                            EncounterSeedCardAction action, MissionLocation missionLocation) {
        List<Action> result = new ArrayList<>();
        MissionRequirement condition1 = new AttributeMissionRequirement(CardAttribute.STRENGTH, 40);
        MissionRequirement condition2 = new AndMissionRequirement(
                new RegularSkillMissionRequirement(SkillName.GEOLOGY, 2),
                new AttributeMissionRequirement(CardAttribute.CUNNING, 20)
        );
        MissionRequirement fullCondition = new OrMissionRequirement(condition1, condition2);

        if (fullCondition.canBeMetBy(attemptingUnit)) {
            result.add(new RemoveDilemmaFromGameAction(attemptingUnit.getPlayer(), thisCard, missionLocation));
        } else {
            result.add(new FailDilemmaAction(attemptingUnit, thisCard));
        }

        return result;
    }

}