package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.KillAction;
import com.gempukku.stccg.actions.choose.SelectCardInPlayAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint101_015 extends CardBlueprint {
    Blueprint101_015() {
        super("101_015"); // Armus - Skin of Evil
    }

    @Override
    public List<Action> getEncounterActions(PhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                            MissionCard mission, EncounterSeedCardAction action) {
        List<Action> actions = new LinkedList<>();
        SelectCardInPlayAction selectPersonnelAction = new SelectCardInPlayAction(thisCard, thisCard.getOwner(),
                "Select personnel to be killed", attemptingUnit.getAttemptingPersonnel(), true);
        KillAction killAction = new KillAction(thisCard.getOwner(), thisCard, selectPersonnelAction);
        actions.add(killAction);
        actions.add(new RemoveDilemmaFromGameAction(attemptingUnit.getPlayer(), thisCard, mission));
        return actions;
    }

}