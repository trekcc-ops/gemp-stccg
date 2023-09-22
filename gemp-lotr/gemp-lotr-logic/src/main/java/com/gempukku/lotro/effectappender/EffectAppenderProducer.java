package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.game.DefaultGame;
import org.json.simple.JSONObject;

public interface EffectAppenderProducer {
    <AbstractGame extends DefaultGame> EffectAppender<AbstractGame> createEffectAppender(
            JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException;
}
