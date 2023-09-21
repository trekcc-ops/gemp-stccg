package com.gempukku.lotro.requirement.trigger;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.PlayerSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.PlayerResolver;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.TriggerConditions;
import org.json.simple.JSONObject;

public class Reconciles implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "player");

        final String player = FieldUtils.getString(value.get("player"), "player");

        PlayerSource playerSource = (player != null) ? PlayerResolver.resolvePlayer(player) : null;

        return new TriggerChecker<>() {
            @Override
            public boolean accepts(DefaultActionContext<DefaultGame> actionContext) {
                if (playerSource != null)
                    return TriggerConditions.reconciles(actionContext.getEffectResult(),
                            playerSource.getPlayer(actionContext));
                else
                    return TriggerConditions.reconciles(actionContext.getEffectResult(), null);
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
