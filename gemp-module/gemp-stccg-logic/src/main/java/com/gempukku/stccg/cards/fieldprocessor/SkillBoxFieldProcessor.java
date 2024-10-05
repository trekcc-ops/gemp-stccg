package com.gempukku.stccg.cards.fieldprocessor;

import com.fasterxml.jackson.databind.JsonNode;
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
    public void processField(String key, JsonNode value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        String skills = value.textValue();
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
        String skillName;
        int skillLevel;
        Map<String, SkillName> skillMap = new HashMap<>();
        new ArrayList<>(Arrays.asList(SkillName.values()))
                .forEach(regularSkill -> skillMap.put(regularSkill.get_humanReadable().toUpperCase(), regularSkill));
        String iconSplit = "(?<=\\[\\*])|(?<=\\[DL])";
        String[] skillSplit = string.split(iconSplit);

        if (skillSplit[0].trim().equals("[*]")) {
            String skill = skillSplit[1].trim();
            String multiplierSplit = "(?=x\\d+)";
            if (skill.split(multiplierSplit).length > 1) {
                // 1E-style multiplier (like Leadership x2)
                String[] stringSplit = skill.split(multiplierSplit);
                skillName = stringSplit[0].trim();
                skillLevel = Integer.parseInt(stringSplit[1].substring(1).trim());
            } else if (skill.substring(0,1).matches("\\d") && skill.charAt(1) == ' ') {
                // 2E-style multiplier (like 2 Leadership)
                skillName = skill.substring(2);
                skillLevel = Integer.parseInt(skill.substring(0,1));
            } else {
                // No multiplier
                skillName = skill;
                skillLevel = 1;
            }
            if (skillMap.get(skillName.toUpperCase()) != null)
                return new RegularSkill(skillMap.get(skillName.toUpperCase()), skillLevel);
            else
                // TODO - Handler for special skills?
                throw new InvalidCardDefinitionException("Skill " + skillName + " doesn't match known skills");

        } else if (skillSplit[0].trim().equals("[DL]")) {
            return new SpecialDownloadSkill(skillSplit[1].trim());
        } else {
            throw new InvalidCardDefinitionException("Invalid skill syntax in JSON file");
        }
    }
}
