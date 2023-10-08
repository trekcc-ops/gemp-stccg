package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.game.PlayConditions;
import org.json.simple.JSONObject;

public class HasCardInHand implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "player", "count", "filter");

        final String player = FieldUtils.getString(object.get("player"), "player", "you");
        final int count = FieldUtils.getInteger(object.get("count"), "count", 1);
        final String filter = FieldUtils.getString(object.get("filter"), "filter");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        return (actionContext) -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            return PlayConditions.hasCardInHand(actionContext.getGame(), playerSource.getPlayer(actionContext), count, filterable);
        };
    }
}
