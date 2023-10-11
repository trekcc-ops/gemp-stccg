package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import org.json.simple.JSONObject;

public interface EffectAppenderProducer {
    <AbstractContext extends ActionContext> EffectAppender<AbstractContext> createEffectAppender(
            JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException;
}
