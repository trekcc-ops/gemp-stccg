package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackActionEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.effectappender.DelayedAppender;
import com.gempukku.stccg.effectappender.EffectAppender;
import com.gempukku.stccg.effectappender.EffectAppenderProducer;
import com.gempukku.stccg.effectappender.MultiEffectAppender;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

import java.util.Collection;

public class PlayCardFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "filter", "on", "cost", "ignoreInDeadPile", "memorize", "nocheck");

        final String filter = environment.getString(effectObject.get("filter"), "filter");
        final String onFilter = environment.getString(effectObject.get("on"), "on");
        final ValueSource costModifierSource = ValueResolver.resolveEvaluator(effectObject.get("cost"), 0, environment);
        final boolean ignoreInDeadPile = environment.getBoolean(effectObject.get("ignoreInDeadPile"), "ignoreInDeadPile", false);
        final String memorize = environment.getString(effectObject.get("memorize"), "memorize", "_temp");
        final boolean noCheck = environment.getBoolean(effectObject.get("nocheck"), "nocheck", false);

        ValueSource countSource = new ConstantValueSource(1);
        if(noCheck)
        {
            //This range will cause choice checks to succeed even if no valid choices are found (which is how draw deck
            // searching is supposed to work RAW). But we don't want this to be the default, or else dual-choice cards
            // that play "from draw deck or discard pile" would allow empty sources to be chosen, which is NPE.
            countSource = ValueResolver.resolveEvaluator("0-1", 1, environment);
        }

        final FilterableSource onFilterableSource = (onFilter != null) ? environment.getFilterFactory().generateFilter(onFilter) : null;

        MultiEffectAppender result = new MultiEffectAppender();
        result.setPlayabilityCheckedForEffect(true);

        result.addEffectAppender(
                CardResolver.resolveCardsInHand(filter,
                        (actionContext) -> {
                            final DefaultGame game = actionContext.getGame();
                            final int costModifier = costModifierSource.getEvaluator(actionContext).evaluateExpression(game, actionContext.getSource());
                            if (onFilterableSource != null) {
                                final Filterable onFilterable = onFilterableSource.getFilterable(actionContext);
                                return Filters.and(Filters.playable(game, costModifier),
                                        Filters.attachableTo(game, onFilterable));
                            }
                            return Filters.playable(game, costModifier);
                        },
                        countSource, memorize, "you", "you", "Choose card to play from hand", false, environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsToPlay = actionContext.getCardsFromMemory(memorize);
                        if (cardsToPlay.size() == 1) {
                            final DefaultGame game = actionContext.getGame();
                            final int costModifier = costModifierSource.getEvaluator(actionContext).evaluateExpression(game, actionContext.getSource());

                            Filterable onFilterable = (onFilterableSource != null) ? onFilterableSource.getFilterable(actionContext) : Filters.any;

                            final CostToEffectAction playCardAction = cardsToPlay.iterator().next().getPlayCardAction(
                                    onFilterable, false);
                            return new StackActionEffect(game, playCardAction);
                        } else {
                            return null;
                        }
                    }
                });

        return result;
    }
}
