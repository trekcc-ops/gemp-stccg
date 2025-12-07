package com.gempukku.stccg.game;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.modifiers.*;
import com.gempukku.stccg.modifiers.attributes.AttributeModifier;
import com.gempukku.stccg.requirement.Condition;

import java.util.*;

public interface ModifiersQuerying {

    default boolean hasIcon(PhysicalCard physicalCard, CardIcon icon) {
        // TODO - Not accurate if the card has LOST an icon
        // TODO - Does not account for modifiers that are inactive
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
            if (modifier.isConditionFulfilled(getGame()) && affectsCardWithSkipSet(card, modifier) &&
                    !modifier.foundNoCumulativeConflict(result)) {
                result.add(modifier);
            }
        }
        return result;
    }

    private List<Modifier> getModifiersInEffect(ModifierEffect modifierEffect) {
        List<Modifier> modifiers = getAllModifiersByEffect(modifierEffect);
        if (modifiers == null)
            return Collections.emptyList();
        else {
            LinkedList<Modifier> liveModifiers = new LinkedList<>();
            for (Modifier modifier : modifiers) {
                if (!modifierIsInSkipSet(modifier)) {
                    addToSkipSet(modifier);
                    if (modifier.isConditionFulfilled(getGame()))
                        if (shouldAdd(modifierEffect, modifier)) {
                            liveModifiers.add(modifier);
                        }
                    removeFromSkipSet(modifier);
                }
            }
            return liveModifiers;
        }
    }

    default List<Modifier> getIconModifiersAffectingCard(CardIcon icon, PhysicalCard card) {
        List<Modifier> modifiers = getAllModifiersByEffect(ModifierEffect.GAIN_ICON_MODIFIER);
        LinkedList<Modifier> liveModifiers = new LinkedList<>();
        if (icon == null || card == null) {
            return liveModifiers;
        }
        for (Modifier modifier : modifiers) {
            if (modifier instanceof IconAffectingModifier iconModifier && iconModifier.getIcon() == icon) {
                if (!modifierIsInSkipSet(modifier)) {
                    addToSkipSet(modifier);
                    if (modifier.isConditionFulfilled(getGame()))
                        if (shouldAdd(ModifierEffect.GAIN_ICON_MODIFIER, modifier)) {
                            if (modifier.affectsCard(getGame(), card) && modifier.foundNoCumulativeConflict(liveModifiers)) {
                                liveModifiers.add(modifier);
                            }
                        }
                    removeFromSkipSet(modifier);
                }
            }
        }
        return liveModifiers;
    }

    private List<Modifier> getSkillModifiersAffectingCard(SkillName skill, PhysicalCard card) {
        List<Modifier> modifiers = getAllModifiersByEffect(ModifierEffect.GAIN_SKILL_MODIFIER);
        LinkedList<Modifier> liveModifiers = new LinkedList<>();
        if (skill == null || card == null) {
            return liveModifiers;
        }
        for (Modifier modifier : modifiers) {
            if (modifier instanceof SkillAffectingModifier skillModifier && skillModifier.getSkills().contains(skill)) {
                if (!modifierIsInSkipSet(modifier)) {
                    addToSkipSet(modifier);
                    if (modifier.isConditionFulfilled(getGame()))
                        if (shouldAdd(ModifierEffect.GAIN_SKILL_MODIFIER, modifier)) {
                            if (modifier.affectsCard(getGame(), card) && modifier.foundNoCumulativeConflict(liveModifiers))
                                liveModifiers.add(modifier);
                        }
                    removeFromSkipSet(modifier);
                }
            }
        }
        return liveModifiers;
    }



    default List<Modifier> getModifiersAffectingCardByEffect(ModifierEffect modifierEffect, PhysicalCard card) {
        List<Modifier> modifiers = getAllModifiersByEffect(modifierEffect);
        LinkedList<Modifier> liveModifiers = new LinkedList<>();
        for (Modifier modifier : modifiers) {
            if (!modifierIsInSkipSet(modifier)) {
                addToSkipSet(modifier);
                Condition condition = modifier.getCondition();
                if (condition == null || condition.isFulfilled(getGame()))
                    if (shouldAdd(modifierEffect, modifier)) {
                        if ((card == null || modifier.affectsCard(getGame(), card)) &&
                                (modifier.foundNoCumulativeConflict(liveModifiers)))
                            liveModifiers.add(modifier);
                    }
                removeFromSkipSet(modifier);
            }
        }
        return liveModifiers;
    }

    default float getAttribute(PhysicalCard card, CardAttribute attribute) {
        float result = switch(attribute) {
            case INTEGRITY -> card.getBlueprint().getIntegrity();
            case CUNNING -> card.getBlueprint().getCunning();
            case STRENGTH -> card.getBlueprint().getStrength();
            case RANGE -> card.getBlueprint().getRange();
            case WEAPONS -> card.getBlueprint().getWeapons();
            case SHIELDS -> card.getBlueprint().getShields();
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

    default boolean canPerformAction(String performingPlayer, Action action) {
        for (Modifier modifier : getModifiersInEffect(ModifierEffect.ACTION_MODIFIER))
            if (!modifier.canPerformAction(getGame(), performingPlayer, action)) {
                return false;
            }
        return true;
    }


    default boolean affectsCardWithSkipSet(PhysicalCard physicalCard, Modifier modifier) {
        if (!modifierIsInSkipSet(modifier) && physicalCard != null) {
            addToSkipSet(modifier);
            boolean result = modifier.affectsCard(getGame(), physicalCard);
            removeFromSkipSet(modifier);
            return result;
        } else {
            return false;
        }
    }

    private boolean shouldAdd(ModifierEffect modifierEffect, Modifier modifier) {
        return modifierEffect == ModifierEffect.TEXT_MODIFIER || modifier.getSource() == null ||
                modifier.isNonCardTextModifier() ||
                !modifier.getSource().hasTextRemoved(getGame());
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