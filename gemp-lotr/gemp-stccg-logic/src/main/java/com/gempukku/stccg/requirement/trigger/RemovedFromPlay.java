package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.results.DiscardCardsFromPlayResult;
import com.gempukku.stccg.results.ForEachKilledResult;
import com.gempukku.stccg.results.ReturnCardsToHandResult;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

public class RemovedFromPlay implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "memorize");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker<>() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(DefaultActionContext<DefaultGame> actionContext) {
                final Filterable filterable = filterableSource.getFilterable(actionContext);
                final boolean killResult = TriggerConditions.forEachKilled(actionContext.getGame(), actionContext.getEffectResult(), filterable);
                if (killResult && memorize != null) {
                    final PhysicalCard killedCard = ((ForEachKilledResult) actionContext.getEffectResult()).getKilledCard();
                    actionContext.setCardMemory(memorize, killedCard);
                }

                final boolean discardResult = TriggerConditions.forEachDiscardedFromPlay(actionContext.getGame(), actionContext.getEffectResult(), filterable);
                if (discardResult && memorize != null) {
                    final PhysicalCard discardedCard = ((DiscardCardsFromPlayResult) actionContext.getEffectResult()).getDiscardedCard();
                    actionContext.setCardMemory(memorize, discardedCard);
                }

                final boolean returnedResult = TriggerConditions.forEachReturnedToHand(actionContext.getGame(), actionContext.getEffectResult(), filterable);
                if (returnedResult && memorize != null) {
                    final PhysicalCard returnedCard = ((ReturnCardsToHandResult) actionContext.getEffectResult()).getReturnedCard();
                    actionContext.setCardMemory(memorize, returnedCard);
                }
                return killResult || discardResult || returnedResult;
            }
        };
    }
}
