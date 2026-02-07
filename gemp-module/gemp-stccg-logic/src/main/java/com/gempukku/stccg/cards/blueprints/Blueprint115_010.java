package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.placecard.PlaceCardOnMissionAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint115_010 extends CardBlueprint {

    // Friendly Fire
    Blueprint115_010() {
        super("115_010");
    }

    @Override
    public List<Action> getEncounterActionsFromJava(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                                    EncounterSeedCardAction action, MissionLocation missionLocation) {

        // TODO - This is a dummy card definition to test out the "place on mission" feature
        List<Action> result = new LinkedList<>();
        result.add(new PlaceCardOnMissionAction(game, attemptingUnit.getControllerName(), thisCard, missionLocation));
        return result;
    }

}