package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.SelectSkillAction;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import com.gempukku.stccg.actions.turn.RequiredTriggerAction;
import com.gempukku.stccg.cards.ActionSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;

import java.util.LinkedList;
import java.util.List;


public class Blueprint155_056 extends CardBlueprint {
    Blueprint155_056() {
        super("155_056"); // Data (The Next Generation)
        addSkill(SkillName.COMPUTER_SKILL);
        addSkill(SkillName.ASTROPHYSICS);
        addSkill(SkillName.ENGINEER);
        addSkill(specialSkill());
        setSkillDotIcons(4);
    }

    @Override
    public List<TopLevelSelectableAction> getRequiredAfterTriggerActions(ActionResult actionResult, PhysicalCard thisCard) {
            // TODO - Need some additional work here to be check skill for usability
        List<TopLevelSelectableAction> actions = new LinkedList<>();
        if (actionResult instanceof PlayCardResult playResult && playResult.getPlayedCard() == thisCard) {
            for (Skill skill : _skills)
                if (skill instanceof ActionSkill actionSkill)
                    actions.add(actionSkill.getAction(thisCard));
        }
        return actions;
    }

    private ActionSkill specialSkill() {
        return new ActionSkill("When reported, selects Anthropology, Physics, or Navigation.") {
            @Override
            public TopLevelSelectableAction getAction(final PhysicalCard thisCard) {
                final RequiredTriggerAction action = new RequiredTriggerAction(thisCard);
                List<SkillName> skillOptions = new LinkedList<>();
                skillOptions.add(SkillName.ANTHROPOLOGY);
                skillOptions.add(SkillName.PHYSICS);
                skillOptions.add(SkillName.NAVIGATION);
                    // TODO - This won't actually do anything at present other than the selection
                action.appendTargeting(new SelectSkillAction(thisCard.getOwner(), thisCard, skillOptions));
                return action;
            }
        };
    }
}