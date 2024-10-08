package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.SpecialDownloadSkill;
import com.gempukku.stccg.cards.blueprints.fieldprocessor.EffectFieldProcessor;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.cards.blueprints.fieldprocessor.ActionSourceAppender;
import com.gempukku.stccg.condition.missionrequirements.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class CardBlueprintDeserializer extends StdDeserializer<CardBlueprint> {

    final private CardBlueprintFactory _blueprintFactory = new CardBlueprintFactory();
    final private EffectFieldProcessor _effectProcessor = new EffectFieldProcessor();
    final Map<String, SkillName> _skillMap = new HashMap<>();
    final Map<String, PersonnelName> _personnelNameMap = new HashMap<>();
    final String multiplierSplit1e = "(?=x\\d+)";
    final String multiplierSplit2e = "(?<=\\d).*(?=\\s\\w)";


    public CardBlueprintDeserializer() {
        this(null);
    }

    public CardBlueprintDeserializer(Class<?> vc) {
        super(vc);
        new ArrayList<>(Arrays.asList(SkillName.values()))
                .forEach(regularSkill -> _skillMap.put(regularSkill.get_humanReadable().toUpperCase(), regularSkill));
        new ArrayList<>(Arrays.asList(PersonnelName.values()))
                .forEach(name -> _personnelNameMap.put(name.getHumanReadable(), name));
    }

    @Override
    public CardBlueprint deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        try {
            JsonNode node = jp.getCodec().readTree(jp);
            CardBlueprint blueprint;
            validateAllowedFields(node, "java-blueprint", "blueprintId", "title", "image-url", "type", "lore",
                    "property-logo", "rarity", "location", "mission-type", "span", "mission-requirements",
                    "affiliation-icons", "point-box", "integrity", "cunning", "strength", "affiliation",
                    "classification", "icons", "skill-box", "facility-type", "uniqueness", "ship-class", "gametext",
                    "range", "weapons", "shields", "region", "staffing", "persona", "characteristic",
                    "image-options", "effects", "subtitle", "quadrant", "tribble-value", "headquarters", "cost",
                    "playable", "species", "tribble-power");
            validateRequiredFields(node, "title", "image-url", "type", "blueprintId");

            // TODO - Implement gametext, ship class, headquarters, playable

            // All cards have these fields
            String blueprintId = getString(node, "blueprintId");
            
            if (node.has("java-blueprint")) {
                try {
                    blueprint = (CardBlueprint) Class.forName("com.gempukku.stccg.cards.blueprints.Blueprint" + blueprintId)
                            .getDeclaredConstructor().newInstance();
                } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                         IllegalAccessException e) {
                    throw new InvalidCardDefinitionException("No valid Java class found for blueprint " + blueprintId);
                }
            } else {
                blueprint = new CardBlueprint(blueprintId);
            }

            if (!Objects.equals(blueprint.getBlueprintId(),blueprintId))
                throw new InvalidCardDefinitionException(
                        "Non-matching blueprintIds: '" + blueprintId + "' vs. '" + blueprint.getBlueprintId());

            // All cards have these fields
            blueprint.setTitle(getString(node, "title"));
            blueprint.setImageUrl(getString(node, "image-url"));
            blueprint.setCardType(getEnum(CardType.class, node, "type"));

            // Most cards have these fields
            blueprint.setLore(getString(node, "lore"));
            blueprint.setPropertyLogo(getEnum(PropertyLogo.class, node, "property-logo"));
            blueprint.setRarity(getString(node, "rarity"));
            blueprint.setUniqueness(getEnum(Uniqueness.class, node, "uniqueness"));

            // Missions & time locations
            blueprint.setLocation(getString(node, "location"));
            blueprint.setMissionType(getEnum(MissionType.class, node, "mission-type"));
            blueprint.setSpan(getInteger(node, "span"));
            String missionRequirementsText = getString(node, "mission-requirements");
            blueprint.setMissionRequirementsText(missionRequirementsText);
            blueprint.setMissionRequirements(createRequirement(missionRequirementsText));
            blueprint.setRegion(getEnum(Region.class, node, "region"));

            if (node.has("affiliation-icons")) {
                for (String icon : node.get("affiliation-icons").textValue().split(",")) {
                    if (icon.equals("any")) {
                        blueprint.setAnyCrewOrAwayTeamCanAttempt();
                    } else {
                        blueprint.addOwnerAffiliationIcon(
                                getEnum(Affiliation.class, icon, "affiliation-icons"));
                    }
                }
            }

            blueprint.setPointsShown(getPointsShown(node.get("point-box")));
            blueprint.setHasPointBox(node.has("point-box"));

            // Personnel
            blueprint.setAttribute(CardAttribute.INTEGRITY, getInteger(node, "integrity"));
            blueprint.setAttribute(CardAttribute.CUNNING, getInteger(node, "cunning"));
            blueprint.setAttribute(CardAttribute.STRENGTH, getInteger(node, "strength"));

            for (Affiliation affiliation :
                    getEnumSetFromCommaDelimited(node, "affiliation", Affiliation.class))
                blueprint.addAffiliation(affiliation);

            blueprint.setClassification(getEnum(SkillName.class, node, "classification"));
            blueprint.setIcons(getCardIconListFromCommaDelimited(node, "icons"));

            if (node.get("skill-box") != null) {
                SkillBox skillBox = new SkillBox(node.get("skill-box"));
                blueprint.setSkillDotIcons(skillBox.getSkillDots());
                blueprint.setSpecialDownloadIcons(skillBox.getSdIcons());
                for (Skill skill : skillBox.getSkillList()) {
                    blueprint.addSkill(skill);
                }
            }

            // Facilities
            blueprint.setFacilityType(getEnum(FacilityType.class, node, "facility-type"));

            // Ships
            blueprint.setAttribute(CardAttribute.RANGE, getInteger(node, "range"));
            blueprint.setAttribute(CardAttribute.WEAPONS, getInteger(node, "weapons"));
            blueprint.setAttribute(CardAttribute.SHIELDS, getInteger(node, "shields"));
            blueprint.setStaffing(getCardIconListFromCommaDelimited(node, "staffing"));

            // Misc
            blueprint.setSubtitle(getString(node, "subtitle"));
            blueprint.setQuadrant(getEnum(Quadrant.class, node, "quadrant"));
            blueprint.setSpecies(getEnum(Species.class, node, "species"));
            blueprint.setPersona(getString(node, "persona"));
            blueprint.setTribbleValue(getInteger(node, "tribble-value"));
            blueprint.setCost(getInteger(node, "cost"));
            setImageOptions(blueprint, node);
            if (node.get("tribble-power") != null) {
                processTribblePower(blueprint, getEnum(TribblePower.class, node, "tribble-power"));
            }

            if (node.get("effects") != null)
                _effectProcessor.processField("effects", node.get("effects"), blueprint, _blueprintFactory);

            for (Characteristic characteristic :
                    getEnumSetFromCommaDelimited(node, "characteristic", Characteristic.class))
                blueprint.addCharacteristic(characteristic);

            return blueprint;
        } catch(InvalidCardDefinitionException exp) {
            throw new IOException(exp.getMessage(), exp);
        }
    }

    private void validateAllowedFields(JsonNode node, String... fields) throws InvalidCardDefinitionException {
        List<String> keys = new ArrayList<>();
        node.fieldNames().forEachRemaining(keys::add);
        for (String key : keys) {
            if (!Arrays.asList(fields).contains(key))
                throw new InvalidCardDefinitionException("Unrecognized field: " + key);
        }
    }

    private void validateRequiredFields(JsonNode node, String... fields) throws InvalidCardDefinitionException {
        List<String> keys = new ArrayList<>();
        node.fieldNames().forEachRemaining(keys::add);
        for (String field : fields) {
            if (!keys.contains(field))
                throw new InvalidCardDefinitionException("Could not find required field " + field +
                        " for blueprint " + node.get("blueprintId"));
        }
    }

    private <T extends Enum<T>> Set<T> getEnumSetFromCommaDelimited(
            JsonNode parentNode, String fieldName, Class<T> enumClass) throws InvalidCardDefinitionException {
        Set<T> result = new HashSet<>();
        if (parentNode.get(fieldName) == null || parentNode.get(fieldName).isNull())
            return result;
        for (String item : parentNode.get(fieldName).textValue().split(",")) {
            result.add(getEnum(enumClass, item, fieldName));
        }
        return result;
    }

    private List<CardIcon> getCardIconListFromCommaDelimited(JsonNode parentNode, String fieldName)
            throws InvalidCardDefinitionException {
        List<CardIcon> result = new LinkedList<>();
        if (parentNode.get(fieldName) == null || parentNode.get(fieldName).isNull())
            return result;
        for (String item : parentNode.get(fieldName).textValue().split(",")) {
            result.add(getEnum(CardIcon.class, item, fieldName));
        }
        return result;
    }

    private String getString(JsonNode parentNode, String fieldName) {
        if (parentNode.get(fieldName) == null)
            return null;
        else return parentNode.get(fieldName).textValue();
    }

    private Integer getInteger(JsonNode parentNode, String fieldName) throws InvalidCardDefinitionException {
        if (parentNode.get(fieldName) == null)
            return 0;
        if (!parentNode.get(fieldName).isInt())
            throw new InvalidCardDefinitionException("Unable to process value for " + fieldName);
        else return parentNode.get(fieldName).asInt();
    }


    private <T extends Enum<T>> T getEnum(Class<T> enumClass, JsonNode parentNode, String fieldName)
            throws InvalidCardDefinitionException {
        JsonNode value = parentNode.get(fieldName);
        if (value == null) return null;
        if (!value.isTextual())
            throw new InvalidCardDefinitionException("Unable to process enum value for " + fieldName);
        return getEnum(enumClass, value.textValue(), fieldName);
    }

    private <T extends Enum<T>> T getEnum(Class<T> enumClass, String value, String fieldName)
            throws InvalidCardDefinitionException {
        if (value == null) return null;
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase().replaceAll("[ '\\-.]", "_"));
        } catch(Exception exp) {
            throw new InvalidCardDefinitionException("Unable to process enum value " + value + " for " + fieldName);
        }
    }

    private MissionRequirement createRequirement(String text) throws InvalidCardDefinitionException {
        if (text == null)
            return null;
        String orNoParens = "\\s+(?i)OR\\s+(?![^(]*\\))";
        String andNoParens = "(\\s\\+\\s+|,\\sand\\s+|,\\s+|\\sand\\s)(?![^(]*\\))";
        String attributeSplit = "(?=>\\d+)|(?=<\\d+)";

        if (text.split(orNoParens).length > 1) {
            String[] stringSplit = text.split(orNoParens);
            List<MissionRequirement> requirements = new LinkedList<>();
            for (String string : stringSplit) {
                requirements.add(createRequirement(string));
            }
            return new OrMissionRequirement(requirements);
        }
        if (text.split(andNoParens).length > 1) {
            String[] stringSplit = text.split(andNoParens);
            List<MissionRequirement> requirements = new LinkedList<>();
            for (String string : stringSplit) {
                requirements.add(createRequirement(string));
            }
            return new AndMissionRequirement(requirements);
        }
        if (text.startsWith("(") && text.endsWith(")")) {
            return createRequirement(text.substring(1, text.length() - 1));
        }
        if (text.split(multiplierSplit1e).length > 1) {
            String[] stringSplit = text.split(multiplierSplit1e);
            for (int i = 0; i < 2; i++) {
                stringSplit[i] = stringSplit[i].trim();
            }
            if (_skillMap.get(stringSplit[0].toUpperCase()) != null)
                return new RegularSkillMissionRequirement(
                        _skillMap.get(stringSplit[0].toUpperCase()), Integer.parseInt(stringSplit[1].substring(1)));
        }
        if (text.split(multiplierSplit2e).length > 1) {
            String[] stringSplit = text.split(multiplierSplit2e);
            for (int i = 0; i < 2; i++) {
                stringSplit[i] = stringSplit[i].trim();
            }
            if (_skillMap.get(stringSplit[1].toUpperCase()) != null)
                return new RegularSkillMissionRequirement(
                        _skillMap.get(stringSplit[1].toUpperCase()), Integer.parseInt(stringSplit[0]));
        }
        if (text.split(attributeSplit).length > 1) {
            String[] stringSplit = text.split(attributeSplit);
            if (stringSplit[1].charAt(0) == '>')
                return new AttributeMissionRequirement(getEnum(
                        CardAttribute.class, stringSplit[0].trim(), "card attribute"),
                        Integer.parseInt(stringSplit[1].substring(1)));
        }
        if (_skillMap.get(text.toUpperCase()) != null) {
            return new RegularSkillMissionRequirement(_skillMap.get(text.toUpperCase()));
        }
        if (_personnelNameMap.get(text) != null) {
            return new PersonnelNameMissionRequirement(_personnelNameMap.get(text));
        }
        // If none of these worked, throw an exception
        throw new InvalidCardDefinitionException("Unable to process mission requirement: " + text);
    }

    int getPointsShown(JsonNode value) {
        String str;
        if (value == null)
            return 0;
        if (value.isTextual())
            str = value.textValue();
        else str = value.toString();

        str = str.replaceAll("[^\\d]", "");
        if (str.isEmpty())
            return 0;
        else
            return Integer.parseInt(str);
    }

    private class SkillBox {
        private final int _skillDots;
        private final int _sdIcons;
        private final List<Skill> _skillList = new LinkedList<>();

        private SkillBox(JsonNode node) throws InvalidCardDefinitionException {
            String[] skillArray = node.textValue().split("(?=\\[\\*])|(?=\\[DL])");
            int skillDots = 0;
            int sdIcons = 0;
            for (String string : skillArray) {
                if (string.trim().startsWith("[*]"))
                    skillDots++;
                if (string.trim().startsWith("[DL]"))
                    sdIcons++;
            }
            _skillDots = skillDots;
            _sdIcons = sdIcons;

            for (String string : skillArray) {
                _skillList.add(getSkill(string));
            }
        }

        private int getSkillDots() { return _skillDots; }
        private int getSdIcons() { return _sdIcons; }
        private List<Skill> getSkillList() { return _skillList; }

    }

    private Skill getSkill(String string) throws InvalidCardDefinitionException {
        String skillName;
        int skillLevel;
        String iconSplit = "(?<=\\[\\*])|(?<=\\[DL])";
        String[] skillSplit = string.split(iconSplit);

        if (skillSplit[0].trim().equals("[*]")) {
            String skill = skillSplit[1].trim();
            if (skill.split(multiplierSplit1e).length > 1) {
                // 1E-style multiplier (like Leadership x2)
                String[] stringSplit = skill.split(multiplierSplit1e);
                skillName = stringSplit[0].trim();
                skillLevel = Integer.parseInt(stringSplit[1].substring(1).trim());
            } else if (skill.split(multiplierSplit2e).length > 1) {
                // 2E-style multiplier (like 2 Leadership)
                String[] stringSplit = skill.split(multiplierSplit2e);
                skillName = stringSplit[1].trim();
                skillLevel = Integer.parseInt(stringSplit[0]);
            } else {
                // No multiplier
                skillName = skill;
                skillLevel = 1;
            }
            if (_skillMap.get(skillName.toUpperCase()) != null)
                return new RegularSkill(_skillMap.get(skillName.toUpperCase()), skillLevel);
            else
                // TODO - Handler for special skills?
                throw new InvalidCardDefinitionException("Skill " + skillName + " doesn't match known skills");

        } else if (skillSplit[0].trim().equals("[DL]")) {
            return new SpecialDownloadSkill(); // TODO - Identify what is being allowed to download
        } else {
            throw new InvalidCardDefinitionException("Invalid skill syntax in JSON file");
        }
    }

    private void setImageOptions(CardBlueprint blueprint, JsonNode parentNode) throws InvalidCardDefinitionException {
        JsonNode value = parentNode.get("image-options");
        if (value != null && value.isArray()) {
            for (JsonNode image : value) {
                blueprint.addImageOption(getEnum(Affiliation.class, image, "affiliation"),
                        image.get("image-url").textValue());
            }
        } else if (value != null)
            throw new InvalidCardDefinitionException(
                    "Image options blueprint field could not be processed as an array");
    }

    private void processTribblePower(CardBlueprint blueprint, TribblePower tribblePower)
            throws InvalidCardDefinitionException {
        blueprint.setTribblePower(tribblePower);
        if (tribblePower.isActive()) {
            String jsonString = "{\"effect\":{\"type\":\"activateTribblePower\"}";
            if (tribblePower == TribblePower.AVALANCHE) {
                jsonString += ",\"requires\":{\"type\":\"cardsInHandMoreThan\",\"player\":\"you\",\"count\":3}";
            }
            jsonString += ",\"optional\":true,\"trigger\":{\"filter\":\"self\",\"type\":\"played\"},\"type\":\"trigger\"}";

            try {
                new ActionSourceAppender().processEffect(
                        new ObjectMapper().readTree(jsonString), blueprint, _blueprintFactory);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}