package com.gempukku.stccg.cards.blueprints.actionsource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.Player;

public class SeedCardActionSource extends DefaultActionSource {
    private final Zone _seedToZone;

    public SeedCardActionSource(@JsonProperty(value = "where", required = true)
                                Zone seedToZone
    ) {
        super("Seed card", 0, null);
        _seedToZone = seedToZone;
    }

    public SeedCardAction createAction(PhysicalCard card) {
        if (_seedToZone == null)
            return new SeedCardAction(card);
        else return new SeedCardAction(card, _seedToZone);
    }

    @Override
    protected SeedCardAction createActionAndAppendToContext(PhysicalCard card, ActionContext actionContext) {
        if (isValid(actionContext)) {
            SeedCardAction action = createAction(card);
            appendActionToContext(action, actionContext);
            return action;
        }
        return null;
    }

    @JsonProperty("limit")
    private void setLimit(int limit) {
        Requirement requirement = actionContext -> {
            PhysicalCard cardSeeding = actionContext.getSource();
            Player seedingPlayer = actionContext.getPerformingPlayer();
            int copiesSeeded = cardSeeding.getNumberOfCopiesSeededByPlayer(seedingPlayer, actionContext.getGame());
            return copiesSeeded < limit;
        };
        addRequirement(requirement);
    }

}