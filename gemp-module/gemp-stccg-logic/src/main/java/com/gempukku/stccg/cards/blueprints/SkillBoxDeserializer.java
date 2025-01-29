package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.SpecialDownloadSkill;
import com.gempukku.stccg.common.filterable.*;

import java.io.IOException;
import java.util.*;

public class SkillBoxDeserializer extends StdDeserializer<SkillBox> {

    final String multiplierSplit1e = "(?=x\\d+)";
    final String multiplierSplit2e = "(?<=\\d).*(?=\\s\\w)";
    final Map<String, SkillName> _skillMap = new HashMap<>();


    public SkillBoxDeserializer() {
        this(null);
    }

    public SkillBoxDeserializer(Class<?> vc) {
        super(vc);
        new ArrayList<>(Arrays.asList(SkillName.values()))
                .forEach(regularSkill -> _skillMap.put(regularSkill.get_humanReadable().toUpperCase(), regularSkill));
    }

    @Override
    public SkillBox deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        String[] skillArray = node.textValue().split("(?=\\[\\*])|(?=\\[DL])");
        List<Skill> skillList = new LinkedList<>();
        int skillDots = 0;
        int sdIcons = 0;
        for (String string : skillArray) {
            if (string.trim().startsWith("[*]"))
                skillDots++;
            if (string.trim().startsWith("[DL]"))
                sdIcons++;
        }
        for (String string : skillArray) {
            skillList.add(getSkill(string));
        }
        return new SkillBox(skillDots, sdIcons, skillList);
    }

    public Skill getSkill(String string) throws IOException {
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
                throw new IOException("Skill " + skillName + " doesn't match known skills");

        } else if (skillSplit[0].trim().equals("[DL]")) {
            return new SpecialDownloadSkill(skillSplit[1].trim()); // TODO - Identify what is being allowed to download
        } else {
            throw new IOException("Invalid skill syntax in JSON file");
        }
    }


}