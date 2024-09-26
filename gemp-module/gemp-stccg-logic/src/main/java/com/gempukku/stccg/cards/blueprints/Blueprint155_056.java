package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.RequiredTriggerAction;
import com.gempukku.stccg.actions.choose.SelectSkillEffect;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import com.gempukku.stccg.cards.ActionSkill;
import com.gempukku.stccg.cards.ModifierSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Characteristic;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.PresentWithYourCardCondition;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.AllAttributeModifier;

import java.util.LinkedList;
import java.util.List;


public class Blueprint155_056 extends CardBlueprint {
    Blueprint155_056() {
        super("155_056");
        addSkill(SkillName.COMPUTER_SKILL);
        addSkill(SkillName.ASTROPHYSICS);
        addSkill(SkillName.ENGINEER);
        addSkill(specialSkill());
        setSkillDotIcons(4);
    }

    @Override
    public List<Action> getRequiredAfterTriggerActions(EffectResult effectResult, PhysicalCard thisCard) {
            // TODO - Need some additional work here to be check skill for usability
        List<Action> actions = new LinkedList<>();
        if (effectResult instanceof PlayCardResult playResult && playResult.getPlayedCard() == thisCard) {
            for (Skill skill : _skills)
                if (skill instanceof ActionSkill actionSkill)
                    actions.add(actionSkill.getAction(thisCard));
        }
        return actions;
    }

    private ActionSkill specialSkill() {
        return new ActionSkill("When reported, selects Anthropology, Physics, or Navigation.") {
            @Override
            public Action getAction(final PhysicalCard thisCard) {
                final RequiredTriggerAction action = new RequiredTriggerAction(thisCard);
                List<SkillName> skillOptions = new LinkedList<>();
                skillOptions.add(SkillName.ANTHROPOLOGY);
                skillOptions.add(SkillName.PHYSICS);
                skillOptions.add(SkillName.NAVIGATION);
                action.appendTargeting(
                        new SelectSkillEffect(thisCard.getGame(), thisCard.getOwner(), skillOptions) {

                            @Override
                            protected void skillChosen(SkillName skill) {
                                ((PersonnelCard) thisCard).addSkill(skill);
                            }
                        }
                );
                return action;
            }
        };
    }
}