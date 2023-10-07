package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.FilterableSource;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ModifierSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class CancelStrengthBonusTo implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "from");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final String from = FieldUtils.getString(object.get("from"), "from");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource fromFilterableSource = environment.getFilterFactory().generateFilter(from, environment);

        return actionContext -> new CancelStrengthBonusTargetModifier(actionContext.getSource(),
                filterableSource.getFilterable(actionContext),
                fromFilterableSource.getFilterable(actionContext));
    }
}
