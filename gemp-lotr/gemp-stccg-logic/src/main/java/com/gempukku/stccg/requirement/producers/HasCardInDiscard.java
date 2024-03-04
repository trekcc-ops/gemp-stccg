package com.gempukku.stccg.requirement.producers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementProducer;
import org.json.simple.JSONObject;

public class HasCardInDiscard extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "player", "count", "filter");

        final String player = environment.getString(object.get("player"), "player", "you");
        final int count = environment.getInteger(object.get("count"), "count", 1);
        final String filter = environment.getString(object.get("filter"), "filter");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);
        return (actionContext) -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            return actionContext.getGameState().getPlayer(playerSource.getPlayerId(actionContext))
                    .hasCardInZone(Zone.DISCARD, count, filterable);
        };
    }
}
