package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.BuiltLotroCardBlueprint;
import com.gempukku.stccg.cards.AidCostSource;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.EffectAppender;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

public class AidCost implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "cost");

        final JSONObject[] costArray = FieldUtils.getObjectArray(value.get("cost"), "cost");

        final EffectAppender<DefaultGame>[] costAppenders = environment.getEffectAppenderFactory().getEffectAppenders(costArray, environment);

        blueprint.setAidCostSource(
                new AidCostSource() {
                    @Override
                    public boolean canPayAidCost(DefaultActionContext<DefaultGame> actionContext) {
                        for (EffectAppender costAppender : costAppenders) {
                            if (!costAppender.isPlayableInFull(actionContext))
                                return false;
                        }

                        return true;
                    }

                    @Override
                    public void appendAidCost(CostToEffectAction action, DefaultActionContext actionContext) {
                        for (EffectAppender costAppender : costAppenders)
                            costAppender.appendEffect(true, action, actionContext);
                    }
                });
    }
}
