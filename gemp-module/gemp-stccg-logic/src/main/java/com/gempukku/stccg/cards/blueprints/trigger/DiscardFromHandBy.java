package com.gempukku.stccg.cards.blueprints.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.actions.discard.DiscardCardFromHandResult;
import com.gempukku.stccg.cards.blueprints.FilterableSource;

public class DiscardFromHandBy implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JsonNode value, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "filter", "memorize", "player", "by");

        final String memorize = value.get("memorize").textValue();
        final String byFilter = value.get("by").textValue();

        PlayerSource playerSource = environment.getPlayerSource(value, "player", true);
        final FilterableSource filterableSource = environment.getFilterable(value, "any");
        final FilterableSource byFilterableSource = environment.getFilterFactory().generateFilter(byFilter);

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                boolean result = TriggerConditions.forEachDiscardedFromHandBy(actionContext.getGame(), actionContext.getEffectResult(),
                        byFilterableSource.getFilterable(actionContext), filterableSource.getFilterable(actionContext));
                if (result && playerSource != null) {
                    // Need to check if it was that player discarding the card
                    final String performingPlayer = ((DiscardCardFromHandResult) actionContext.getEffectResult()).getSource().getOwnerName();
                    if (performingPlayer == null || !performingPlayer.equals(playerSource.getPlayerId(actionContext)))
                        result = false;
                }
                if (result && memorize != null) {
                    actionContext.setCardMemory(memorize, ((DiscardCardFromHandResult) actionContext.getEffectResult()).getDiscardedCard());
                }
                return result;
            }
        };
    }
}