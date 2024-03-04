package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.effectappender.DefaultDelayedAppender;
import com.gempukku.stccg.effectappender.EffectAppender;
import com.gempukku.stccg.effectappender.EffectAppenderProducer;
import com.gempukku.stccg.effectappender.MultiEffectAppender;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackActionEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

import java.util.Collection;

public abstract class PlayCardEffectAppenderProducer implements EffectAppenderProducer {

    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "filter", "on", "cost", "memorize", "nocheck");

        final String filter = environment.getString(effectObject.get("filter"), "filter");
        final String onFilter = environment.getString(effectObject.get("on"), "on");
        final ValueSource costModifierSource =
                ValueResolver.resolveEvaluator(effectObject.get("cost"), 0, environment);
        final String memorize = environment.getString(effectObject.get("memorize"), "memorize", "_temp");
        final boolean noCheck = environment.getBoolean(effectObject.get("nocheck"), "nocheck", false);

        ValueSource countSource = new ConstantValueSource(1);
        if(noCheck)
        {
            //This range will cause choice checks to succeed even if no valid choices are found (which is how draw deck
            // searching is supposed to work RAW).  However, we don't want this to be the default, else dual-choice cards
            // that play "from draw deck or discard pile" would allow empty sources to be chosen, which is NPE.
            countSource = ValueResolver.resolveEvaluator("0-1", 1, environment);
        }

                // TODO - Rewrote the line below to match STCCG filter instead of LOTR syntax
        final FilterableSource onFilterableSource = (onFilter != null) ?
                environment.getFilterFactory().generateFilter(onFilter) : null;
/*        final FilterableSource onFilterableSource = (onFilter != null) ?
                environment.getFilterFactory().parseSTCCGFilter(onFilter) : null; */

        MultiEffectAppender result = new MultiEffectAppender();
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
                            final int costModifier = costModifierSource.evaluateExpression(context);
                            Filterable onFilterable = (onFilterableSource != null) ?
                                    onFilterableSource.getFilterable(context) : Filters.any;

                            final CostToEffectAction playCardAction = cardsToPlay.iterator().next().getPlayCardAction(
                                    costModifier, onFilterable, false);
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

    protected abstract EffectAppender resolveCardsAppender(String filter, ValueSource costModifierSource,
                                                           FilterableSource onFilterableSource, ValueSource countSource,
                                                           String memorize, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException;

    protected abstract boolean isAppendedEffectPlayableInFull(ActionContext actionContext);

}
