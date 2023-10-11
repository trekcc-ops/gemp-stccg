package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.PutCardFromPlayOnBottomOfDeckEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PutCardsFromPlayOnBottomOfDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "filter", "count");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(filter, valueSource, "_temp", player, "Choose cards in play", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected List<Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cards = actionContext.getCardsFromMemory("_temp");
                        List<Effect> result = new LinkedList<>();
                        for (PhysicalCard card : cards) {
                            result.add(new PutCardFromPlayOnBottomOfDeckEffect(actionContext.getGame(), card));
                        }
                        return result;
                    }
                });

        return result;
    }

}
