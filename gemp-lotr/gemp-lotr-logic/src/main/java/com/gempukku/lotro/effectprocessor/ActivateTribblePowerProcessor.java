package com.gempukku.lotro.effectprocessor;

import com.gempukku.lotro.actions.DefaultActionSource;
import com.gempukku.lotro.cards.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.common.TribblePower;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.requirement.trigger.TriggerChecker;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class ActivateTribblePowerProcessor implements EffectProcessor {
    private final TribblePower _tribblePower;

    public ActivateTribblePowerProcessor(TribblePower tribblePower) {
        _tribblePower = tribblePower;
    }
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint,
                              CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
//        FieldUtils.validateAllowedFields(value, "tribble-power");
//        final Logger LOG = Logger.getLogger(ActivateTribblePowerProcessor.class);

        final boolean optional = true;

        JSONObject triggerObject = new JSONObject();
        triggerObject.put("type", "played");
        triggerObject.put("filter", "self");
        final JSONObject[] triggerArray = FieldUtils.getObjectArray(triggerObject, "trigger");
        String jsonString = null;
        JSONObject fullObject;

        if (_tribblePower == TribblePower.DISCARD) {
            jsonString = "{\"effect\":{\"forced\":true,\"type\":\"discardFromHand\"},\"optional\":true,";
            jsonString += "\"trigger\":{\"filter\":\"self\",\"type\":\"played\"},\"type\":\"trigger\"}";
        } else {
            throw new RuntimeException("Called ActivateTribblePowerProcessor unexpectedly");
        }

        try {
            JSONParser parser = new JSONParser();
            fullObject = (JSONObject) parser.parse(jsonString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        for (JSONObject trigger : triggerArray) {
            final TriggerChecker triggerChecker =
                    environment.getTriggerCheckerFactory().getTriggerChecker(trigger, environment);
            final boolean before = triggerChecker.isBefore();

            DefaultActionSource triggerActionSource = new DefaultActionSource();
            triggerActionSource.addPlayRequirement(triggerChecker);
            EffectUtils.processRequirementsCostsAndEffects(fullObject, environment, triggerActionSource);

            if (before) {
                if (optional)
                    blueprint.appendOptionalBeforeTrigger(triggerActionSource);
                else
                    blueprint.appendRequiredBeforeTrigger(triggerActionSource);
            } else {
                if (optional)
                    blueprint.appendOptionalAfterTrigger(triggerActionSource);
                else
                    blueprint.appendRequiredAfterTrigger(triggerActionSource);
            }
        }

    }
}