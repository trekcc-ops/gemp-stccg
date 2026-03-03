package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.discard.DiscardAction;
import com.gempukku.stccg.actions.discard.DiscardCardToPointAreaAction;
import com.gempukku.stccg.actions.discard.DiscardSingleCardAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;

public class DiscardThisCardSubActionBlueprint implements SubActionBlueprint {

    private final Requirement _requirement;
    private final boolean _toPointArea;

    public DiscardThisCardSubActionBlueprint() {
        _requirement = null;
        _toPointArea = false;
    }

    public DiscardThisCardSubActionBlueprint(boolean toPointArea) {
        _requirement = null;
        _toPointArea = toPointArea;
    }

    @JsonCreator
    private DiscardThisCardSubActionBlueprint(@JsonProperty("requires") Requirement requirement) {
        _requirement = requirement;
        _toPointArea = false;
    }

    @Override
    public DiscardAction createAction(DefaultGame cardGame, GameTextContext context) {
        if (_requirement == null || _requirement.accepts(context, cardGame)) {
            if (_toPointArea) {
                return new DiscardCardToPointAreaAction(cardGame, context.card(), context.yourName(), context.card());
            } else {
                return new DiscardSingleCardAction(cardGame, context.card(), context.yourName(), context.card());
            }
        } else {
            return null;
        }
    }
}