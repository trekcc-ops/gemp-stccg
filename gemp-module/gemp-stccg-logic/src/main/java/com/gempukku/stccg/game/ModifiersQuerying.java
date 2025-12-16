package com.gempukku.stccg.game;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.modifiers.*;
import com.gempukku.stccg.modifiers.attributes.AttributeModifier;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public interface ModifiersQuerying {

    default boolean hasIcon(PhysicalCard physicalCard, CardIcon icon) {
        if (physicalCard.getBlueprint().hasIcon(icon))
            return true;
        for (Modifier modifier : getIconModifiersAffectingCard(icon, physicalCard)) {
            if (modifier.hasIcon(physicalCard, icon)) {
                return true;
            }
        }
        return false;
    }

    default Collection<Modifier> getModifiersAffectingCard(PhysicalCard card) {
        Collection<Modifier> result = new HashSet<>();
        for (Modifier modifier : getAllModifiers()) {
            if (modifierIsAffectingCard(modifier, card, result)) {
                result.add(modifier);
            }
        }
        return result;
    }

    private List<Modifier> getModifiersInEffect(ModifierEffect modifierEffect) {
        List<Modifier> modifiers = getAllModifiersByEffect(modifierEffect);
        List<Modifier> result = new LinkedList<>();
        for (Modifier modifier : modifiers) {
            if (modifierIsInEffect(modifier)) {
                result.add(modifier);
            }
        }
        return result;
    }

    default List<Modifier> getIconModifiersAffectingCard(CardIcon icon, PhysicalCard card) {
        List<Modifier> modifiers = getAllModifiersByEffect(ModifierEffect.GAIN_ICON_MODIFIER);
        LinkedList<Modifier> liveModifiers = new LinkedList<>();
        for (Modifier modifier : modifiers) {
            if (modifier instanceof IconAffectingModifier iconModifier && iconModifier.getIcon() == icon) {
                if (modifierIsAffectingCard(modifier, card, liveModifiers)) {
                    liveModifiers.add(modifier);
                }
            }
        }
        return liveModifiers;
    }

    private List<Modifier> getSkillModifiersAffectingCard(SkillName skill, PhysicalCard card) {
        List<Modifier> modifiers = getAllModifiersByEffect(ModifierEffect.GAIN_SKILL_MODIFIER);
        LinkedList<Modifier> liveModifiers = new LinkedList<>();
        for (Modifier modifier : modifiers) {
            if (modifier instanceof SkillAffectingModifier skillModifier && skillModifier.getSkills().contains(skill)) {
                if (modifierIsAffectingCard(modifier, card, liveModifiers)) {
                    liveModifiers.add(modifier);
                }
            }
        }
        return liveModifiers;
    }


    private boolean modifierIsAffectingCard(Modifier modifier, PhysicalCard card,
                                            Collection<Modifier> modifiersThusFar) {
        if (!modifier.affectsCard(getGame(), card)) {
            return false;
        } else if (!modifier.foundNoCumulativeConflict(modifiersThusFar)) {
            return false;
        } else {
            return modifierIsInEffect(modifier);
        }
    }
    
    private boolean modifierIsInEffect(Modifier modifier) {
        boolean result;
        if (modifierIsInSkipSet(modifier)) {
            result = false;
        } else {
            addToSkipSet(modifier);
            if (!modifier.isConditionFulfilled(getGame())) {
                result = false;
            } else if (modifier.isEffectType(ModifierEffect.TEXT_MODIFIER)) {
                result = true;
            } else {
                result = !modifier.isSuspended(getGame());
            }
            removeFromSkipSet(modifier);
        }
        return result;
    }



    default List<Modifier> getModifiersAffectingCardByEffect(ModifierEffect modifierEffect, PhysicalCard card) {
        List<Modifier> modifiers = getAllModifiersByEffect(modifierEffect);
        LinkedList<Modifier> liveModifiers = new LinkedList<>();
        for (Modifier modifier : modifiers) {
            if (modifierIsAffectingCard(modifier, card, liveModifiers)) {
                liveModifiers.add(modifier);
            }
        }
        return liveModifiers;
    }

    default float getAttribute(PhysicalCard card, CardAttribute attribute) {
        CardBlueprint blueprint = card.getBlueprint();
        float result = switch(attribute) {
            case INTEGRITY -> blueprint.getIntegrity();
            case CUNNING -> blueprint.getCunning();
            case STRENGTH -> blueprint.getStrength();
            case RANGE -> blueprint.getRange();
            case WEAPONS -> blueprint.getWeapons();
            case SHIELDS -> blueprint.getShields();
        };

        if (attribute == CardAttribute.WEAPONS &&
                !getModifiersAffectingCardByEffect(ModifierEffect.WEAPONS_DISABLED_MODIFIER, card).isEmpty()) {
            return 0;
        }

        Collection<Modifier> attributeModifiers =
                getModifiersAffectingCardByEffect(ModifierEffect.ATTRIBUTE_MODIFIER, card);
        for (Modifier modifier : attributeModifiers) {
            if (modifier instanceof AttributeModifier attributeModifier &&
                    attributeModifier.getAttributesModified().contains(attribute)) {
                result += modifier.getAttributeModifier(getGame(), card);
            }
        }
        if (result < 0)
            return 0;
        else
            return result;
    }

    default boolean playerRestrictedFromPerformingActionDueToModifiers(String performingPlayer, Action action) {
        for (Modifier modifier : getModifiersInEffect(ModifierEffect.ACTION_MODIFIER))
            if (!modifier.canPerformAction(getGame(), performingPlayer, action)) {
                return true;
            }
        return false;
    }


    default boolean canDiscardCardsFromHand(String playerId, PhysicalCard source) {
        for (Modifier modifier : getModifiersAffectingCardByEffect(ModifierEffect.DISCARD_NOT_FROM_PLAY, source))
            if (!modifier.canDiscardCardsFromHand(getGame(), playerId, source))
                return false;
        return true;
    }

    default boolean hasFlagActive(ModifierFlag modifierFlag) {
        return getAllModifiersByEffect(ModifierEffect.SPECIAL_FLAG_MODIFIER).stream()
                .anyMatch(modifier -> modifier.hasFlagActive(modifierFlag));
    }

    default Integer getSkillLevel(PersonnelCard physicalCard, SkillName skillName) {
        int level = 0;
        for (Skill skill : physicalCard.getBlueprint().getSkills(getGame(), physicalCard)) {
            if (skill instanceof RegularSkill regularSkill) {
                if (regularSkill.getRegularSkill() == skillName) {
                    level += regularSkill.getLevel();
                }
            }
        }
        if (physicalCard.getBlueprint().getClassification() == skillName)
            level += 1;

        for (Modifier modifier : getSkillModifiersAffectingCard(skillName, physicalCard)) {
            if (modifier instanceof GainSkillModifier skillModifier && skillModifier.getSkills().contains(skillName))
                level += 1;
        }
        return level;
    }

    default boolean canPlayerSolveMission(String playerId, MissionLocation mission) {
        for (Modifier modifier : getModifiersInEffect(ModifierEffect.SOLVE_MISSION_MODIFIER)) {
            if (modifier instanceof PlayerCannotSolveMissionModifier missionModifier)
                if (missionModifier.cannotSolveMission(mission, playerId))
                    return false;
        }
        return true;
    }

    default boolean canNotPlayCard(String performingPlayer, PhysicalCard card) {
        for (Modifier modifier : getModifiersInEffect(ModifierEffect.ACTION_MODIFIER))
            if (modifier.cantPlayCard(getGame(), performingPlayer, card))
                return true;
        return false;
    }

    List<Modifier> getAllModifiersByEffect(ModifierEffect modifierEffect);
    List<Modifier> getAllModifiers();
    void addToSkipSet(Modifier modifier);
    void removeFromSkipSet(Modifier modifier);
    boolean modifierIsInSkipSet(Modifier modifier);
    DefaultGame getGame();

}