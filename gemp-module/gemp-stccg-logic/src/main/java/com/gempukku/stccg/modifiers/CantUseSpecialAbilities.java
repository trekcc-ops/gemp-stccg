package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import org.json.simple.JSONObject;

public class CantUseSpecialAbilities implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter");

        final String filter = environment.getString(object.get("filter"), "filter", "any");
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);

        return actionContext -> new PlayersCantUseCardSpecialAbilitiesModifier(actionContext.getSource(), filterableSource.getFilterable(actionContext));
    }
}
