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

    public SeedCardAction createAction(PhysicalCard card) {
        if (_seedToZone == null)
            return new SeedCardAction(card);
        else return new SeedCardAction(card, _seedToZone);
    }

    @Override
    protected SeedCardAction createActionAndAppendToContext(DefaultGame cardGame, PhysicalCard card,
                                                            ActionContext actionContext) {
        if (isValid(cardGame, actionContext)) {
            SeedCardAction action = createAction(card);
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