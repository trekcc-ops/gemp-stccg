package com.gempukku.lotro.requirement.trigger;

import com.gempukku.lotro.cards.DefaultActionContext;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.PlayerSource;
import com.gempukku.lotro.effectappender.resolver.PlayerResolver;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.game.TribblesGame;
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
