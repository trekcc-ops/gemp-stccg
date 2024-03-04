package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.CardType;

public class CardTypeFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        blueprint.setCardType(environment.getEnum(CardType.class, value, key));
    }
}
