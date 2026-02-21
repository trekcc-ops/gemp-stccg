package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.discard.DiscardSingleCardAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;

import java.util.ArrayList;
import java.util.List;

public class DiscardThisCardSubActionBlueprint implements SubActionBlueprint {

    private final Requirement _requirement;

    public DiscardThisCardSubActionBlueprint() {
        _requirement = null;
    }

    @JsonCreator
    private DiscardThisCardSubActionBlueprint(@JsonProperty("requires") Requirement requirement) {
        _requirement = requirement;
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, GameTextContext actionContext) {
        List<Action> result = new ArrayList<>();
        if (_requirement == null || _requirement.accepts(actionContext, cardGame)) {
            PhysicalCard performingCard = actionContext.card();
            String playerName = actionContext.yourName();
            result.add(new DiscardSingleCardAction(cardGame, performingCard, playerName, performingCard));
        }
        return result;
    }
}