package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.actions.discard.DiscardCardFromHandResult;
import org.json.simple.JSONObject;

public class DiscardFromHandBy implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "filter", "memorize", "player", "by");

        final String filter = environment.getString(value.get("filter"), "filter", "any");
        final String memorize = environment.getString(value.get("memorize"), "memorize");
        final String player = environment.getString(value.get("player"), "player", "you");
        final String byFilter = environment.getString(value.get("by"), "by");

        PlayerSource playerSource = (player != null) ? PlayerResolver.resolvePlayer(player) : null;
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);
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
