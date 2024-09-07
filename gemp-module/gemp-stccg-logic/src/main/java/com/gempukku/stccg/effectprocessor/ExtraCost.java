package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.effectappender.EffectAppender;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.ExtraPlayCost;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.condition.Condition;
import org.json.simple.JSONObject;

public class ExtraCost implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "cost");

        final EffectAppender costAppender = environment.getEffectAppenderFactory().getEffectAppender((JSONObject) value.get("cost"));

        blueprint.appendExtraPlayCost(
                (actionContext) -> new ExtraPlayCost() {
                    @Override
                    public void appendExtraCosts(DefaultGame game, CostToEffectAction action, PhysicalCard card) {
                        costAppender.appendEffect(true, action, actionContext);
                    }

                    @Override
                    public boolean canPayExtraCostsToPlay(DefaultGame game, PhysicalCard card) {
                        return costAppender.isPlayableInFull(actionContext);
                    }

                    @Override
                    public Condition getCondition() {
                        return null;
                    }
                });
    }
}
