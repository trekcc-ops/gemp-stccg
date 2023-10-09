package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.results.DiscardCardFromHandResult;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

public class DiscardFromHandBy implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "memorize", "player", "by");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");
        final String player = FieldUtils.getString(value.get("player"), "player", "you");
        final String byFilter = FieldUtils.getString(value.get("by"), "by");

        PlayerSource playerSource = (player != null) ? PlayerResolver.resolvePlayer(player) : null;
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource byFilterableSource = environment.getFilterFactory().generateFilter(byFilter, environment);

        return new TriggerChecker<>() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(DefaultActionContext<DefaultGame> actionContext) {
                boolean result = TriggerConditions.forEachDiscardedFromHandBy(actionContext.getGame(), actionContext.getEffectResult(),
                        byFilterableSource.getFilterable(actionContext), filterableSource.getFilterable(actionContext));
                if (result && playerSource != null) {
                    // Need to check if it was that player discarding the card
                    final String performingPlayer = ((DiscardCardFromHandResult) actionContext.getEffectResult()).getSource().getOwner();
                    if (performingPlayer == null || !performingPlayer.equals(playerSource.getPlayer(actionContext)))
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
