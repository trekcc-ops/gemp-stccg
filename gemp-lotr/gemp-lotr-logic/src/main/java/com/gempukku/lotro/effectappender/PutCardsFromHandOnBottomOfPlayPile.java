package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ValueSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.CardResolver;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.PutCardsFromHandOnBottomOfPlayPileEffect;
import com.gempukku.lotro.game.DefaultGame;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PutCardsFromHandOnBottomOfPlayPile implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "optional", "filter", "count", "reveal");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final boolean optional = FieldUtils.getBoolean(effectObject.get("optional"), "optional", false);
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
        final ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final boolean reveal = FieldUtils.getBoolean(effectObject.get("reveal"), "reveal", true);

        ValueSource valueSource;
        if (optional)
            valueSource = ValueResolver.resolveEvaluator("0-" + count, environment);
        else
            valueSource = count;

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInHand(filter, valueSource, "_temp", player, player,
                        "Choose cards from hand to put beneath play pile", environment));
        result.addEffectAppender(
                new DelayedAppender<>() {
                    @Override
                    protected List<Effect> createEffects(boolean cost, CostToEffectAction action, DefaultActionContext<DefaultGame> actionContext) {
                        final Collection<? extends LotroPhysicalCard> cards = actionContext.getCardsFromMemory("_temp");
                        List<Effect> result = new LinkedList<>();
                        for (LotroPhysicalCard card : cards) {
                            result.add(new PutCardsFromHandOnBottomOfPlayPileEffect(reveal, card));
                        }
                        return result;
                    }
                });

        return result;
    }

}
