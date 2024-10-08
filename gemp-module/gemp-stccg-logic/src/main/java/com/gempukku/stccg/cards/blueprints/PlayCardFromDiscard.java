package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackActionEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.ConstantValueSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.effect.DefaultDelayedAppender;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprint;
import com.gempukku.stccg.cards.blueprints.effect.EffectAppenderProducer;
import com.gempukku.stccg.cards.blueprints.effect.MultiEffectBlueprint;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.ModifierFlag;

import java.util.Collection;

public class PlayCardFromDiscard implements EffectAppenderProducer {

    @Override
    public EffectBlueprint createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "filter", "on", "cost", "memorize", "nocheck");

        final String filter = effectObject.get("filter").textValue();
        final String onFilter = effectObject.get("on").textValue();
        final ValueSource costModifierSource =
                ValueResolver.resolveEvaluator(effectObject.get("cost"), 0, environment);
        final String memorize = BlueprintUtils.getString(effectObject, "memorize", "_temp");
        final boolean noCheck = BlueprintUtils.getBoolean(effectObject, "nocheck", false);

        // TODO - LotR "nocheck" property should be in the overall function, not the individual Json definitions
        ValueSource countSource = new ConstantValueSource(1);
        if(noCheck)
        {
            //This range will cause choice checks to succeed even if no valid choices are found (which is how draw deck
            // searching is supposed to work RAW).  However, we don't want this to be the default, else dual-choice cards
            // that play "from draw deck or discard pile" would allow empty sources to be chosen, which is NPE.
            countSource = ValueResolver.resolveEvaluator("0-1");
        }

        final FilterableSource onFilterableSource = (onFilter != null) ?
                environment.getFilterFactory().generateFilter(onFilter) : null;

        MultiEffectBlueprint result = new MultiEffectBlueprint();
        result.setPlayabilityCheckedForEffect(true);

        PlayerSource you = PlayerResolver.resolvePlayer("you");
        FilterableSource cardFilter = environment.getCardFilterableIfChooseOrAll(filter);
        FilterableSource playableCardsFilter = (actionContext) -> {
            final DefaultGame game = actionContext.getGame();
            final int costModifier = costModifierSource.evaluateExpression(actionContext, actionContext.getSource());
            if (onFilterableSource != null) {
                final Filterable onFilterable = onFilterableSource.getFilterable(actionContext);
                return Filters.and(Filters.playable(costModifier), Filters.attachableTo(game, onFilterable));
            }

            return Filters.playable(costModifier);
        };

        EffectBlueprint targetCardAppender = CardResolver.resolveCardsInZone(filter, playableCardsFilter, countSource,
                memorize, you, you, "Choose card to play", cardFilter, Zone.DISCARD, false,
                environment.getCardSourceFromZone(you, Zone.DISCARD, filter));

        result.addEffectAppender(targetCardAppender);
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                        final Collection<? extends PhysicalCard> cardsToPlay = context.getCardsFromMemory(memorize);
                        if (cardsToPlay.size() == 1) {
                            final DefaultGame game = context.getGame();
                            Filterable onFilterable = (onFilterableSource != null) ?
                                    onFilterableSource.getFilterable(context) : Filters.any;

                            final CostToEffectAction playCardAction = cardsToPlay.iterator().next().getPlayCardAction(
                                    onFilterable, false);
                            return new StackActionEffect(game, playCardAction);
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        return isAppendedEffectPlayableInFull(actionContext);
                    }

                    @Override
                    public boolean isPlayabilityCheckedForEffect() {
                        return true;
                    }
                });

        return result;
    }

    protected boolean isAppendedEffectPlayableInFull(ActionContext actionContext) {
        return actionContext.getGame().getModifiersQuerying().hasFlagActive(ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK);
    }

}