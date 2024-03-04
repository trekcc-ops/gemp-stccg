package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.RegularSkill;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SkillBoxFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        // TODO - No support for special skills or SDs right now
        String skills = environment.getString(value, key);
        String[] splitString = skills.split("\\[\\*\\]");
        blueprint.setSkillDotIcons(StringUtils.countMatches(skills, "[*]"));

        for (String string : splitString) {
            blueprint.addSkill(getSkill(string));
        }
    }

    public static Skill getSkill(String string) throws InvalidCardDefinitionException {
        Map<String, RegularSkill> skillMap = new HashMap<>();
        new ArrayList<>(Arrays.asList(RegularSkill.values()))
                .forEach(regularSkill -> skillMap.put(regularSkill.get_humanReadable(), regularSkill));

        String multiplierSplit = "(?=x\\d+)";
        if (string.split(multiplierSplit).length > 1) {
            String[] stringSplit = string.split(multiplierSplit);
            if (skillMap.get(stringSplit[0].trim()) != null) {
                return new Skill(
                        skillMap.get(stringSplit[0].trim()),
                        Integer.parseInt(stringSplit[1].substring(1).trim())
                );
            } else {
                throw new InvalidCardDefinitionException("Skill doesn't match known skills");
            }
        } else {
            return new Skill(skillMap.get(string.trim()), 1);
        }
    }

}
