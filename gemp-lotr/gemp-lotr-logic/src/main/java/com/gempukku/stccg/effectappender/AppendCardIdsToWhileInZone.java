package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.UnrespondableEffect;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

public class AppendCardIdsToWhileInZone implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "memory");

        String memory = FieldUtils.getString(effectObject.get("memory"), "memory");

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext<DefaultGame> actionContext) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(DefaultGame game) {
                        String value = (String) actionContext.getSource().getWhileInZoneData();
                        if (value == null)
                            value = "";
                        for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(memory)) {
                            value += "," + physicalCard.getCardId();
                        }
                        actionContext.getSource().setWhileInZoneData(value);
                    }
                };
            }
        };
    }
}
