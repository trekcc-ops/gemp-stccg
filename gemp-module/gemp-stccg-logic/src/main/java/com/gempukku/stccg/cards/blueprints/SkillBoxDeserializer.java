package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.SpecialDownloadSkill;
import com.gempukku.stccg.common.filterable.SkillName;

import java.io.IOException;
import java.util.*;

public class SkillBoxDeserializer extends StdDeserializer<SkillBox> {

    private static final String MULTIPLIER_SPLIT_1E = "(?=x\\d+)";
    private static final String MULTIPLIER_SPLIT_2E = "(?<=\\d).*(?=\\s\\w)";
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
        if (node.isTextual()) {
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
        } else {
            int skillDots = (node.has("skill-dots")) ? node.get("skill-dots").asInt() : 0;
            int sdIcons = 0;
            List<Skill> skillList = new ArrayList<>();
            for (JsonNode skillNode : node.get("skills")) {
                Skill skillToAdd = jp.getCodec().treeToValue(skillNode, Skill.class);
                if (skillToAdd instanceof SpecialDownloadSkill) {
                    sdIcons++;
                }
                skillList.add(skillToAdd);
            }
            return new SkillBox(skillDots, sdIcons, skillList);
        }
    }

    public Skill getSkill(String string) throws IOException {
        String skillName;
        int skillLevel;
        String iconSplit = "(?<=\\[\\*])|(?<=\\[DL])";
        String[] skillSplit = string.split(iconSplit);

        if (skillSplit[0].trim().equals("[*]")) {
            String skill = skillSplit[1].trim();
            if (skill.split(MULTIPLIER_SPLIT_1E).length > 1) {
                // 1E-style multiplier (like Leadership x2)
                String[] stringSplit = skill.split(MULTIPLIER_SPLIT_1E);
                skillName = stringSplit[0].trim();
                skillLevel = Integer.parseInt(stringSplit[1].substring(1).trim());
            } else if (skill.split(MULTIPLIER_SPLIT_2E).length > 1) {
                // 2E-style multiplier (like 2 Leadership)
                String[] stringSplit = skill.split(MULTIPLIER_SPLIT_2E);
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