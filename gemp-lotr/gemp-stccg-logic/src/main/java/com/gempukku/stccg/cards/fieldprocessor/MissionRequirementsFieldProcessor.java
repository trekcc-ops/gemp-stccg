package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.PersonnelName;
import com.gempukku.stccg.common.filterable.RegularSkill;
import com.gempukku.stccg.requirement.missionrequirements.*;

import java.util.*;

public class MissionRequirementsFieldProcessor implements FieldProcessor {
    final Map<String, RegularSkill> _skillMap = new HashMap<>();
    final Map<String, PersonnelName> _personnelNameMap = new HashMap<>();
    CardBlueprintFactory _environment;
    public MissionRequirementsFieldProcessor() {
        new ArrayList<>(Arrays.asList(RegularSkill.values()))
                .forEach(regularSkill -> _skillMap.put(regularSkill.get_humanReadable(), regularSkill));
        new ArrayList<>(Arrays.asList(PersonnelName.values()))
                .forEach(name -> _personnelNameMap.put(name.getHumanReadable(), name));
    }


    @Override
    public void processField(String key, Object value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        _environment = environment;
        final String requirements = environment.getString(value, key);
        blueprint.setMissionRequirementsText(requirements);
        blueprint.setMissionRequirements(createRequirement(requirements));
    }

    private MissionRequirement createRequirement(String text) throws InvalidCardDefinitionException {
        String orNoParens = "\\s+OR\\s+(?![^\\(]*\\))";
        String andNoParens = "\\s+\\+\\s+(?![^\\(]*\\))";
        String multiplierSplit = "(?=x\\d+)";
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
        if (text.split(multiplierSplit).length > 1) {
            String[] stringSplit = text.split(multiplierSplit);
            if (_skillMap.get(stringSplit[0].trim()) != null)
                return new RegularSkillMissionRequirement(
                        _skillMap.get(stringSplit[0].trim()), Integer.parseInt(stringSplit[1].substring(1)));
        }
        if (text.split(attributeSplit).length > 1) {
            String[] stringSplit = text.split(attributeSplit);
            if (stringSplit[1].charAt(0) == '>')
                return new AttributeMissionRequirement(_environment.getEnum(CardAttribute.class, stringSplit[0].trim()),
                        Integer.parseInt(stringSplit[1].substring(1)));
        }
        if (_skillMap.get(text) != null) {
            return new RegularSkillMissionRequirement(_skillMap.get(text));
        }
        if (_personnelNameMap.get(text) != null) {
            return new PersonnelNameMissionRequirement(_personnelNameMap.get(text));
        }
            // If none of these worked, throw an exception
        throw new InvalidCardDefinitionException("Mission requirements do not conform to expected syntax");
    }

}
