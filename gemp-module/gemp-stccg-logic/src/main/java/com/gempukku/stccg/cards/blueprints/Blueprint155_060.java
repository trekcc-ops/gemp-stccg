package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.RequiredTriggerAction;
import com.gempukku.stccg.actions.choose.SelectSkillEffect;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import com.gempukku.stccg.cards.ActionSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;

import java.util.LinkedList;
import java.util.List;


public class Blueprint155_060 extends CardBlueprint {
    Blueprint155_060() {
        super("155_060"); // Geordi La Forge (The Next Generation)
        setSkillDotIcons(2);
    }

    // TODO - Add modifier to gain skills depending on location type he is at
}