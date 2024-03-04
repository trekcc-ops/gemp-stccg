package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import org.json.simple.JSONObject;

public abstract class TribblesRequirementProducer extends RequirementProducer {
    @Override
    public abstract TribblesRequirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException;
}
