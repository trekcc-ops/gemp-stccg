package com.gempukku.lotro.requirement.trigger;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.FilterableSource;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.results.PlayCardResult;
import com.gempukku.lotro.results.PlayEventResult;
import com.gempukku.lotro.game.DefaultGame;
import org.json.simple.JSONObject;

public class PlayedTriggerCheckerProducer implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "on", "memorize", "exertsRanger");

        final String filterString = FieldUtils.getString(value.get("filter"), "filter");
        final String onString = FieldUtils.getString(value.get("on"), "on");
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");
        boolean exertsRanger = FieldUtils.getBoolean(value.get("exertsRanger"), "exertsRanger", false);
        final FilterableSource filter = environment.getFilterFactory().generateFilter(filterString, environment);
        final FilterableSource onFilter = (onString != null) ? environment.getFilterFactory().generateFilter(onString, environment) : null;
        return new TriggerChecker<>() {
            @Override
            public boolean accepts(DefaultActionContext<DefaultGame> actionContext) {
                final Filterable filterable = filter.getFilterable(actionContext);
                boolean played;
                if (onFilter != null) {
                    final Filterable onFilterable = onFilter.getFilterable(actionContext);
                    played = TriggerConditions.playedOn(actionContext.getGame(), actionContext.getEffectResult(), onFilterable, filterable);
                } else {
                    played = TriggerConditions.played(actionContext.getGame(), actionContext.getEffectResult(), filterable);
                }

                if (played) {
                    PlayCardResult playCardResult = (PlayCardResult) actionContext.getEffectResult();
                    if (exertsRanger && playCardResult instanceof PlayEventResult && !((PlayEventResult) playCardResult).isRequiresRanger())
                        return false;

                    if (memorize != null) {
                        LotroPhysicalCard playedCard = playCardResult.getPlayedCard();
                        actionContext.setCardMemory(memorize, playedCard);
                    }
                }
                return played;
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
