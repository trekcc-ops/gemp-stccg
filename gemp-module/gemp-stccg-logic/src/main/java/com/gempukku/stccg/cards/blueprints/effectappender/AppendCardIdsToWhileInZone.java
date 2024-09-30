package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class AppendCardIdsToWhileInZone implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "memory");

        String memory = effectObject.get("memory").textValue();

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new UnrespondableEffect(context) {
                    @Override
                    protected void doPlayEffect() {
                        String value = (String) context.getSource().getWhileInZoneData();
                        if (value == null)
                            value = "";
                        for (PhysicalCard physicalCard : context.getCardsFromMemory(memory)) {
                            value += "," + physicalCard.getCardId();
                        }
                        context.getSource().setWhileInZoneData(value);
                    }
                };
            }
        };
    }
}
