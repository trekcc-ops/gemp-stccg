package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.UnrespondableEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

import java.util.Collection;

public class MemorizeActive implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "memory");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String memory = FieldUtils.getString(effectObject.get("memory"), "memory");

        final FilterableSource filterSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(DefaultGame game) {
                        final Filterable filterable = filterSource.getFilterable(actionContext);
                        final Collection<PhysicalCard> physicalCards = Filters.filterActive(game, filterable);
                        actionContext.setCardMemory(memory, physicalCards);
                    }
                };
            }
        };
    }

}
