package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.SelectSkillAction;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import com.gempukku.stccg.actions.turn.RequiredTriggerAction;
import com.gempukku.stccg.cards.ActionSkill;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Blueprint155_056 extends CardBlueprint {

    // Data (The Next Generation)
    Blueprint155_056() {
        List<Skill> skillList = new ArrayList<>();
        skillList.add(new RegularSkill(SkillName.COMPUTER_SKILL));
        skillList.add(new RegularSkill(SkillName.ASTROPHYSICS));
        skillList.add(new RegularSkill(SkillName.ENGINEER));
        skillList.add(specialSkill());
        _skillBox = new SkillBox(4,0,skillList);
    }

    @Override
    public List<TopLevelSelectableAction> getRequiredAfterTriggerActions(DefaultGame cardGame,
                                                                         ActionResult actionResult,
                                                                         PhysicalCard thisCard) {
            // TODO - Need some additional work here to be check skill for usability
        List<TopLevelSelectableAction> actions = new LinkedList<>();
        if (actionResult instanceof PlayCardResult playResult && playResult.getPlayedCard() == thisCard) {
            for (Skill skill : getSkills(cardGame, thisCard))
                if (skill instanceof ActionSkill actionSkill)
                    actions.add(actionSkill.getAction(cardGame, thisCard));
        }
        return actions;
    }

    private ActionSkill specialSkill() {
        return new ActionSkill("When reported, selects Anthropology, Physics, or Navigation.") {
            @Override
            public TopLevelSelectableAction getAction(DefaultGame cardGame, PhysicalCard thisCard) {
                final RequiredTriggerAction action = new RequiredTriggerAction(cardGame, thisCard);
                List<SkillName> skillOptions = new LinkedList<>();
                skillOptions.add(SkillName.ANTHROPOLOGY);
                skillOptions.add(SkillName.PHYSICS);
                skillOptions.add(SkillName.NAVIGATION);
                    // TODO - This won't actually do anything at present other than the selection
                action.appendTargeting(new SelectSkillAction(cardGame, thisCard.getOwnerName(), skillOptions));
                return action;
            }
        };
    }
}