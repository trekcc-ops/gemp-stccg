package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.attributes.AttributeModifier;
import com.gempukku.stccg.modifiers.attributes.WeaponsDisabledModifier;
import com.gempukku.stccg.requirement.Condition;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AddAffiliationIconToMissionModifier.class, name = "addAffiliationIconToMission"),
        @JsonSubTypes.Type(value = AttributeModifier.class, name = "attribute"),
            // TODO Do we need all the types of attribute modifiers??
        @JsonSubTypes.Type(value = GainIconModifier.class, name = "gainIcon"),
        @JsonSubTypes.Type(value = GainSkillModifier.class, name = "gainSkill"),
        @JsonSubTypes.Type(value = PlayerCannotSolveMissionModifier.class, name = "cannotSolveMission"),
        @JsonSubTypes.Type(value = PlayerCantPlayCardsModifier.class, name = "cannotPlayCards"),
        @JsonSubTypes.Type(value = WeaponsDisabledModifier.class, name = "weaponsDisabled"),
        @JsonSubTypes.Type(value = YouCanPlayAUIconCardsModifier.class, name = "canPlayAU"),
        @JsonSubTypes.Type(value = YouCanSeedAUIconCardsModifier.class, name = "canSeedAU")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface Modifier {
    PhysicalCard getSource();

    String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard);

    ModifierEffect getModifierType();

    Condition getCondition();
    boolean isCumulative();
    boolean isConditionFulfilled(DefaultGame cardGame);

    boolean affectsCard(DefaultGame cardGame, PhysicalCard physicalCard);

    boolean hasRemovedText(DefaultGame game, PhysicalCard physicalCard);

    float getAttributeModifier(DefaultGame cardGame, PhysicalCard physicalCard);

    boolean canPerformAction(DefaultGame game, String performingPlayer, Action action);

    boolean cantPlayCard(DefaultGame game, String performingPlayer, PhysicalCard card);

    void appendExtraCosts(DefaultGame game, Action action, PhysicalCard card);

    boolean canDiscardCardsFromHand(DefaultGame game, String playerId, PhysicalCard source);

    boolean hasFlagActive(ModifierFlag modifierFlag);

    boolean hasIcon(PhysicalCard card, CardIcon icon);

    boolean foundNoCumulativeConflict(Iterable<Modifier> modifierList);

    boolean isSuspended(DefaultGame cardGame);

    boolean isEffectType(ModifierEffect effectType);
}