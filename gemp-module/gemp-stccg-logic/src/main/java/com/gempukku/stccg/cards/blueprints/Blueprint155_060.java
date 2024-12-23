package com.gempukku.stccg.cards.blueprints;


import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Blueprint155_060 extends CardBlueprint {

    // Geordi La Forge (The Next Generation)

    Blueprint155_060() {
        super("155_060");
        setSkillDotIcons(2);
    }

    @Override
    public List<Skill> getSkills(DefaultGame game, PhysicalCard thisCard) {
        Collection<SkillName> skillNames = new LinkedList<>();
        if (thisCard.isAtSpaceLocation()) {
            skillNames.add(SkillName.NAVIGATION);
            skillNames.add(SkillName.ASTROPHYSICS);
            skillNames.add(SkillName.STELLAR_CARTOGRAPHY);
        }
        if (thisCard.isAtPlanetLocation()) {
            skillNames.add(SkillName.ENGINEER);
            skillNames.add(SkillName.PHYSICS);
            skillNames.add(SkillName.COMPUTER_SKILL);
        }

        List<Skill> result = new LinkedList<>();

        for (SkillName skillName : skillNames) {
            result.add(new RegularSkill(skillName));
        }

        return result;
    }

}