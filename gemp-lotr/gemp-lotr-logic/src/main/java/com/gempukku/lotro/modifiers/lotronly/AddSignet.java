package com.gempukku.lotro.modifiers.lotronly;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.FilterableSource;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ModifierSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.modifiers.ModifierSourceProducer;
import com.gempukku.lotro.common.Signet;
import org.json.simple.JSONObject;

public class AddSignet implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "signet");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final Signet signet = FieldUtils.getEnum(Signet.class, object.get("signet"), "signet");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return actionContext ->
                new AddSignetModifier(actionContext.getSource(),
                        filterableSource.getFilterable(actionContext),
                        signet);
    }
}
