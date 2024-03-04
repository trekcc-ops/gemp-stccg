package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.CardAttribute;

public class AttributeFieldProcessor implements FieldProcessor {
    private final CardAttribute _attribute;
    public AttributeFieldProcessor(CardAttribute attribute) {
        _attribute = attribute;
    }
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        blueprint.setAttribute(_attribute, environment.getInteger(value, key));
    }
}
