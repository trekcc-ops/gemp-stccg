package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.results.DiscardCardsFromPlayResult;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

public class Discarded implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "memorize", "player");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");
        final String player = FieldUtils.getString(value.get("player"), "player");

        PlayerSource playerSource = (player != null) ? PlayerResolver.resolvePlayer(player) : null;

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker<>() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(DefaultActionContext<DefaultGame> actionContext) {
                final Filterable filterable = filterableSource.getFilterable(actionContext);
                boolean result = TriggerConditions.forEachDiscardedFromPlay(actionContext.getGame(), actionContext.getEffectResult(), filterable);
                if (result && playerSource != null) {
                    // Need to check if it was that player discarding the card
                    final String performingPlayer = ((DiscardCardsFromPlayResult) actionContext.getEffectResult()).getPerformingPlayer();
                    if (performingPlayer == null || !performingPlayer.equals(playerSource.getPlayer(actionContext)))
                        result = false;
                }
                if (result && memorize != null) {
                    final PhysicalCard discardedCard = ((DiscardCardsFromPlayResult) actionContext.getEffectResult()).getDiscardedCard();
                    actionContext.setCardMemory(memorize, discardedCard);
                }
                return result;
            }
        };
    }
}
