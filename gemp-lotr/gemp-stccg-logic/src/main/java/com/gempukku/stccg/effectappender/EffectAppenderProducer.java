package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

public interface EffectAppenderProducer {
    <AbstractGame extends DefaultGame> EffectAppender<AbstractGame> createEffectAppender(
            JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException;
}
