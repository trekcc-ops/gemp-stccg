package com.gempukku.stccg.actions.sources;

import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;

public class SeedCardActionSource extends DefaultActionSource {

    private Zone _seedToZone;

    @Override
    public SeedCardAction createAction(PhysicalCard card) {
        if (_seedToZone == null)
            return new SeedCardAction(card);
        else return new SeedCardAction(card, _seedToZone);
    }

    @Override
    public SeedCardAction createActionAndAppendToContext(PhysicalCard card, ActionContext actionContext) {
        if (isValid(actionContext)) {
            SeedCardAction action = createAction(card);
            appendActionToContext(action, actionContext);
            return action;
        }
        return null;
    }

    public void setSeedZone(Zone zone) { _seedToZone = zone; }
}