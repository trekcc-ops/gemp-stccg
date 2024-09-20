package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.effectprocessor.*;
import com.gempukku.stccg.modifiers.ModifyOwnCost;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EffectFieldProcessor implements FieldProcessor {
    private final Map<String, EffectProcessor> effectProcessors = new HashMap<>();

    public EffectFieldProcessor() {
        effectProcessors.put("activated", new ActivatedEffectProcessor());
        effectProcessors.put("activatedindiscard", new ActivatedInDiscardEffectProcessor());
        effectProcessors.put("activatedtrigger", new ActivatedTriggerEffectProcessor());
        effectProcessors.put("discardedfromplaytrigger", new DiscardedFromPlayTriggerEffectProcessor());
        effectProcessors.put("extracost", new ExtraCost());
        effectProcessors.put("inhandtrigger", new InHandTriggerEffectProcessor());
        effectProcessors.put("modifier", new ModifierProcessor());
        effectProcessors.put("modifyowncost", new ModifyOwnCost());
        effectProcessors.put("playedinotherphase", new PlayedInOtherPhase());
        effectProcessors.put("playoutofsequence", new PlayOutOfSequenceProcessor());
        effectProcessors.put("responseevent", new ResponseEventEffectProcessor());
        effectProcessors.put("trigger", new TriggerEffectProcessor());

        // Deprecated from LotR
//        effectProcessors.put("event", new EventEffectProcessor());
    }

    @Override
    public void processField(String key, Object value, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        final JSONObject[] effectsArray = environment.getObjectArray(value, key);
        for (JSONObject effect : effectsArray) {
            final String effectType = environment.getString(effect.get("type"), "type");
            final EffectProcessor effectProcessor = effectProcessors.get(effectType.toLowerCase());
            if (effectProcessor == null)
                throw new InvalidCardDefinitionException("Unable to find effect of type: " + effectType);
            effectProcessor.processEffect(effect, blueprint, environment);
        }
    }
}
