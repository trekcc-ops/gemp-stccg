package com.gempukku.lotro.game;

import com.gempukku.lotro.cards.LotroCardBlueprint;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.modifiers.ModifierFlag;

import java.util.Collection;

public class PlayConditions {
    public static boolean canPayForShadowCard(DefaultGame game, PhysicalCard self, Filterable validTargetFilter, int withTwilightRemoved, int twilightModifier, boolean ignoreRoamingPenalty) {
        int minimumCost;
        if (validTargetFilter == null)
            minimumCost = game.getModifiersQuerying().getTwilightCost(game, self, null, twilightModifier, ignoreRoamingPenalty);
        else {
            minimumCost = 0;
            for (PhysicalCard potentialTarget : Filters.filterActive(game, validTargetFilter)) {
                minimumCost = Math.min(minimumCost, game.getModifiersQuerying().getTwilightCost(game, self, potentialTarget, twilightModifier, ignoreRoamingPenalty));
            }
        }

        return minimumCost <= game.getGameState().getTwilightPool() - withTwilightRemoved;
    }

    private static boolean containsPhase(Phase[] phases, Phase phase) {
        for (Phase phase1 : phases) {
            if (phase1 == phase)
                return true;
        }
        return false;
    }

    public static boolean isAhead(DefaultGame game) {
        String currentPlayer = game.getGameState().getCurrentPlayerId();
        int currentPosition = game.getGameState().getCurrentSiteNumber();
        for (String player : game.getGameState().getPlayerOrder().getAllPlayers()) {
            if (!player.equals(currentPlayer))
                if (game.getGameState().getPlayerPosition(player) >= currentPosition)
                    return false;
        }
        return true;
    }


    public static boolean canDiscardFromHand(DefaultGame game, String playerId, int count, Filterable... cardFilter) {
        return hasCardInHand(game, playerId, count, cardFilter);
    }

    public static boolean hasCardInHand(DefaultGame game, String playerId, int count, Filterable... cardFilter) {
        return Filters.filter(game.getGameState().getHand(playerId), game, cardFilter).size() >= count;
    }

    public static boolean canDiscardCardsFromHandToPlay(PhysicalCard source, DefaultGame game, String playerId, int count, Filterable... cardFilter) {
        return Filters.filter(game.getGameState().getHand(playerId), game, Filters.and(cardFilter, Filters.not(source))).size() >= count;
    }

    public static boolean canRemoveFromDiscard(PhysicalCard source, DefaultGame game, String playerId, int count, Filterable... cardFilters) {
        return hasCardInDiscard(game, playerId, count, cardFilters);
    }

    public static boolean hasCardInDiscard(DefaultGame game, String playerId, int count, Filterable... cardFilters) {
        return Filters.filter(game.getGameState().getDiscard(playerId), game, cardFilters).size() >= count;
    }

    public static boolean hasCardInPlayPile(TribblesGame game, String playerId, int count, Filterable... cardFilters) {
        return Filters.filter(game.getGameState().getPlayPile(playerId), game, cardFilters).size() >= count;
    }

    public static boolean canRemoveFromDiscardToPlay(PhysicalCard source, DefaultGame game, String playerId, int count, Filterable... cardFilter) {
        return Filters.filter(game.getGameState().getDiscard(playerId), game, Filters.and(cardFilter, Filters.not(source))).size() >= count;
    }

    public static boolean canPlayCardDuringPhase(DefaultGame game, Phase phase, PhysicalCard self) {
        return (phase == null || game.getGameState().getCurrentPhase() == phase)
                && self.getZone() == Zone.HAND
                && (!self.getBlueprint().isUnique() || Filters.countActive(game, Filters.name(self.getBlueprint().getTitle())) == 0);
    }

    public static boolean canPlayCardFromHandDuringPhase(DefaultGame game, Phase[] phases, PhysicalCard self) {
        return (phases == null || containsPhase(phases, game.getGameState().getCurrentPhase()))
                && self.getZone() == Zone.HAND
                && (!self.getBlueprint().isUnique() || Filters.countActive(game, Filters.name(self.getBlueprint().getTitle())) == 0);
    }

    public static boolean isPhase(DefaultGame game, Phase phase) {
        return (game.getGameState().getCurrentPhase() == phase);
    }

    public static boolean location(DefaultGame game, Filterable... filters) {
        return Filters.and(filters).accepts(game, game.getGameState().getCurrentSite());
    }

    public static boolean stackedOn(PhysicalCard card, DefaultGame game, Filterable... filters) {
        return Filters.and(filters).accepts(game, card.getStackedOn());
    }

    public static boolean checkUniqueness(DefaultGame game, PhysicalCard self, boolean ignoreCheckingDeadPile) {
        LotroCardBlueprint blueprint = self.getBlueprint();
        if (!blueprint.isUnique())
            return true;

        final int activeCount = Filters.countActive(game, Filters.name(blueprint.getTitle()));
        return activeCount == 0
                && (ignoreCheckingDeadPile || (Filters.filter(game.getGameState().getDeadPile(self.getOwner()), game, Filters.name(blueprint.getTitle())).size() == 0));
    }

    private static int getTotalCompanions(String playerId, DefaultGame game) {
        return Filters.countActive(game, CardType.COMPANION)
                + Filters.filter(game.getGameState().getDeadPile(playerId), game, CardType.COMPANION).size();
    }

    public static boolean checkRuleOfNine(DefaultGame game, PhysicalCard self) {
        if (self.getZone() == Zone.DEAD)
            return (getTotalCompanions(self.getOwner(), game) <= 9);
        else
            return (getTotalCompanions(self.getOwner(), game) < 9);
    }

    public static boolean canExert(final PhysicalCard source, final DefaultGame game, final int times, final int count, Filterable... filters) {
        final Filter filter = Filters.and(filters, Filters.character);
        return Filters.countActive(game, filter,
                (Filter) (game1, physicalCard) -> (game1.getModifiersQuerying().getVitality(game1, physicalCard) > times)
                        && game1.getModifiersQuerying().canBeExerted(game1, source, physicalCard)) >= count;
    }

    public static boolean canStackDeckTopCards(PhysicalCard source, DefaultGame game, String deckId, int cardCount, Filterable... onto) {
        return game.getGameState().getDrawDeck(deckId).size() >= cardCount
                && canSpot(game, onto);
    }

    public static boolean canDiscardFromStacked(PhysicalCard source, DefaultGame game, String playerId, final int cardCount, Filterable from, Filterable... stackedFilter) {
        return Filters.canSpot(game, Filters.and(from, Filters.hasStacked(cardCount, stackedFilter)));
    }

    public static boolean canSpot(DefaultGame game, Filterable... filters) {
        return canSpot(game, 1, filters);
    }

    public static boolean isActive(DefaultGame game, Filterable... filters) {
        return isActive(game, 1, filters);
    }

    public static boolean isActive(DefaultGame game, int count, Filterable... filters) {
        return Filters.countActive(game, filters) >= count;
    }

    public static boolean canSpot(DefaultGame game, int count, Filterable... filters) {
        return Filters.canSpot(game, count, filters);
    }

    public static boolean canPlayFromDeck(String playerId, DefaultGame game, Filterable... filters) {
        return !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK);
    }

    public static boolean canPlayFromHand(String playerId, DefaultGame game, Filterable... filters) {
        return Filters.filter(game.getGameState().getHand(playerId), game, Filters.and(filters, Filters.playable(game))).size() > 0;
    }

    public static boolean canPlayFromHand(String playerId, DefaultGame game, int twilightModifier, Filterable... filters) {
        return Filters.filter(game.getGameState().getHand(playerId), game, Filters.and(filters, Filters.playable(game, twilightModifier))).size() > 0;
    }

    public static boolean canPlayFromHand(String playerId, DefaultGame game, int twilightModifier, boolean ignoreRoamingPenalty, Filterable... filters) {
        return Filters.filter(game.getGameState().getHand(playerId), game, Filters.and(filters, Filters.playable(game, twilightModifier, ignoreRoamingPenalty))).size() > 0;
    }

    public static boolean canPlayFromHand(String playerId, DefaultGame game, int twilightModifier, boolean ignoreRoamingPenalty, boolean ignoreCheckingDeadPile, Filterable... filters) {
        return Filters.filter(game.getGameState().getHand(playerId), game, Filters.and(filters, Filters.playable(game, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile))).size() > 0;
    }

    public static boolean canPlayFromHand(String playerId, DefaultGame game, int withTwilightRemoved, int twilightModifier, boolean ignoreRoamingPenalty, boolean ignoreCheckingDeadPile, Filterable... filters) {
        return Filters.filter(game.getGameState().getHand(playerId), game, Filters.and(filters, Filters.playable(game, withTwilightRemoved, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile, false))).size() > 0;
    }

    public static boolean canPlayFromDeadPile(String playerId, DefaultGame game, Filterable... filters) {
        return Filters.filter(game.getGameState().getDeadPile(playerId), game, Filters.and(filters, Filters.playable(game, 0, false, true))).size() > 0;
    }

    public static boolean canPlayFromStacked(String playerId, DefaultGame game, Filterable stackedOn, Filterable... filters) {
        final Collection<PhysicalCard> matchingStackedOn = Filters.filterActive(game, stackedOn);
        for (PhysicalCard stackedOnCard : matchingStackedOn) {
            if (Filters.filter(game.getGameState().getStackedCards(stackedOnCard), game, Filters.and(filters, Filters.playable(game))).size() > 0)
                return true;
        }

        return false;
    }

    public static boolean canPlayFromStacked(String playerId, DefaultGame game, int withTwilightRemoved, Filterable stackedOn, Filterable... filters) {
        final Collection<PhysicalCard> matchingStackedOn = Filters.filterActive(game, stackedOn);
        for (PhysicalCard stackedOnCard : matchingStackedOn) {
            if (Filters.filter(game.getGameState().getStackedCards(stackedOnCard), game, Filters.and(filters, Filters.playable(game, withTwilightRemoved, 0, false, false, false))).size() > 0)
                return true;
        }

        return false;
    }

    public static boolean canPlayFromStacked(String playerId, DefaultGame game, int withTwilightRemoved, int twilightModifier, Filterable stackedOn, Filterable... filters) {
        final Collection<PhysicalCard> matchingStackedOn = Filters.filterActive(game, stackedOn);
        for (PhysicalCard stackedOnCard : matchingStackedOn) {
            if (Filters.filter(game.getGameState().getStackedCards(stackedOnCard), game, Filters.and(filters, Filters.playable(game, withTwilightRemoved, twilightModifier, false, false, false))).size() > 0)
                return true;
        }

        return false;
    }

    public static boolean canPlayFromDiscard(String playerId, DefaultGame game, Filterable... filters) {
        if (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK))
            return false;
        return Filters.filter(game.getGameState().getDiscard(playerId), game, Filters.and(filters, Filters.playable(game))).size() > 0;
    }

    public static boolean canPlayFromDiscard(String playerId, DefaultGame game, int modifier, Filterable... filters) {
        if (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK))
            return false;
        return Filters.filter(game.getGameState().getDiscard(playerId), game, Filters.and(filters, Filters.playable(game, modifier))).size() > 0;
    }

    public static boolean canPlayFromDiscard(String playerId, DefaultGame game, int withTwilightRemoved, int modifier, Filterable... filters) {
        if (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK))
            return false;
        return Filters.filter(game.getGameState().getDiscard(playerId), game, Filters.and(filters, Filters.playable(game, withTwilightRemoved, modifier, false, false, false))).size() > 0;
    }

    public static boolean canDiscardFromPlay(final PhysicalCard source, DefaultGame game, final PhysicalCard card) {
        return game.getModifiersQuerying().canBeDiscardedFromPlay(game, source.getOwner(), card, source);
    }

    public static boolean canSelfDiscard(PhysicalCard source, DefaultGame game) {
        return canDiscardFromPlay(source, game, source);
    }

    public static boolean canDiscardFromPlay(final PhysicalCard source, DefaultGame game, int count, final Filterable... filters) {
        return Filters.countActive(game, Filters.and(filters,
                (Filter) (game1, physicalCard) -> game1.getModifiersQuerying().canBeDiscardedFromPlay(game1, source.getOwner(), physicalCard, source))) >= count;
    }

    public static boolean canDiscardFromPlay(final PhysicalCard source, DefaultGame game, final Filterable... filters) {
        return canDiscardFromPlay(source, game, 1, filters);
    }

    public static boolean checkTurnLimit(DefaultGame game, PhysicalCard card, int max) {
        return game.getModifiersQuerying().getUntilEndOfTurnLimitCounter(card).getUsedLimit() < max;
    }

    public static boolean checkPhaseLimit(DefaultGame game, PhysicalCard card, int max) {
        return game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(card, game.getGameState().getCurrentPhase()).getUsedLimit() < max;
    }

    public static boolean checkPhaseLimit(DefaultGame game, PhysicalCard card, Phase phase, int max) {
        return game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(card, phase).getUsedLimit() < max;
    }

    public static boolean checkPhaseLimit(DefaultGame game, PhysicalCard card, String prefix, int max) {
        return game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(card, prefix, game.getGameState().getCurrentPhase()).getUsedLimit() < max;
    }
}
