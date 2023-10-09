package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.game.TribblesGame;
import org.json.simple.JSONObject;

public class PlayerGoesOut implements TriggerCheckerProducer<TribblesGame> {
    @Override
    public TriggerChecker<TribblesGame> getTriggerChecker(JSONObject value, CardGenerationEnvironment environment)
            throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value);
        PlayerSource playerSource = PlayerResolver.resolvePlayer("you");

        return new TriggerChecker<>() {
            @Override
            public boolean accepts(DefaultActionContext<TribblesGame> actionContext) {
                return TriggerConditions.playerGoesOut(actionContext.getEffectResult(),
                        playerSource.getPlayer(actionContext));
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
