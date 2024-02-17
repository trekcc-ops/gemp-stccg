package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.common.filterable.Uniqueness;
import com.gempukku.stccg.effectappender.EffectAppenderFactory;
import com.gempukku.stccg.fieldprocessor.*;
import com.gempukku.stccg.filters.FilterFactory;
import com.gempukku.stccg.modifiers.ModifierSourceFactory;
import com.gempukku.stccg.requirement.RequirementFactory;
import com.gempukku.stccg.requirement.trigger.TriggerCheckerFactory;
import org.json.simple.JSONObject;

import java.util.*;

public class CardBlueprintFactory implements CardGenerationEnvironment {
    private final Map<String, FieldProcessor> fieldProcessors = new HashMap<>();

    private final EffectAppenderFactory effectAppenderFactory = new EffectAppenderFactory();
    private final FilterFactory filterFactory = new FilterFactory();
    private final RequirementFactory requirementFactory = new RequirementFactory();
    private final TriggerCheckerFactory triggerCheckerFactory = new TriggerCheckerFactory();
    private final ModifierSourceFactory modifierSourceFactory = new ModifierSourceFactory();

    public CardBlueprintFactory() {
        fieldProcessors.put("title", new TitleFieldProcessor());
        fieldProcessors.put("property-logo", new PropertyLogoFieldProcessor());
        fieldProcessors.put("lore", new LoreFieldProcessor());
        fieldProcessors.put("subtitle", new SubtitleFieldProcessor());
        fieldProcessors.put("image-url", new ImageUrlFieldProcessor());
        fieldProcessors.put("tribble-value", new TribbleValueFieldProcessor());
        fieldProcessors.put("tribble-power", new TribblePowerFieldProcessor());
        fieldProcessors.put("uniqueness", new UniquenessFieldProcessor());
        fieldProcessors.put("side", new SideFieldProcessor());
        fieldProcessors.put("type", new CardTypeFieldProcessor());
        fieldProcessors.put("seedphase", new SeedPhaseProcessor());

        fieldProcessors.put("quadrant", new QuadrantFieldProcessor());
        fieldProcessors.put("region", new RegionFieldProcessor());
        fieldProcessors.put("location", new LocationFieldProcessor());
        fieldProcessors.put("caninsertintospaceline", new CanInsertIntoSpacelineProcessor());
        fieldProcessors.put("affiliation-icons", new AffiliationIconsFieldProcessor());
        fieldProcessors.put("mission-type", new MissionTypeFieldProcessor());
        fieldProcessors.put("point-box", new PointBoxFieldProcessor());

        fieldProcessors.put("race", new RaceFieldProcessor());
        fieldProcessors.put("affiliation", new AffiliationFieldProcessor());
        fieldProcessors.put("facility-type", new FacilityTypeFieldProcessor());
        fieldProcessors.put("itemclass", new PossessionClassFieldProcessor());
        fieldProcessors.put("keyword", new KeywordFieldProcessor());
        fieldProcessors.put("keywords", new KeywordFieldProcessor());
        fieldProcessors.put("twilight", new CostFieldProcessor());
        fieldProcessors.put("site", new SiteNumberFieldProcessor());
        fieldProcessors.put("direction", new DirectionFieldProcessor());
        fieldProcessors.put("target", new TargetFieldProcessor());
        fieldProcessors.put("requires", new RequirementFieldProcessor());
        fieldProcessors.put("effects", new EffectFieldProcessor());

        // Fields in the JSON, but not yet implemented
        fieldProcessors.put("game-text", new NullProcessor());
        fieldProcessors.put("mission-requirements", new NullProcessor());
        fieldProcessors.put("point-box", new NullProcessor());
        fieldProcessors.put("span", new NullProcessor());
        fieldProcessors.put("classification", new NullProcessor());
        fieldProcessors.put("icons", new NullProcessor()); // For misc personnel icons
        fieldProcessors.put("skill-box", new NullProcessor());
        fieldProcessors.put("restriction-box", new NullProcessor());
        fieldProcessors.put("span", new NullProcessor());
        fieldProcessors.put("integrity", new NullProcessor());
        fieldProcessors.put("cunning", new NullProcessor());
        fieldProcessors.put("strength", new NullProcessor());
    }

    public CardBlueprint buildFromJson(JSONObject json) throws InvalidCardDefinitionException {
//        LOGGER.debug("Calling buildFromJson");
        BuiltCardBlueprint result = new BuiltCardBlueprint();

        Set<Map.Entry<String, Object>> values = json.entrySet();
        for (Map.Entry<String, Object> value : values) {
            final String field = value.getKey().toLowerCase();
            final Object fieldValue = value.getValue();
            final FieldProcessor fieldProcessor = fieldProcessors.get(field);
            if (fieldProcessor == null)
                throw new InvalidCardDefinitionException("Unrecognized field: " + field);

//            LOGGER.debug("Processing field " + field + " with value " + fieldValue);
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

        // Set quadrant to alpha if none was specified
        List<CardType> implicitlyAlphaQuadrant = Arrays.asList(CardType.PERSONNEL, CardType.SHIP, CardType.FACILITY,
                CardType.MISSION);
        if (result.getQuadrant() == null && implicitlyAlphaQuadrant.contains(result.getCardType()))
            result.setQuadrant(Quadrant.ALPHA);

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
        public void processField(String key, Object value, BuiltCardBlueprint blueprint, CardGenerationEnvironment environment) {
            // Ignore
        }
    }
}
