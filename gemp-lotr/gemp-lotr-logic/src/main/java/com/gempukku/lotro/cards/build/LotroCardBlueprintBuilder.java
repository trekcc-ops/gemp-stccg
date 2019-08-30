package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.cards.build.field.*;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderFactory;
import com.gempukku.lotro.cards.build.field.effect.filter.FilterFactory;
import com.gempukku.lotro.cards.build.field.effect.requirement.RequirementFactory;
import com.gempukku.lotro.cards.build.field.effect.trigger.TriggerCheckerFactory;
import com.gempukku.lotro.game.LotroCardBlueprint;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LotroCardBlueprintBuilder implements CardGenerationEnvironment {
    private Map<String, FieldProcessor> fieldProcessors = new HashMap<>();

    private EffectAppenderFactory effectAppenderFactory = new EffectAppenderFactory();
    private FilterFactory filterFactory = new FilterFactory();
    private RequirementFactory requirementFactory = new RequirementFactory();
    private TriggerCheckerFactory triggerCheckerFactory = new TriggerCheckerFactory();

    public LotroCardBlueprintBuilder() {
        fieldProcessors.put("title", new TitleFieldProcessor());
        fieldProcessors.put("subtitle", new SubtitleFieldProcessor());
        fieldProcessors.put("side", new SideFieldProcessor());
        fieldProcessors.put("type", new CardTypeFieldProcessor());
        fieldProcessors.put("culture", new CultureFieldProcessor());
        fieldProcessors.put("race", new RaceFieldProcessor());
        fieldProcessors.put("signet", new SignetFieldProcessor());
        fieldProcessors.put("keyword", new KeywordFieldProcessor());
        fieldProcessors.put("cost", new CostFieldProcessor());
        fieldProcessors.put("strength", new StrengthFieldProcessor());
        fieldProcessors.put("vitality", new VitalityFieldProcessor());
        fieldProcessors.put("resistance", new ResistanceFieldProcessor());
        fieldProcessors.put("block", new SiteBlockFieldProcessor());
        fieldProcessors.put("site", new SiteNumberFieldProcessor());
        fieldProcessors.put("possession", new PossessionClassFieldProcessor());
        fieldProcessors.put("direction", new DirectionFieldProcessor());
        fieldProcessors.put("effects", new EffectFieldProcessor());
        fieldProcessors.put("target", new TargetFieldProcessor());
        fieldProcessors.put("requirement", new RequirementFieldProcessor());
        fieldProcessors.put("allyhome", new AllyHomeFieldProcessor());
    }

    public LotroCardBlueprint buildFromJson(JSONObject json) throws InvalidCardDefinitionException {
        BuiltLotroCardBlueprint result = new BuiltLotroCardBlueprint();

        Set<Map.Entry<String, Object>> values = json.entrySet();
        for (Map.Entry<String, Object> value : values) {
            final String field = value.getKey().toLowerCase();
            final Object fieldValue = value.getValue();
            final FieldProcessor fieldProcessor = fieldProcessors.get(field);
            if (fieldProcessor == null)
                throw new InvalidCardDefinitionException("Unrecognized field: " + field);

            fieldProcessor.processField(field, fieldValue, result, this);
        }

        result.validateConsistency();

        return result;
    }

    @Override
    public EffectAppenderFactory getEffectAppenderFactory() {
        return effectAppenderFactory;
    }

    @Override
    public FilterFactory getFilterFactory() {
        return filterFactory;
    }

    @Override
    public RequirementFactory getRequirementFactory() {
        return requirementFactory;
    }

    @Override
    public TriggerCheckerFactory getTriggerCheckerFactory() {
        return triggerCheckerFactory;
    }
}
