package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackActionEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.ConstantValueSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.effectappender.DelayedAppender;
import com.gempukku.stccg.cards.blueprints.effectappender.EffectAppender;
import com.gempukku.stccg.cards.blueprints.effectappender.EffectAppenderProducer;
import com.gempukku.stccg.cards.blueprints.effectappender.MultiEffectAppender;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class PlayCardFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "filter", "on", "cost", "memorize", "nocheck");

        final String filter = environment.getString(effectObject, "filter");
        final String onFilter = effectObject.get("on").textValue();
        final ValueSource costModifierSource =
                ValueResolver.resolveEvaluator(effectObject.get("cost"), 0, environment);
        final String memorize = environment.getString(effectObject, "memorize", "_temp");
        final boolean noCheck = environment.getBoolean(effectObject, "nocheck", false);

        ValueSource countSource = new ConstantValueSource(1);
        if(noCheck)
        {
            //This range will cause choice checks to succeed even if no valid choices are found (which is how draw deck
            // searching is supposed to work RAW). But we don't want this to be the default, or else dual-choice cards
            // that play "from draw deck or discard pile" would allow empty sources to be chosen, which is NPE.
            countSource = ValueResolver.resolveEvaluator("0-1");
        }

        final FilterableSource onFilterableSource = (onFilter != null) ? environment.getFilterFactory().generateFilter(onFilter) : null;

        FilterableSource choiceFilter = (actionContext) -> {
            final DefaultGame game = actionContext.getGame();
            final int costModifier = costModifierSource.getEvaluator(actionContext).evaluateExpression(game, actionContext.getSource());
            if (onFilterableSource != null) {
                final Filterable onFilterable = onFilterableSource.getFilterable(actionContext);
                return Filters.and(Filters.playable(costModifier),
                        Filters.attachableTo(game, onFilterable));
            }
            return Filters.playable(costModifier);
        };
        FilterableSource typeFilter = environment.getCardFilterableIfChooseOrAll(filter);
        PlayerSource you = ActionContext::getPerformingPlayerId;
        EffectAppender targetCardAppender = CardResolver.resolveCardsInZone(filter, choiceFilter, countSource,
                memorize, you, you, "Choose card to play", typeFilter, Zone.HAND, false,
                environment.getCardSourceFromZone(you, Zone.HAND, filter));

        MultiEffectAppender result = new MultiEffectAppender();
        result.setPlayabilityCheckedForEffect(true);



        result.addEffectAppender(targetCardAppender);
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsToPlay = actionContext.getCardsFromMemory(memorize);
                        if (cardsToPlay.size() == 1) {

                            Filterable onFilterable = (onFilterableSource != null) ? onFilterableSource.getFilterable(actionContext) : Filters.any;

                            final CostToEffectAction playCardAction = cardsToPlay.iterator().next().getPlayCardAction(
                                    onFilterable, false);
                            return new StackActionEffect(actionContext.getGame(), playCardAction);
                        } else {
                            return null;
                        }
                    }
                });

        return result;
    }
}
