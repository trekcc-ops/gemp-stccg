package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackCardFromDiscardEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class StackCardsFromDiscard implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "filter", "where", "count");

        final String filter = environment.getString(effectObject.get("filter"), "filter", "choose(any)");
        final String where = environment.getString(effectObject.get("where"), "where");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCard(where, "_temp1", "you", "Choose card to stack on", environment));
        result.addEffectAppender(
                CardResolver.resolveCardsInDiscard(filter, valueSource, "_temp2", "you", "Choose cards to stack", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final PhysicalCard card = actionContext.getCardFromMemory("_temp1");
                        if (card != null) {
                            final Collection<? extends PhysicalCard> cardsInDiscard = actionContext.getCardsFromMemory("_temp2");

                            List<Effect> result = new LinkedList<>();
                            for (PhysicalCard physicalCard : cardsInDiscard) {
                                result.add(new StackCardFromDiscardEffect(physicalCard, card));
                            }

                            return result;
                        }
                        return null;
                    }
                });

        return result;
    }

}
