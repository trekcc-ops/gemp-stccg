package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.choose.SelectRandomCardAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.missionattempt.OvercomeDilemmaConditionAction;
import com.gempukku.stccg.actions.discard.KillSinglePersonnelAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.missionrequirements.*;
import com.gempukku.stccg.filters.EncounteringCardFilter;
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
    public List<Action> getEncounterActionsFromJava(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                                    EncounterSeedCardAction action, MissionLocation missionLocation) {
        List<Action> result = new ArrayList<>();
        MissionRequirement condition1 = new AttributeMissionRequirement(CardAttribute.STRENGTH, 40);
        MissionRequirement condition2 = new AndMissionRequirement(
                new RegularSkillMissionRequirement(SkillName.GEOLOGY, 2),
                new AttributeMissionRequirement(CardAttribute.CUNNING, 20)
        );
        MissionRequirement fullCondition = new OrMissionRequirement(condition1, condition2);
        KillSinglePersonnelAction killAction = new KillSinglePersonnelAction(game, thisCard.getOwnerName(), thisCard,
                new SelectRandomCardAction(game, thisCard.getOwnerName(),
                        new EncounteringCardFilter(thisCard)));

        OvercomeDilemmaConditionAction overcomeAction =
                new OvercomeDilemmaConditionAction(game, thisCard, action, fullCondition, attemptingUnit, killAction);
        result.add(overcomeAction);
        return result;
    }

}