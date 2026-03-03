package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.discard.DiscardSingleCardAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;

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
    public DiscardSingleCardAction createAction(DefaultGame cardGame, ActionWithSubActions action,
                                                GameTextContext context) {
        if (_requirement == null || _requirement.accepts(context, cardGame)) {
            PhysicalCard performingCard = context.card();
            String playerName = context.yourName();
            return new DiscardSingleCardAction(cardGame, performingCard, playerName, performingCard);
        } else {
            return null;
        }
    }
}