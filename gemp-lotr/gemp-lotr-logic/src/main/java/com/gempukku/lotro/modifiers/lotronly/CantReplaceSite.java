package com.gempukku.lotro.modifiers.lotronly;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.FilterableSource;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ModifierSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.modifiers.ModifierSourceProducer;
import org.json.simple.JSONObject;

public class CantReplaceSite implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter");

        final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return actionContext -> new CantReplaceSiteModifier(actionContext.getSource(), null,
                filterableSource.getFilterable(actionContext));
    }
}
