package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.missionattempt.FailDilemmaAction;
import com.gempukku.stccg.actions.placecard.PlaceCardOnTopOfDrawDeckAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.missionrequirements.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint116_008 extends CardBlueprint {
    Blueprint116_008() {
        super("116_008"); // New Essentialists
    }

    @Override
    public List<Action> getEncounterActionsFromJava(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                                    EncounterSeedCardAction action, MissionLocation missionLocation) {

        List<Action> result = new LinkedList<>();

        MissionRequirement condition1 = new AndMissionRequirement(
                new AttributeMissionRequirement(CardAttribute.INTEGRITY, 40),
                new RegularSkillMissionRequirement(SkillName.HONOR, 2)
        );
        MissionRequirement condition2 = new AndMissionRequirement(
                new AttributeMissionRequirement(CardAttribute.CUNNING, 40),
                new RegularSkillMissionRequirement(SkillName.TREACHERY, 2)
        );
        MissionRequirement fullCondition = new OrMissionRequirement(condition1, condition2);

        if (fullCondition.canBeMetBy(attemptingUnit)) {
            // overcome
            result.add(new RemoveDilemmaFromGameAction(game, attemptingUnit.getControllerName(), thisCard));
        } else {
            // fail
            PhysicalCard randomCard = TextUtils.getRandomItemFromList(attemptingUnit.getAttemptingPersonnel());
            result.add(new PlaceCardOnTopOfDrawDeckAction(game, attemptingUnit.getControllerName(), randomCard));
            result.add(new FailDilemmaAction(game, attemptingUnit, thisCard, action));
        }

        return result;
    }

}