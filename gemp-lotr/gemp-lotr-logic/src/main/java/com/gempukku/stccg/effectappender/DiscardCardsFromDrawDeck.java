package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.common.Zone;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.DiscardCardsFromZoneEffect;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DiscardCardsFromDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "filter", "memorize", "player", "deck");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String deck = FieldUtils.getString(effectObject.get("deck"), "deck", "you");

        final ValueSource<DefaultGame> countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInDeck(filter, null, countSource, memorize, player, deck, "Choose cards to discard", environment));
        result.addEffectAppender(
                new DelayedAppender<>() {
                    @Override
                    protected List<Effect> createEffects(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsToDiscard = actionContext.getCardsFromMemory(memorize);
                        List<Effect> result = new LinkedList<>();
                        for (PhysicalCard physicalCard : cardsToDiscard) {
                            result.add(new DiscardCardsFromZoneEffect(action.getActionSource(), Zone.DRAW_DECK, physicalCard));
                        }

                        return result;
                    }
                });

        return result;
    }

}


