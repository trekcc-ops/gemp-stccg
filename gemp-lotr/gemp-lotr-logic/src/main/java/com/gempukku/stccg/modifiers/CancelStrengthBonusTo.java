package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ModifierSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
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
