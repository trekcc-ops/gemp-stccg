package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.FilterableSource;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.CardResolver;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.DoNothingEffect;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.ReplaceInSkirmishEffect;
import org.json.simple.JSONObject;

public class ReplaceInSkirmish implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "with");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String with = FieldUtils.getString(effectObject.get("with"), "with");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        MultiEffectAppender result = new MultiEffectAppender();
        result.addEffectAppender(
                CardResolver.resolveCard(with, "_temp", "you", "Choose character to replace with in skirmish", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                        final LotroPhysicalCard card = actionContext.getCardFromMemory("_temp");
                        if (card != null)
                            return new ReplaceInSkirmishEffect(card, filterableSource.getFilterable(actionContext));
                        else
                            return new DoNothingEffect();
                    }
                });

        return result;
    }

}
