package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import org.json.simple.JSONObject;

public abstract class TribblesRequirementProducer extends RequirementProducer {
    @Override
    public abstract TribblesRequirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment)
            throws InvalidCardDefinitionException;
}
