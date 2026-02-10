package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.actions.playcard.SeedOutpostAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.YouPlayerSource;
import com.gempukku.stccg.requirement.SeedQuantityLimitRequirement;

public class SeedCardActionBlueprint extends DefaultActionBlueprint {
    private final Zone _seedToZone;

    public SeedCardActionBlueprint(@JsonProperty(value = "where")
                                   Zone seedToZone,
                                   @JsonProperty(value = "limit")
                                   Integer limit
    ) {
        super(new YouPlayerSource());
        _seedToZone = seedToZone;
        if (limit != null) {
            costs.add(new UsePerGameLimitActionBlueprint(this, limit));
        }
    }

    @Override
    public SeedCardAction createAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard thisCard) {
        ActionContext actionContext = new ActionContext(thisCard, performingPlayerName);
        if (isValid(cardGame, actionContext) &&
                cardGame.getRules().cardCanEnterPlay(cardGame, thisCard, PlayCardAction.EnterPlayActionType.SEED)) {
            SeedCardAction action;
            if (thisCard instanceof FacilityCard facility) {
                action = new SeedOutpostAction(cardGame, facility);
            } else {
                action = (_seedToZone == null) ? new SeedCardAction(cardGame, thisCard, actionContext) :
                        new SeedCardAction(cardGame, thisCard, _seedToZone, actionContext);
            }
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