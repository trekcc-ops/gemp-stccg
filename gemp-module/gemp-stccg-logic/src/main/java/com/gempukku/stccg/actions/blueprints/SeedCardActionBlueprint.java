package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.SeedQuantityLimitRequirement;

public class SeedCardActionBlueprint extends DefaultActionBlueprint {
    private final Zone _seedToZone;

    public SeedCardActionBlueprint(@JsonProperty(value = "where", required = true)
                                Zone seedToZone
    ) {
        super(0);
        _seedToZone = seedToZone;
    }

    @Override
    public SeedCardAction createAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard thisCard) {
        ActionContext actionContext = new ActionContext(thisCard, performingPlayerName);
        if (isValid(cardGame, actionContext)) {
            SeedCardAction action = (_seedToZone == null) ? new SeedCardAction(thisCard) :
                    new SeedCardAction(thisCard, _seedToZone);
            appendActionToContext(cardGame, action, actionContext);
            return action;
        }
        return null;
    }


    @JsonProperty("limit")
    private void setLimit(int limit) {
        addRequirement(new SeedQuantityLimitRequirement(limit));
    }

}