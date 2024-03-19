package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import org.json.simple.JSONObject;

public interface EffectProcessor {
    void processEffect(JSONObject value, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException;
}
