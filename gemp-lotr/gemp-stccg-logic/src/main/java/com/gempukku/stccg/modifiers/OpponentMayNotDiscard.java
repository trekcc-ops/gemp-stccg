package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import org.json.simple.JSONObject;

public class OpponentMayNotDiscard implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter");

        final String filter = environment.getString(object.get("filter"), "filter");
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);

        return (actionContext) -> new CantDiscardFromPlayByPlayerModifier(actionContext.getSource(), "Can't be discarded by opponent",
                filterableSource.getFilterable(actionContext),
                actionContext.getPerformingPlayerId());
    }
}
