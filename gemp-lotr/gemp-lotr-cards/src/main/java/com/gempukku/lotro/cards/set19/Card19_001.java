package com.gempukku.lotro.cards.set19;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.effects.*;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.*;

import java.util.Collections;
import java.util.List;

/**
 * Set: Ages End
 * Type: The One Ring
 * Game Text: While wearing The One Ring, each time the bearer is about to take a wound, add a burden instead.
 * Maneuver: Wear The One Ring until the regroup phase. Skirmish: Add a burden to make the Ring-bearer strength +3.
 */
public class Card19_001 extends AbstractAttachable {
    public Card19_001() {
        super(null, CardType.THE_ONE_RING, 0, null, null, "The One Ring", "The Great Ring", true);
    }

    @Override
    public Filter getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.none;
    }

    @Override
    public List<? extends Action> getPhaseActionsInPlay(String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.MANEUVER, self)
                && !game.getGameState().isWearingRing()
                && !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.RING_TEXT_INACTIVE)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendEffect(
                    new PutOnTheOneRingEffect());
            return Collections.singletonList(action);
        }
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)
                && !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.RING_TEXT_INACTIVE)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new AddBurdenEffect(self.getOwner(), self, 1));
            action.appendEffect(
                    new AddUntilEndOfPhaseModifierEffect(
                            new StrengthModifier(self, self.getAttachedTo(), 3)));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredBeforeTriggers(LotroGame game, Effect effect, PhysicalCard self) {
        if (TriggerConditions.isGettingWounded(effect, game, Filters.hasAttached(self))
                && game.getGameState().isWearingRing()
                && !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.RING_TEXT_INACTIVE)) {
            WoundCharactersEffect woundEffect = (WoundCharactersEffect) effect;
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(new NegateWoundEffect(woundEffect, self.getAttachedTo()));
            action.appendEffect(new AddBurdenEffect(self.getOwner(), self, 1));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if ((TriggerConditions.startOfPhase(game, effectResult, Phase.REGROUP) || TriggerConditions.endOfPhase(game, effectResult, Phase.REGROUP))
                && game.getGameState().isWearingRing()) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(new TakeOffTheOneRingEffect());
            return Collections.singletonList(action);
        }
        return null;
    }
}
