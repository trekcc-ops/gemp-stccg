package com.gempukku.stccg.cards.blueprints.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.common.filterable.Filterable;

public class PlayedTriggerCheckerProducer implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JsonNode value)
            throws InvalidCardDefinitionException {
        FilterFactory factory = new FilterFactory();
        BlueprintUtils.validateAllowedFields(value, "filter", "on", "memorize");
        BlueprintUtils.validateRequiredFields(value, "filter");

        final PlayerSource playingPlayer = BlueprintUtils.getPlayerSource(value, "player",true);
        final FilterableSource filter = factory.parseSTCCGFilter(value.get("filter").textValue());
        final FilterableSource onFilter = (value.has("on")) ?
                factory.parseSTCCGFilter(value.get("on").textValue()) : null;
        final String memorize = BlueprintUtils.getString(value, "memorize", "_temp");

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable filterable = filter.getFilterable(actionContext);
                final String playingPlayerId = playingPlayer.getPlayerId(actionContext);
                final ActionResult actionResult = actionContext.getEffectResult();
                final boolean played;

                if (onFilter != null) {
                    final Filterable onFilterable = onFilter.getFilterable(actionContext);
                    played = TriggerConditions.playedOn(actionContext.getGame(), actionResult, onFilterable, filterable);
                } else {
                    played = TriggerConditions.played(actionContext.getGame(),
                            actionContext.getGame().getPlayer(playingPlayerId), actionResult, filterable);
                }

                if (played && memorize != null)
                    actionContext.setCardMemory(memorize, ((PlayCardResult) actionResult).getPlayedCard());
                return played;
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}