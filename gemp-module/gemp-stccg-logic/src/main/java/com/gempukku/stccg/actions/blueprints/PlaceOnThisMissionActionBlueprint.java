package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.placecard.PlaceCardOnMissionAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.blueprints.ModifierBlueprint;

public class PlaceOnThisMissionActionBlueprint implements SubActionBlueprint {

    private final ModifierBlueprint _modifier;

    private PlaceOnThisMissionActionBlueprint(@JsonProperty("modifier")ModifierBlueprint modifier) {
        _modifier = modifier;
    }
    @Override
    public PlaceCardOnMissionAction createAction(DefaultGame cardGame, GameTextContext actionContext) {
        PhysicalCard card = actionContext.card();
        int locationId = card.getLocationId();
        if (_modifier != null) {
            Modifier modifier = _modifier.createModifier(cardGame, actionContext.card(), actionContext);
            return new PlaceCardOnMissionAction(cardGame, actionContext.yourName(),
                    actionContext.card(), locationId, modifier);
        } else {
            return new PlaceCardOnMissionAction(cardGame, actionContext.yourName(),
                    actionContext.card(), locationId);
        }
    }
}