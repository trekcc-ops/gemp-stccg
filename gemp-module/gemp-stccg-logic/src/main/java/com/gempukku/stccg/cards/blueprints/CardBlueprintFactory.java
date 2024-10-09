package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.fieldprocessor.*;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.common.filterable.Uniqueness;
import com.gempukku.stccg.effectappender.EffectAppender;
import com.gempukku.stccg.effectappender.EffectAppenderFactory;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.filters.FilterFactory;
import com.gempukku.stccg.modifiers.ModifierSourceFactory;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementFactory;
import com.gempukku.stccg.requirement.trigger.TriggerCheckerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class CardBlueprintFactory {
    private final Map<String, FieldProcessor> fieldProcessors = new HashMap<>();

    private final EffectAppenderFactory effectAppenderFactory = new EffectAppenderFactory(this);
    private final FilterFactory filterFactory = new FilterFactory(this);
    private final RequirementFactory requirementFactory = new RequirementFactory(this);
    private final TriggerCheckerFactory triggerCheckerFactory = new TriggerCheckerFactory();
    private final ModifierSourceFactory modifierSourceFactory = new ModifierSourceFactory();

    public CardBlueprintFactory() {
        // String input
        for (String fieldName : new String[]{"title", "lore", "subtitle", "rarity", "image-url", "persona"}) {
            fieldProcessors.put(fieldName, new StringFieldProcessor());
        }

        fieldProcessors.put("property-logo", new PropertyLogoFieldProcessor());
        fieldProcessors.put("uniqueness", new UniquenessFieldProcessor());
        fieldProcessors.put("type", new CardTypeFieldProcessor());
        fieldProcessors.put("seed", new SeedFieldProcessor());
        fieldProcessors.put("icons", new IconsFieldProcessor());
        fieldProcessors.put("tribble-value", new TribbleValueFieldProcessor());
        fieldProcessors.put("tribble-power", new TribblePowerFieldProcessor());
        fieldProcessors.put("characteristic", new CharacteristicFieldProcessor());

        fieldProcessors.put("quadrant", new QuadrantFieldProcessor());
        fieldProcessors.put("region", new RegionFieldProcessor());
        fieldProcessors.put("location", new LocationFieldProcessor());
        fieldProcessors.put("caninsertintospaceline", new CanInsertIntoSpacelineProcessor());
        fieldProcessors.put("affiliation-icons", new AffiliationIconsFieldProcessor());
        fieldProcessors.put("mission-type", new MissionTypeFieldProcessor());
        fieldProcessors.put("mission-requirements", new MissionRequirementsFieldProcessor());
        fieldProcessors.put("point-box", new PointBoxFieldProcessor());
        fieldProcessors.put("span", new SpanFieldProcessor());
        fieldProcessors.put("integrity", new AttributeFieldProcessor(CardAttribute.INTEGRITY));
        fieldProcessors.put("cunning", new AttributeFieldProcessor(CardAttribute.CUNNING));
        fieldProcessors.put("strength", new AttributeFieldProcessor(CardAttribute.STRENGTH));
        fieldProcessors.put("range", new AttributeFieldProcessor(CardAttribute.RANGE));
        fieldProcessors.put("weapons", new AttributeFieldProcessor(CardAttribute.WEAPONS));
        fieldProcessors.put("shields", new AttributeFieldProcessor(CardAttribute.SHIELDS));
        fieldProcessors.put("classification", new ClassificationFieldProcessor());
        fieldProcessors.put("skill-box", new SkillBoxFieldProcessor());
        fieldProcessors.put("skills2e", new SkillBoxFieldProcessor());
        fieldProcessors.put("species", new SpeciesFieldProcessor());
        fieldProcessors.put("image-options", new ImageOptionsFieldProcessor());

        fieldProcessors.put("affiliation", new AffiliationFieldProcessor());
        fieldProcessors.put("staffing", new StaffingFieldProcessor());
        fieldProcessors.put("facility-type", new FacilityTypeFieldProcessor());
        fieldProcessors.put("cost", new CostFieldProcessor());
        fieldProcessors.put("keyword", new KeywordFieldProcessor());
        fieldProcessors.put("keywords", new KeywordFieldProcessor());
        fieldProcessors.put("target", new TargetFieldProcessor());
        fieldProcessors.put("requires", new RequirementFieldProcessor());
        fieldProcessors.put("effects", new EffectFieldProcessor());

        // Fields in the JSON, but not yet implemented
        fieldProcessors.put("gametext", new NullProcessor());
        fieldProcessors.put("ship-class", new NullProcessor());
        fieldProcessors.put("restriction-box", new NullProcessor());
        fieldProcessors.put("dilemma-type", new NullProcessor());
        fieldProcessors.put("headquarters", new NullProcessor());   // Flavor text for 2E headquarters?
        fieldProcessors.put("playable", new NullProcessor()); // Cards that can be played to a 2E headquarters
    }

    public CardBlueprint buildFromJava(String blueprintId) throws InvalidCardDefinitionException {
        try {
            return (CardBlueprint) Class.forName("com.gempukku.stccg.cards.blueprints.Blueprint" + blueprintId)
                    .getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException e) {
            throw new InvalidCardDefinitionException("No valid Java class found for blueprint " + blueprintId);
        }
    }

    public CardBlueprint buildFromJsonNew(String blueprintId, JsonNode json) throws InvalidCardDefinitionException {
        CardBlueprint result;
        result = new CardBlueprint(blueprintId);

        if (json.has("java-blueprint"))
            result = buildFromJava(blueprintId);

        Iterator<String> iterator = json.fieldNames();
        List<String> keys = new ArrayList<>();
        iterator.forEachRemaining(keys::add);
        keys.remove("java-blueprint");
        keys.remove("blueprintId");

        for (String key : keys) {
            final String field = key.toLowerCase();
            final JsonNode fieldValue = json.get(field);
            final FieldProcessor fieldProcessor = fieldProcessors.get(field);
            if (fieldProcessor == null)
                throw new InvalidCardDefinitionException("Unrecognized field: " + field);
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

        // Set rarity to P if none was specified
        if (result.getRarity() == null)
            result.setRarity("P");

        result.validateConsistency();

        return result;
    }

    public EffectAppenderFactory getEffectAppenderFactory() {
        return effectAppenderFactory;
    }

    public FilterFactory getFilterFactory() {
        return filterFactory;
    }

    public TriggerCheckerFactory getTriggerCheckerFactory() {
        return triggerCheckerFactory;
    }

    public ModifierSourceFactory getModifierSourceFactory() {
        return modifierSourceFactory;
    }

    private static class NullProcessor implements FieldProcessor {
        @Override
        public void processField(String key, JsonNode value, CardBlueprint blueprint,
                                 CardBlueprintFactory environment) {
            // Ignore
        }
    }

    public int getInteger(JsonNode parentNode, String key, int defaultValue) throws InvalidCardDefinitionException {
        if (!parentNode.has(key))
            return defaultValue;
        else {
            JsonNode node = parentNode.get(key);
            if (!node.isInt())
                throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
            else return node.asInt(defaultValue);
        }
    }


    public String getString(JsonNode parentNode, String key) throws InvalidCardDefinitionException {
        if (!parentNode.has(key))
            return null;
        else return parentNode.get(key).textValue();
    }

    public String getString(JsonNode parentNode, String key, String defaultValue)
            throws InvalidCardDefinitionException {
        if (parentNode == null || !parentNode.has(key))
            return defaultValue;
        else
            return parentNode.get(key).textValue();
    }

    public FilterableSource getFilterable(JsonNode node) throws InvalidCardDefinitionException {
        return filterFactory.generateFilter(node.get("filter").textValue());
    }


    public FilterableSource getFilterable(JsonNode node, String defaultValue)
            throws InvalidCardDefinitionException {
        if (!node.has("filter"))
            return filterFactory.generateFilter(defaultValue);
        else return getFilterable(node);
    }


    public boolean getBoolean(JsonNode parentNode, String key) throws InvalidCardDefinitionException {
        return getBoolean(parentNode, key, null);
    }

    public boolean getBoolean(JsonNode parentNode, String key, boolean defaultValue)
            throws InvalidCardDefinitionException {
        return getBoolean(parentNode, key, Boolean.valueOf(defaultValue));
    }

    public boolean getBoolean(JsonNode parentNode, String key, Boolean defaultValue)
            throws InvalidCardDefinitionException {
        if (parentNode.has(key)) {
            JsonNode node = parentNode.get(key);
            if (!node.isBoolean())
                throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
            else return node.asBoolean();
        } else {
            if (defaultValue != null)
                return defaultValue;
            else throw new InvalidCardDefinitionException("Value of " + key + " is required");
        }
    }


    public <T extends Enum<T>> T getEnum(Class<T> enumClass, String value, String key)
            throws InvalidCardDefinitionException {
        if (value == null)
            return null;
        try {
            return Enum.valueOf(enumClass, value.toUpperCase().replaceAll("[ '\\-.]", "_"));
        } catch(Exception exp) {
            throw new InvalidCardDefinitionException("Unable to process enum value " + value + " in " + key + " field");
        }
    }

    public <T extends Enum<T>> T getEnum(Class<T> enumClass, String string) throws InvalidCardDefinitionException {
        try {
            return Enum.valueOf(enumClass, string.trim().toUpperCase().replaceAll("[ '\\-.]", "_"));
        } catch(Exception exp) {
            throw new InvalidCardDefinitionException("Unable to process enum value " + string);
        }
    }

    public PlayerSource getPlayerSource(JsonNode parentNode, String key, boolean useYouAsDefault)
            throws InvalidCardDefinitionException {
        String playerString;
        if (parentNode.get(key) == null && useYouAsDefault)
            playerString = "you";
        else playerString = parentNode.get(key).textValue();
        return PlayerResolver.resolvePlayer(playerString);
    }

    public Requirement getRequirement(JsonNode object) throws InvalidCardDefinitionException {
        return requirementFactory.getRequirement(object);
    }


    public JsonNode[] getNodeArray(JsonNode node) {
        List<JsonNode> nodes = new LinkedList<>();
        if (node == null)
            return new JsonNode[0];
        else if (node.isArray()) {
            for (JsonNode elem : node) {
                nodes.add(elem);
            }
        } else {
            nodes.add(node);
        }
        return nodes.toArray(new JsonNode[0]);
    }

    public String[] getStringArray(JsonNode node) {
        JsonNode[] nodeArray = getNodeArray(node);
        String[] stringArray = new String[nodeArray.length];
        for (int i = 0; i < nodeArray.length; i++) {
            stringArray[i] = nodeArray[i].textValue();
        }
        return stringArray;
    }


    public Requirement[] getRequirementsFromJSON(JsonNode parentNode) throws InvalidCardDefinitionException {
        List<Requirement> result = new LinkedList<>();
        if (parentNode.has("requires")) {
            if (parentNode.get("requires").isArray()) {
                for (JsonNode requirement : parentNode.get("requires"))
                    result.add(getRequirement(requirement));
            } else result.add(getRequirement(parentNode.get("requires")));
        }
        return result.toArray(new Requirement[0]);
    }


    public List<EffectAppender> getEffectAppendersFromJSON(JsonNode node)
            throws InvalidCardDefinitionException {
        List<EffectAppender> appenders = new LinkedList<>();
        if (node.isArray()) {
            for (JsonNode effect : node)
                appenders.add(effectAppenderFactory.getEffectAppender(effect));
        } else {
            appenders.add(effectAppenderFactory.getEffectAppender(node));
        }
        return appenders;
    }


    public void validateAllowedFields(JsonNode node, String... fields) throws InvalidCardDefinitionException {
        List<String> keys = new ArrayList<>();
        node.fieldNames().forEachRemaining(keys::add);
        for (String key : keys) {
            if (!key.equals("type") && !Arrays.asList(fields).contains(key))
                throw new InvalidCardDefinitionException("Unrecognized field: " + key);
        }
    }
}
