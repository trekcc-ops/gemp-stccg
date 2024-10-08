package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.cards.blueprints.effect.DefaultDelayedAppender;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprint;
import com.gempukku.stccg.cards.blueprints.effect.EffectAppenderProducer;
import com.gempukku.stccg.cards.blueprints.effect.MultiEffectBlueprint;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackActionEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public abstract class PlayCardEffectAppenderProducer implements EffectAppenderProducer {

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

        result.addEffectAppender(resolveCardsAppender(
                filter, costModifierSource, onFilterableSource, countSource, memorize, environment));
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

    protected abstract EffectBlueprint resolveCardsAppender(String filter, ValueSource costModifierSource,
                                                            FilterableSource onFilterableSource, ValueSource countSource,
                                                            String memorize, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException;

    protected abstract boolean isAppendedEffectPlayableInFull(ActionContext actionContext);

}