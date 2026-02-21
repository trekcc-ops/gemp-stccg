package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.placecard.PlaceCardOnMissionAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.blueprints.ModifierBlueprint;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class PlaceOnThisMissionActionBlueprint implements SubActionBlueprint {

    private final ModifierBlueprint _modifier;

    private PlaceOnThisMissionActionBlueprint(@JsonProperty("modifier")ModifierBlueprint modifier) {
        _modifier = modifier;
    }
    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions parentAction,
                                      GameTextContext actionContext)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        List<Action> result = new ArrayList<>();
        PhysicalCard card = actionContext.card();
        int locationId = card.getLocationId();
        Modifier modifier = _modifier.createModifier(cardGame, actionContext.card(), actionContext);
        Action action = new PlaceCardOnMissionAction(cardGame, actionContext.yourName(),
                actionContext.card(), locationId, modifier);
        result.add(action);
        return result;
    }
}