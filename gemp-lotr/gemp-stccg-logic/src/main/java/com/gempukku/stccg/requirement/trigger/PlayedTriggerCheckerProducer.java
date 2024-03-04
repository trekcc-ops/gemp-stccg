package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import org.json.simple.JSONObject;

public class PlayedTriggerCheckerProducer implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "filter", "player", "on", "memorize");

        final PlayerSource playingPlayer = environment.getPlayerSource(value, "player","you");
        final FilterableSource filter = environment.getFilterFactory().parseSTCCGFilter(
                environment.getString(value.get("filter"), "filter"));
        final FilterableSource onFilter = environment.getFilterFactory().parseSTCCGFilter(
                environment.getString(value.get("on"), "on"));

        final String memorize = environment.getString(value.get("memorize"), "memorize");


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
