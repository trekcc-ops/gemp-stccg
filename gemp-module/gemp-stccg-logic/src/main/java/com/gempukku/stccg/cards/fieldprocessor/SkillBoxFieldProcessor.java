package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.common.filterable.SkillName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SkillBoxFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        String skills = environment.getString(value, key);
//        String[] splitString = skills.split("\\[\\*]");
        String[] splitString = skills.split("(?=\\[\\*])|(?=\\[DL])");
        int skillDots = 0;
        int sdIcons = 0;
        for (String string : splitString) {
            if (string.trim().startsWith("[*]"))
                skillDots++;
            if (string.trim().startsWith("[DL]"))
                sdIcons++;
        }
        blueprint.setSkillDotIcons(skillDots);
        blueprint.setSpecialDownloadIcons(sdIcons);

        for (String string : splitString) {
            blueprint.addSkill(getSkill(string));
        }
    }

    public static Skill getSkill(String string) throws InvalidCardDefinitionException {
        Map<String, SkillName> skillMap = new HashMap<>();
        new ArrayList<>(Arrays.asList(SkillName.values()))
                .forEach(regularSkill -> skillMap.put(regularSkill.get_humanReadable(), regularSkill));
        String iconSplit = "(?<=\\[\\*])|(?<=\\[DL])";
        String[] skillSplit = string.split(iconSplit);

        if (skillSplit[0].trim().equals("[*]")) {
            String skill = skillSplit[1].trim();
            String multiplierSplit = "(?=x\\d+)";
            if (skill.split(multiplierSplit).length > 1) {
                String[] stringSplit = skill.split(multiplierSplit);
                if (skillMap.get(stringSplit[0].trim()) != null) {
                    return new RegularSkill(
                            skillMap.get(stringSplit[0].trim()),
                            Integer.parseInt(stringSplit[1].substring(1).trim())
                    );
                } else {
                    throw new InvalidCardDefinitionException("Skill doesn't match known skills");
                }
            } else {
                return new RegularSkill(skillMap.get(skill), 1);
            }
        } else if (skillSplit[0].trim().equals("[DL]")) {
            return new SpecialDownloadSkill();
        } else {
            throw new InvalidCardDefinitionException("Invalid skill syntax in JSON file");
        }
    }
}
