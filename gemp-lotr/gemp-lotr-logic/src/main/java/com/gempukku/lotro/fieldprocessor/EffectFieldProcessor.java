package com.gempukku.lotro.fieldprocessor;

import com.gempukku.lotro.cards.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.effectprocessor.*;
import com.gempukku.lotro.modifiers.ModifyOwnCost;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EffectFieldProcessor implements FieldProcessor {
    private final Map<String, EffectProcessor> effectProcessors = new HashMap<>();

    public EffectFieldProcessor() {
        effectProcessors.put("activated", new ActivatedEffectProcessor());
        effectProcessors.put("activatedindiscard", new ActivatedInDiscardEffectProcessor());
        effectProcessors.put("activatedtrigger", new ActivatedTriggerEffectProcessor());
        effectProcessors.put("aidcost", new AidCost());
        effectProcessors.put("copycard", new CopyCard());
        effectProcessors.put("discardedfromplaytrigger", new DiscardedFromPlayTriggerEffectProcessor());
        effectProcessors.put("discount", new PotentialDiscount());
        effectProcessors.put("event", new EventEffectProcessor());
        effectProcessors.put("extracost", new ExtraCost());
        effectProcessors.put("inhandtrigger", new InHandTriggerEffectProcessor());
        effectProcessors.put("killedtrigger", new KilledTriggerEffectProcessor());
        effectProcessors.put("modifier", new ModifierProcessor());
        effectProcessors.put("modifyowncost", new ModifyOwnCost());
        effectProcessors.put("playedinotherphase", new PlayedInOtherPhase());
        effectProcessors.put("playoutofsequence", new PlayOutOfSequenceProcessor());
        effectProcessors.put("responseevent", new ResponseEventEffectProcessor());
        effectProcessors.put("trigger", new TriggerEffectProcessor());
    }

    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final JSONObject[] effectsArray = FieldUtils.getObjectArray(value, key);
        for (JSONObject effect : effectsArray) {
            final String effectType = FieldUtils.getString(effect.get("type"), "type");
            final EffectProcessor effectProcessor = effectProcessors.get(effectType.toLowerCase());
            if (effectProcessor == null)
                throw new InvalidCardDefinitionException("Unable to find effect of type: " + effectType);
            effectProcessor.processEffect(effect, blueprint, environment);
        }
    }
}
