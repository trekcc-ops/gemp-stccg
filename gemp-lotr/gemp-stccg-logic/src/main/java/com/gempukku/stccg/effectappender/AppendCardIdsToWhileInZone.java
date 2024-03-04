package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import org.json.simple.JSONObject;

public class AppendCardIdsToWhileInZone implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "memory");

        String memory = environment.getString(effectObject.get("memory"), "memory");

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new UnrespondableEffect() {
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
