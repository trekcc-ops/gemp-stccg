package com.gempukku.lotro.cards;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Uniqueness;
import com.gempukku.lotro.effectappender.EffectAppenderFactory;
import com.gempukku.lotro.fieldprocessor.*;
import com.gempukku.lotro.filters.FilterFactory;
import com.gempukku.lotro.modifiers.ModifierSourceFactory;
import com.gempukku.lotro.requirement.RequirementFactory;
import com.gempukku.lotro.requirement.trigger.TriggerCheckerFactory;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.*;

public class LotroCardBlueprintBuilder implements CardGenerationEnvironment {
    final Logger LOG = Logger.getLogger(LotroCardBlueprintBuilder.class);
    private final Map<String, FieldProcessor> fieldProcessors = new HashMap<>();

    private final EffectAppenderFactory effectAppenderFactory = new EffectAppenderFactory();
    private final FilterFactory filterFactory = new FilterFactory();
    private final RequirementFactory requirementFactory = new RequirementFactory();
    private final TriggerCheckerFactory triggerCheckerFactory = new TriggerCheckerFactory();
    private final ModifierSourceFactory modifierSourceFactory = new ModifierSourceFactory();

    public LotroCardBlueprintBuilder() {
        fieldProcessors.put("title", new TitleFieldProcessor());
        fieldProcessors.put("subtitle", new SubtitleFieldProcessor());
        fieldProcessors.put("image-url", new ImageUrlFieldProcessor());
        fieldProcessors.put("tribble-value", new TribbleValueFieldProcessor());
        fieldProcessors.put("tribble-power", new TribblePowerFieldProcessor());
        fieldProcessors.put("uniqueness", new UniquenessFieldProcessor());
        fieldProcessors.put("side", new SideFieldProcessor());
        fieldProcessors.put("culture", new CultureFieldProcessor());
        fieldProcessors.put("type", new CardTypeFieldProcessor());
        fieldProcessors.put("seedphase", new SeedPhaseProcessor());

        fieldProcessors.put("quadrant", new QuadrantFieldProcessor());
        fieldProcessors.put("region", new RegionFieldProcessor());
        fieldProcessors.put("location", new LocationFieldProcessor());

        fieldProcessors.put("race", new RaceFieldProcessor());
        fieldProcessors.put("affiliation", new AffiliationFieldProcessor());
        fieldProcessors.put("facility-type", new FacilityTypeFieldProcessor());
        fieldProcessors.put("itemclass", new PossessionClassFieldProcessor());
        fieldProcessors.put("keyword", new KeywordFieldProcessor());
        fieldProcessors.put("keywords", new KeywordFieldProcessor());
        fieldProcessors.put("twilight", new CostFieldProcessor());
        fieldProcessors.put("strength", new StrengthFieldProcessor());
        fieldProcessors.put("vitality", new VitalityFieldProcessor());
        fieldProcessors.put("resistance", new ResistanceFieldProcessor());
        fieldProcessors.put("site", new SiteNumberFieldProcessor());
        fieldProcessors.put("direction", new DirectionFieldProcessor());
        fieldProcessors.put("target", new TargetFieldProcessor());
        fieldProcessors.put("requires", new RequirementFieldProcessor());
        fieldProcessors.put("effects", new EffectFieldProcessor());

        fieldProcessors.put("gametext", new NullProcessor());
        fieldProcessors.put("lore", new NullProcessor());
        fieldProcessors.put("promotext", new NullProcessor());

        //Soon!  But not yet
        fieldProcessors.put("cardinfo", new NullProcessor());
        fieldProcessors.put("alts", new NullProcessor());
    }

    public LotroCardBlueprint buildFromJson(JSONObject json) throws InvalidCardDefinitionException {
//        LOG.debug("Calling buildFromJson");
        BuiltLotroCardBlueprint result = new BuiltLotroCardBlueprint();

        Set<Map.Entry<String, Object>> values = json.entrySet();
        for (Map.Entry<String, Object> value : values) {
            final String field = value.getKey().toLowerCase();
            final Object fieldValue = value.getValue();
            final FieldProcessor fieldProcessor = fieldProcessors.get(field);
            if (fieldProcessor == null)
                throw new InvalidCardDefinitionException("Unrecognized field: " + field);

//            LOG.debug("Processing field " + field + " with value " + fieldValue);
            fieldProcessor.processField(field, fieldValue, result, this);
        }

        // Apply uniqueness based on ST1E glossary
        List<CardType> implicitlyUniqueTypes = Arrays.asList(CardType.PERSONNEL, CardType.SHIP, CardType.FACILITY,
                CardType.SITE, CardType.MISSION, CardType.TIME_LOCATION);
        if (result.getUniqueness() == null) {
            if (implicitlyUniqueTypes.contains(result.getCardType())) {
                result.setUniqueness(Uniqueness.UNIQUE);
            } else {
                result.setUniqueness(Uniqueness.UNIVERSAL);
            }
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

    @Override
    public ModifierSourceFactory getModifierSourceFactory() {
        return modifierSourceFactory;
    }

    private static class NullProcessor implements FieldProcessor {
        @Override
        public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) {
            // Ignore
        }
    }
}
