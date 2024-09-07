package com.gempukku.stccg.requirement.producers;

import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementProducer;
import org.json.simple.JSONObject;

public class IsEqual extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "firstNumber", "secondNumber");

        final ValueSource firstNumber = ValueResolver.resolveEvaluator(object.get("firstNumber"), environment);
        final ValueSource secondNumber = ValueResolver.resolveEvaluator(object.get("secondNumber"), environment);

        return actionContext -> firstNumber.evaluateExpression(actionContext, null) ==
                secondNumber.evaluateExpression(actionContext, null);
    }
}
