package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import org.json.simple.JSONObject;

public class CancelStrengthBonusTo implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter", "from");

        final String filter = environment.getString(object.get("filter"), "filter");
        final String from = environment.getString(object.get("from"), "from");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);
        final FilterableSource fromFilterableSource = environment.getFilterFactory().generateFilter(from);

        return actionContext -> new CancelStrengthBonusTargetModifier(actionContext.getSource(),
                filterableSource.getFilterable(actionContext),
                fromFilterableSource.getFilterable(actionContext));
    }
}
