package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.EffectAppender;
import com.gempukku.lotro.effectappender.EffectAppenderProducer;
import com.gempukku.lotro.effectappender.DelayedAppender;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.UnrespondableEffect;
import com.gempukku.lotro.game.DefaultGame;
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
                        for (LotroPhysicalCard physicalCard : actionContext.getCardsFromMemory(memory)) {
                            value += "," + physicalCard.getCardId();
                        }
                        actionContext.getSource().setWhileInZoneData(value);
                    }
                };
            }
        };
    }
}
