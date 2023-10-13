package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.results.EffectResult;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.modifiers.ExtraPlayCost;
import com.gempukku.stccg.modifiers.Modifier;

import java.util.List;
import java.util.Set;

public interface CardBlueprint {
    Quadrant getQuadrant();

    String getLocation();
    Region getRegion();

    enum Direction {
        LEFT, RIGHT
    }

    Side getSide();

    CardType getCardType();

    Culture getCulture();

    Race getRace();

    Uniqueness getUniqueness();

    boolean isUnique();

    boolean isUniversal();

    String getTitle();
    String getImageUrl();

    String getSubtitle();

    boolean hasKeyword(Keyword keyword);

    int getKeywordCount(Keyword keyword);

    Filterable getValidTargetFilter(String playerId, DefaultGame game, PhysicalCard self);

    int getTwilightCost();

    int getTwilightCostModifier(DefaultGame game, PhysicalCard self, PhysicalCard target);

    int getStrength();

    int getVitality();

    int getResistance();

    int getTribbleValue();
    TribblePower getTribblePower();

    PlayEventAction getPlayEventCardAction(String playerId, DefaultGame game, PhysicalCard self);

    List<? extends Modifier> getInPlayModifiers(DefaultGame game, PhysicalCard self);

    List<? extends Modifier> getStackedOnModifiers(DefaultGame game, PhysicalCard self);

    List<? extends Modifier> getInDiscardModifiers(DefaultGame game, PhysicalCard self);

    boolean playRequirementsNotMet(DefaultGame game, PhysicalCard self);

    List<? extends Action> getPhaseActionsInHand(String playerId, DefaultGame game, PhysicalCard self);

    List<? extends Action> getPhaseActionsFromDiscard(String playerId, DefaultGame game, PhysicalCard self);

    List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, DefaultGame game, PhysicalCard self);

    List<? extends ActivateCardAction> getPhaseActionsFromStacked(String playerId, DefaultGame game, PhysicalCard self);

    List<RequiredTriggerAction> getRequiredBeforeTriggers(DefaultGame game, Effect effect, PhysicalCard self);

    List<RequiredTriggerAction> getRequiredAfterTriggers(DefaultGame game, EffectResult effectResult, PhysicalCard self);


    List<OptionalTriggerAction> getOptionalBeforeTriggers(String playerId, DefaultGame game, Effect effect, PhysicalCard self);

    List<ActionSource> getOptionalAfterTriggers();

    List<? extends ActivateCardAction> getOptionalInPlayBeforeActions(String playerId, DefaultGame game, Effect effect, PhysicalCard self);

    List<? extends ActivateCardAction> getOptionalInPlayAfterActions(String playerId, DefaultGame game, EffectResult effectResult, PhysicalCard self);


    List<PlayEventAction> getPlayResponseEventAfterActions(String playerId, DefaultGame game, EffectResult effectResult, PhysicalCard self);

    List<PlayEventAction> getPlayResponseEventBeforeActions(String playerId, DefaultGame game, Effect effect, PhysicalCard self);


    List<OptionalTriggerAction> getOptionalInHandAfterTriggers(String playerId, DefaultGame game, EffectResult effectResult, PhysicalCard self);


    RequiredTriggerAction getDiscardedFromPlayRequiredTrigger(DefaultGame game, PhysicalCard self);

    OptionalTriggerAction getDiscardedFromPlayOptionalTrigger(String playerId, DefaultGame game, PhysicalCard self);


    RequiredTriggerAction getKilledRequiredTrigger(DefaultGame game, PhysicalCard self);

    OptionalTriggerAction getKilledOptionalTrigger(String playerId, DefaultGame game, PhysicalCard self);

    Set<PossessionClass> getPossessionClasses();

    String getDisplayableInformation(PhysicalCard self);

    List<? extends ExtraPlayCost> getExtraCostToPlay(DefaultGame game, PhysicalCard self);

    int getPotentialDiscount(DefaultGame game, String playerId, PhysicalCard self);

    void appendPotentialDiscountEffects(DefaultGame game, CostToEffectAction action, String playerId, PhysicalCard self);

    List<FilterableSource> getCopiedFilters();

    boolean canPlayOutOfSequence(TribblesGame game, PhysicalCard self);
    boolean canInsertIntoSpaceline();
}
