package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import org.json.simple.JSONObject;

public abstract class RequirementProducer {
    public abstract Requirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException;
}
