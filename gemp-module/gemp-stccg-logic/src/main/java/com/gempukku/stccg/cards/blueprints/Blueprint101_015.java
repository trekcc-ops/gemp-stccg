package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectRandomCardAction;
import com.gempukku.stccg.actions.modifiers.KillSinglePersonnelAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint101_015 extends CardBlueprint {
    // A
    Blueprint101_015() {
        super("101_015"); // Armus - Skin of Evil
    }

    @Override
    public List<Action> getEncounterActions(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                            EncounterSeedCardAction action, MissionLocation missionLocation) {
        List<Action> actions = new LinkedList<>();
        SelectCardsAction selectPersonnelAction = new SelectRandomCardAction(game, thisCard.getOwner(),
                "Select personnel to be killed", Filters.personnelInAttemptingUnit(attemptingUnit));
        actions.add(new KillSinglePersonnelAction(thisCard.getOwner(), thisCard, selectPersonnelAction));
        actions.add(new RemoveDilemmaFromGameAction(attemptingUnit.getPlayer(), thisCard));
        return actions;
    }

}