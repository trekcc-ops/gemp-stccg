package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.actions.ActivateCardResult;
import org.json.simple.JSONObject;

public class UsesSpecialAbility implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "filter", "memorize");

        String filter = environment.getString(value.get("filter"), "filter", "any");
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);

        final String memorize = environment.getString(value.get("memorize"), "memorize");
        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                boolean activated = TriggerConditions.activated(actionContext.getGame(), actionContext.getEffectResult(), filterableSource.getFilterable(actionContext));

                if (activated) {
                    ActivateCardResult activateCardResult = (ActivateCardResult) actionContext.getEffectResult();

                    if (memorize != null) {
                        PhysicalCard playedCard = activateCardResult.getSource();
                        actionContext.setCardMemory(memorize, playedCard);
                    }
                }
                return activated;
            }
        };
    }
}
