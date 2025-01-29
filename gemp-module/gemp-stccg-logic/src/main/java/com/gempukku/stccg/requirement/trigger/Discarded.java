package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.discard.DiscardCardFromPlayResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;

public class Discarded implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JsonNode value)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(value, "filter", "memorize", "player");

        final String memorize = value.get("memorize").textValue();
        final String player = value.get("player").textValue();

        PlayerSource playerSource = (player != null) ? PlayerResolver.resolvePlayer(player) : null;

        final FilterableSource filterableSource = BlueprintUtils.getFilterable(value, "any");

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable filterable = filterableSource.getFilterable(actionContext);
                boolean result = TriggerConditions.forEachDiscardedFromPlay(actionContext.getGame(), actionContext.getEffectResult(), filterable);
                if (result && playerSource != null) {
                    // Need to check if it was that player discarding the card
                    final String performingPlayer = ((DiscardCardFromPlayResult) actionContext.getEffectResult()).getPerformingPlayer();
                    if (performingPlayer == null || !performingPlayer.equals(playerSource.getPlayerId(actionContext)))
                        result = false;
                }
                if (result && memorize != null) {
                    final PhysicalCard discardedCard = ((DiscardCardFromPlayResult) actionContext.getEffectResult()).getDiscardedCard();
                    actionContext.setCardMemory(memorize, discardedCard);
                }
                return result;
            }
        };
    }
}