package com.gempukku.stccg.requirement.producers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementProducer;
import org.json.simple.JSONObject;

public class HasCardInPlayPile extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment)
            throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "player", "count", "filter");

        final String player = FieldUtils.getString(object.get("player"), "player", "you");
        final int count = FieldUtils.getInteger(object.get("count"), "count", 1);
        final String filter = FieldUtils.getString(object.get("filter"), "filter");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        final FilterableSource filterableSource =
                environment.getFilterFactory().generateFilter(filter, environment);
        return (actionContext) -> {
            final String playerId = playerSource.getPlayerId(actionContext);
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            return actionContext.getGameState().getPlayer(playerId).hasCardInZone(Zone.PLAY_PILE, count, filterable);
        };
    }
}
