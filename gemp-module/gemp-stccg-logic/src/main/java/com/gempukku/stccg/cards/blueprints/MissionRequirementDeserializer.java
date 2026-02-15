package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.PersonnelName;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.missionrequirements.*;

import java.io.IOException;
import java.util.*;

public class MissionRequirementDeserializer extends StdDeserializer<MissionRequirement> {

    final String multiplierSplit1e = "(?=x\\d+)";
    final String multiplierSplit2e = "(?<=\\d).*(?=\\s\\w)";
    final Map<String, SkillName> _skillMap = new HashMap<>();
    final Map<String, PersonnelName> _personnelNameMap = new HashMap<>();


    public MissionRequirementDeserializer() {
        this(null);
    }

    public MissionRequirementDeserializer(Class<?> vc) {
        super(vc);
        new ArrayList<>(Arrays.asList(SkillName.values()))
                .forEach(regularSkill -> _skillMap.put(regularSkill.get_humanReadable().toUpperCase(), regularSkill));
        new ArrayList<>(Arrays.asList(PersonnelName.values()))
                .forEach(name -> _personnelNameMap.put(name.getHumanReadable(), name));
    }

    @Override
    public MissionRequirement deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        return createRequirement(node.textValue());
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
            if (_skillMap.get(stringSplit[0].toUpperCase()) != null) {
                return new RegularSkillMissionRequirement(_skillMap.get(stringSplit[0].toUpperCase()),
                        Integer.parseInt(stringSplit[1].substring(1)));
            }
            if (stringSplit[0].startsWith("personnelWith(") && stringSplit[0].endsWith(")")) {
                int numberOfPersonnelNeeded = Integer.parseInt(stringSplit[1].substring(1));
                String requirement = stringSplit[0].substring(14,stringSplit[0].length()-1);
                MissionRequirement personnelRequirement = createRequirement(requirement);
                return new FromOnePersonnelMissionRequirement(personnelRequirement, numberOfPersonnelNeeded);
            }
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
        String upperText = text.toUpperCase();
        if ((upperText.startsWith("INTEGRITY") || upperText.startsWith("CUNNING") || upperText.startsWith("STRENGTH") ||
                upperText.startsWith("RANGE") || upperText.startsWith("WEAPONS") || upperText.startsWith("SHIELDS")) &&
                text.split(attributeSplit).length > 1) {
            String[] stringSplit = text.split(attributeSplit);
            if (stringSplit[1].charAt(0) == '>') {
                String attributeName = stringSplit[0].trim().toUpperCase(Locale.ROOT);
                CardAttribute cardAttribute = CardAttribute.valueOf(attributeName);
                return new AttributeMissionRequirement(cardAttribute,
                        Integer.parseInt(stringSplit[1].substring(1))
                );
            }
            else throw new InvalidCardDefinitionException("Unable to process attribute mission requirement");
        }
        if (_skillMap.get(text.toUpperCase()) != null) {
            return new RegularSkillMissionRequirement(_skillMap.get(text.toUpperCase()));
        }
        if (_personnelNameMap.get(text) != null) {
            return new PersonnelNameMissionRequirement(_personnelNameMap.get(text));
        }
        if (text.startsWith("name(") && text.endsWith(")")) {
            String name = text.substring("name(".length(), text.length() - 1);
            return new PersonnelNameMissionRequirement(name);
        }
        if (text.startsWith("personnelWith(") && text.endsWith(")")) {
            String requirement = text.substring(14,text.length()-1);
            MissionRequirement personnelRequirement = createRequirement(requirement);
            return new FromOnePersonnelMissionRequirement(personnelRequirement);
        }
        // If none of these worked, throw an exception
        throw new InvalidCardDefinitionException("Unable to process mission requirement: " + text);
    }

}