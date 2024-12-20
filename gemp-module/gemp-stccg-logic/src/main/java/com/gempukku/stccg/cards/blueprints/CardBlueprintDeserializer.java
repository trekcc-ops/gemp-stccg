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
import com.gempukku.stccg.cards.blueprints.effect.EffectFieldProcessor;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.condition.missionrequirements.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class CardBlueprintDeserializer extends StdDeserializer<CardBlueprint> {

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
            BlueprintUtils.validateRequiredFields(node, "title", "image-url", "type", "blueprintId");
            final CardBlueprint blueprint = createBlueprint(node);

            List<String> fieldNames = new ArrayList<>();
            node.fieldNames().forEachRemaining(fieldNames::add);
            for (String fieldName : fieldNames) {
                switch(fieldName) {
                    // Attributes and ignored fields are at the top of this list, otherwise it is in alphabetical order

                    case "blueprintId", "java-blueprint": break; // Already processed by createBlueprint
                    case "headquarters", "playable": break; // No implementation built yet for these 2E fields
                    case "cunning", "integrity", "range", "shields", "strength", "weapons":
                        blueprint.setAttribute(
                                CardAttribute.valueOf(fieldName.toUpperCase()), getInteger(node, fieldName));
                        break;

                        // Alphabetical order going forward
                    case "affiliation":
                        for (Affiliation affiliation :
                                getEnumSetFromCommaDelimited(node, fieldName, Affiliation.class))
                            blueprint.addAffiliation(affiliation);
                        break;
                    case "affiliation-icons":
                        for (String icon : node.get(fieldName).textValue().split(",")) {
                            if (icon.equals("any")) blueprint.setAnyCrewOrAwayTeamCanAttempt();
                            else if (icon.equals("any except borg")) blueprint.setAnyExceptBorgCanAttempt();
                            else blueprint.addOwnerAffiliationIcon(
                                    getEnum(Affiliation.class, icon, fieldName));
                        }
                        break;
                    case "characteristic":
                        for (Characteristic characteristic :
                                getEnumSetFromCommaDelimited(node, fieldName, Characteristic.class))
                            blueprint.addCharacteristic(characteristic);
                        break;
                    case "classification": blueprint.setClassification(getEnum(SkillName.class, node, fieldName)); break;
                    case "cost": blueprint.setCost(getInteger(node, fieldName)); break;
                    case "effects": EffectFieldProcessor.processField(node.get(fieldName), blueprint); break;
                    case "facility-type": blueprint.setFacilityType(getEnum(FacilityType.class, node, fieldName)); break;
                    case "gametext": {
                        // assumes this is just ship special equipment
                        Collection<ShipSpecialEquipment> specialEquipment =
                                getEnumSetFromCommaDelimited(node, fieldName, ShipSpecialEquipment.class);
                        blueprint.addSpecialEquipment(specialEquipment);
                        break;
                    }
                    case "icons": blueprint.setIcons(getCardIconListFromCommaDelimited(node, fieldName)); break;
                    case "image-options": setImageOptions(blueprint, node); break;
                    case "image-url": blueprint.setImageUrl(BlueprintUtils.getString(node, fieldName)); break;
                    case "location": blueprint.setLocation(BlueprintUtils.getString(node, fieldName)); break;
                    case "lore": blueprint.setLore(BlueprintUtils.getString(node, fieldName)); break;
                    case "mission-requirements":
                        String missionRequirementsText = BlueprintUtils.getString(node, fieldName);
                        blueprint.setMissionRequirementsText(missionRequirementsText);
                        blueprint.setMissionRequirements(createRequirement(missionRequirementsText));
                        break;
                    case "mission-type": blueprint.setMissionType(getEnum(MissionType.class, node, fieldName)); break;
                    case "persona": blueprint.setPersona(BlueprintUtils.getString(node, fieldName)); break;
                    case "point-box":
                        blueprint.setPointsShown(getPointsShown(node.get(fieldName)));
                        blueprint.setHasPointBox(true);
                        break;
                    case "property-logo": blueprint.setPropertyLogo(getEnum(PropertyLogo.class, node, fieldName)); break;
                    case "quadrant": blueprint.setQuadrant(getEnum(Quadrant.class, node, fieldName)); break;
                    case "rarity": blueprint.setRarity(BlueprintUtils.getString(node, fieldName)); break;
                    case "region": blueprint.setRegion(getEnum(Region.class, node, fieldName)); break;
                    case "ship-class": {
                        String classString = node.get(fieldName).textValue().toUpperCase(Locale.ROOT)
                                .replace(" CLASS","");
                        blueprint.setShipClass(getEnum(ShipClass.class, classString, fieldName));
                        break;
                    }
                    case "skill-box": processSkillBox(blueprint, node, fieldName); break;
                    case "span": blueprint.setSpan(getInteger(node, fieldName)); break;
                    case "species": blueprint.setSpecies(getEnum(Species.class, node, fieldName)); break;
                    case "staffing": blueprint.setStaffing(getCardIconListFromCommaDelimited(node, fieldName)); break;
                    case "subtitle": blueprint.setSubtitle(BlueprintUtils.getString(node, fieldName)); break;
                    case "title": blueprint.setTitle(BlueprintUtils.getString(node, fieldName)); break;
                    case "tribble-power": processTribblePower(blueprint, node, fieldName); break;
                    case "tribble-value": blueprint.setTribbleValue(getInteger(node, fieldName)); break;
                    case "type": blueprint.setCardType(getEnum(CardType.class, node, fieldName)); break;
                    case "uniqueness": blueprint.setUniqueness(getEnum(Uniqueness.class, node, fieldName)); break;
                    default: throw new InvalidCardDefinitionException("Invalid field " + fieldName + " in blueprint");
                }
            }

            fillInBlueprintBlanks(blueprint);
            validateConsistency(blueprint);
            return blueprint;

        } catch(InvalidCardDefinitionException exp) {
            throw new IOException(exp.getMessage(), exp);
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

            /* orNoParens splits requirements joined by "or", if they're not inside parentheses.
                Examples:
                    match -> Diplomacy x2 OR OFFICER + ENGINEER
                    match -> Honor or Treachery
                    no match -> Diplomacy + Anthropology + (Jean-Luc Picard OR Worf OR CUNNING >35) */
        String orNoParens = "\\s+(?i)OR\\s+(?![^(]*\\))";

            /* andNoParens splits requirements joined by "and", if they're not inside parentheses.
                Examples:
                    match -> ENGINEER + Physics + Computer Skill
                    match -> Exobiology, Medical, and Cunning>35
                    no match -> (Leadership and Cunning>35) or (Transporters and Integrity>35)  */
        String andNoParens = "(\\s\\+\\s+|,\\sand\\s+|,\\s+|\\sand\\s)(?![^(]*\\))";

            // attributeSplit splits attribute requirements by < or > operators
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
                return new RegularSkillMissionRequirement(_skillMap.get(stringSplit[0].toUpperCase()),
                        Integer.parseInt(stringSplit[1].substring(1)));
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
                return new AttributeMissionRequirement(
                        getEnum(CardAttribute.class, stringSplit[0].trim(), "card attribute"),
                        Integer.parseInt(stringSplit[1].substring(1))
                );
            else throw new InvalidCardDefinitionException("Unable to process attribute mission requirement");
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
        if (value == null) return 0;
        String str = (value.isTextual()) ? value.textValue() : value.toString();
        return (str.isEmpty()) ? 0 : Integer.parseInt(str.replaceAll("[^\\d]", ""));
    }

    private void processSkillBox(CardBlueprint blueprint, JsonNode node, String fieldName)
            throws InvalidCardDefinitionException {
        SkillBox skillBox = new SkillBox(node.get(fieldName));
        blueprint.setSkillDotIcons(skillBox.getSkillDots());
        blueprint.setSpecialDownloadIcons(skillBox.getSdIcons());
        for (Skill skill : skillBox.getSkillList()) {
            blueprint.addSkill(skill);
        }
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
            return new SpecialDownloadSkill(skillSplit[1].trim()); // TODO - Identify what is being allowed to download
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

    private void processTribblePower(CardBlueprint blueprint, JsonNode node, String fieldName)
            throws InvalidCardDefinitionException {
        TribblePower tribblePower = getEnum(TribblePower.class, node, fieldName);
        if (tribblePower == null)
            throw new InvalidCardDefinitionException("Unable to identify tribble power");
        blueprint.setTribblePower(tribblePower);
        if (tribblePower.isActive()) {
            StringBuilder jsonString = new StringBuilder();
            jsonString.append("{\"effect\":{\"type\":\"activateTribblePower\"}");
            if (tribblePower == TribblePower.AVALANCHE) {
                jsonString.append(",\"requires\":{\"type\":\"cardsInHandMoreThan\",\"player\":\"you\",\"count\":3}");
            }
            jsonString.append(
                    ",\"optional\":true,\"trigger\":{\"filter\":\"self\",\"type\":\"played\"},\"type\":\"trigger\"}");

            try {
                EffectFieldProcessor.appendActionSource(new ObjectMapper().readTree(jsonString.toString()), blueprint);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isNot2E(CardBlueprint blueprint) {
        return !blueprint.getBlueprintId().startsWith("1_");
    }

    private void validateConsistency(CardBlueprint blueprint) throws InvalidCardDefinitionException {
        if (blueprint.getTitle() == null)
            throw new InvalidCardDefinitionException("Card has to have a title");
        if (blueprint.getCardType() == null)
            throw new InvalidCardDefinitionException("Card has to have a type");
        if (blueprint.getUniqueness() == null)
            throw new InvalidCardDefinitionException("Card has to have a uniqueness");
        if (blueprint.getRarity() == null)
            throw new InvalidCardDefinitionException("Card has to have a rarity");

        if (blueprint.getCardType() == CardType.MISSION) {
            if (blueprint.getPropertyLogo() != null)
                throw new InvalidCardDefinitionException("Mission card should not have a property logo");
            if (blueprint.getLocation() == null && !blueprint.getTitle().equals("Space") && isNot2E(blueprint))
                throw new InvalidCardDefinitionException("Mission card should have a location");
            if (blueprint.getQuadrant() == null)
                throw new InvalidCardDefinitionException("Mission card should have a quadrant");
        } else if (blueprint.getCardType() == CardType.TRIBBLE) {
            if (blueprint.getTribblePower() == null)
                throw new InvalidCardDefinitionException("Tribble card has to have a Tribble power");
            if (!Arrays.asList(1, 10, 100, 1000, 10000, 100000).contains(blueprint.getTribbleValue()))
                throw new InvalidCardDefinitionException("Tribble card does not have a valid Tribble value");
        } else if (blueprint.getPropertyLogo() == null && isNot2E(blueprint))
                // Technically tribbles have property logos too, they're just never relevant
            throw new InvalidCardDefinitionException("Non-mission card has to have a property logo");
    }
    
    private void fillInBlueprintBlanks(CardBlueprint blueprint) {
        // Apply uniqueness if the card doesn't specify it
        List<CardType> implicitlyUniqueTypes = Arrays.asList(CardType.PERSONNEL, CardType.SHIP, CardType.FACILITY,
                CardType.SITE, CardType.MISSION, CardType.TIME_LOCATION);
        if (blueprint.getUniqueness() == null) {
            if (implicitlyUniqueTypes.contains(blueprint.getCardType())) {
                blueprint.setUniqueness(Uniqueness.UNIQUE);
            } else {
                blueprint.setUniqueness(Uniqueness.UNIVERSAL);
            }
        }

        // Set quadrant to alpha if the card doesn't specify it
        List<CardType> implicitlyAlphaQuadrant = Arrays.asList(CardType.PERSONNEL, CardType.SHIP, CardType.FACILITY,
                CardType.MISSION);
        if (blueprint.getQuadrant() == null && implicitlyAlphaQuadrant.contains(blueprint.getCardType()))
            blueprint.setQuadrant(Quadrant.ALPHA);

        // Set rarity to V if none was specified
        if (blueprint.getRarity() == null)
            blueprint.setRarity("V");
    }

    private CardBlueprint createBlueprint(JsonNode node) throws InvalidCardDefinitionException {
        final CardBlueprint blueprint;
        String blueprintId = BlueprintUtils.getString(node, "blueprintId");
        if (blueprintId == null)
            throw new InvalidCardDefinitionException("Null value for blueprintId");

        if (node.has("java-blueprint")) {
            try {
                blueprint = (CardBlueprint) Class.forName(
                                "com.gempukku.stccg.cards.blueprints.Blueprint" + blueprintId)
                        .getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                     InstantiationException | IllegalAccessException exception) {
                throw new InvalidCardDefinitionException("No valid Java class found for blueprint " + blueprintId);
            }
        } else {
            blueprint = new CardBlueprint(blueprintId);
        }

        if (!Objects.equals(blueprint.getBlueprintId(),blueprintId))
            throw new InvalidCardDefinitionException(
                    "Non-matching blueprintIds: '" + blueprintId + "' vs. '" + blueprint.getBlueprintId());
        return blueprint;
    }

}