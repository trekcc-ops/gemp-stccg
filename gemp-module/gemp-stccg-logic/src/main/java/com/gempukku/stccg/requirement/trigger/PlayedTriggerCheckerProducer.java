package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.playcard.PlayCardResult;

public class PlayedTriggerCheckerProducer implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JsonNode value, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "filter", "player", "on", "memorize");

        final PlayerSource playingPlayer = environment.getPlayerSource(value, "player",true);
        final FilterableSource filter =
                environment.getFilterFactory().parseSTCCGFilter(value.get("filter").textValue());
        final FilterableSource onFilter;

        if (value.has("on")) {
            onFilter = environment.getFilterFactory().parseSTCCGFilter(value.get("on").textValue());
        } else {
            onFilter = null;
        }
        final String memorize = environment.getString(value, "memorize");

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable filterable = filter.getFilterable(actionContext);
                final String playingPlayerId = playingPlayer.getPlayerId(actionContext);
                final EffectResult effectResult = actionContext.getEffectResult();
                final boolean played;

                if (onFilter != null) {
                    final Filterable onFilterable = onFilter.getFilterable(actionContext);
                    played = TriggerConditions.playedOn(effectResult, onFilterable, filterable);
                } else {
                    played = TriggerConditions.played(playingPlayerId, effectResult, filterable);
                }

                if (played && memorize != null)
                    actionContext.setCardMemory(memorize, ((PlayCardResult) effectResult).getPlayedCard());
                return played;
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
