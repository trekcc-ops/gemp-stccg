package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.RegularSkill;

import java.util.*;

public class MissionRequirement {
    private RegularSkill _regularSkill;
    private String _text;
    public MissionRequirement(String text) {
        _text = text;
        RegularSkill regularSkill = Enum.valueOf(
                RegularSkill.class, text.toUpperCase().replaceAll("[ '\\-]","_"));
/*        if (regularSkill == null)


        List<RegularSkill> skillList = new ArrayList<>(Arrays.asList(RegularSkill.values()));
        Map<String, RegularSkill> skillMap = new HashMap<>();
        skillList.forEach(regularSkill -> skillMap.put(regularSkill.get_humanReadable(), regularSkill));
        RegularSkill regularSkill = skillMap.get(text);
        if (regularSkill != null) {
            return awayTeam.hasSkill(regularSkill); // TODO - Doesn't count multipliers
        } else {
            return false;
        } */
    }

    public String getText() { return _text; }
//    boolean accepts(ActionContext actionContext);
}