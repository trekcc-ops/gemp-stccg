package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.cards.cardgroup.PhysicalCardGroup;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.WeaponsDisabledModifier;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.rules.UndefinedRuleException;
import com.gempukku.stccg.rules.generic.RuleSet;

import java.util.*;

public class ST1ERuleSet extends RuleSet<ST1EGame> {

    @Override
    protected void applySpecificRules(ST1EGame cardGame) {
        applyActionProxiesAsRules(cardGame,
                new ST1EPlayCardInPhaseRule(),
                new ST1EChangeAffiliationRule(),
                new ST1EPhaseActionsRule(),
                new SpecialDownloadRule()
        );

        List<Modifier> modifiers = getGlobalRulesBasedModifiersForCardsInPlay();
        for (Modifier modifier : modifiers) {
            cardGame.getModifiersEnvironment().addAlwaysOnModifier(modifier);
        }

        new ST1EAffiliationAttackRestrictionsRule(cardGame).applyRule();
    }

    public boolean isLocationValidPlayCardDestinationPerRules(ST1EGame game, FacilityCard facility,
                                                              GameLocation location,
                                                              Class<? extends PlayCardAction> actionClass,
                                                              String performingPlayerName,
                                                              Collection<Affiliation> affiliationOptions) {
        try {
            Player performingPlayer = game.getPlayer(performingPlayerName);
            return PlayCardDestinationRules.isLocationValidPlayCardDestinationForFacilityPerRules(
                    game, location, facility, actionClass, performingPlayer, affiliationOptions);
        } catch(UndefinedRuleException | PlayerNotFoundException exp) {
            game.sendErrorMessage(exp);
            return false;
        }
    }

    public boolean canShipAttemptMission(ShipCard ship, int locationId, ST1EGame cardGame,
                                         String performingPlayerName) {
        try {
            GameLocation location = cardGame.getGameState().getLocationById(locationId);
            if (location instanceof MissionLocation missionLocation) {
                return MissionAttemptRules.canShipAttemptMission(ship, missionLocation, cardGame,
                        performingPlayerName);
            } else {
                throw new InvalidGameLogicException("Tried to attempt a non-mission location");
            }
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }



    public boolean areCardsCompatiblePerRules(CardWithCompatibility card1, CardWithCompatibility card2) {
        return CompatibilityRule.areCardsCompatible(card1, card2);
    }

    @Override
    public List<Modifier> getModifiersWhileCardIsInPlay(PhysicalCard card) {
        List<Modifier> result = new ArrayList<>();
        if (card instanceof FacilityCard facility) {
            result.add(DockingRules.getExtendedShieldsModifier(facility));
        }
        return result;
    }

    @Override
    public Zone getDiscardZone(boolean cardWorthPoints) {
        if (cardWorthPoints) {
            return Zone.POINT_AREA;
        } else {
            return Zone.DISCARD;
        }
    }

    @Override
    public PhysicalCardGroup getDiscardToScorePointsGroup(DefaultGame cardGame, PhysicalCard card,
                                                          String performingPlayerId) throws PlayerNotFoundException {
        return cardGame.getPlayer(performingPlayerId).getCardGroup(Zone.POINT_AREA);
    }

    public List<Modifier> getGlobalRulesBasedModifiersForCardsInPlay() {
        // Rule about using WEAPONS
        List<Modifier> result = new ArrayList<>();
        CardFilter facilityOrExposedShip = Filters.or(Filters.exposedShip, CardType.FACILITY);
        CardFilter controllerControlsMatchingPersonnelAboard = Filters.controllerControlsMatchingPersonnelAboard;
        CardFilter affectedFilter = Filters.and(
                Filters.or(Filters.ship, Filters.facility),
                Filters.notAll(Filters.active, facilityOrExposedShip, controllerControlsMatchingPersonnelAboard)
        );
        Modifier weaponsModifier = new WeaponsDisabledModifier(affectedFilter);
        result.add(weaponsModifier);
        return result;
    }

    public boolean canAUCardBePlayed(ST1EGame stGame, PhysicalCard cardToPlay) {
        return stGame.isCardAllowingPlayerToPlayAUCards(cardToPlay.getOwnerName());
    }

    private boolean canAUCardEnterPlay(ST1EGame stGame, PhysicalCard cardToPlay,
                                       PlayCardAction.EnterPlayActionType actionType) {
        return switch(actionType) {
            case DOWNLOAD, PLAY -> stGame.isCardAllowingPlayerToPlayAUCards(cardToPlay.getOwnerName());
            case SEED -> stGame.isCardAllowingPlayerToSeedAUCards(cardToPlay.getOwnerName());
        };
    }

    public boolean cardCanEnterPlay(DefaultGame cardGame, PhysicalCard card,
                                    PlayCardAction.EnterPlayActionType actionType) {
        if (cardGame instanceof ST1EGame stGame) {
            if (card.hasIcon(cardGame, CardIcon.AU_ICON) &&
                    !canAUCardEnterPlay(stGame, card, actionType)) {
                return false;
            }
            if (actionType == PlayCardAction.EnterPlayActionType.PLAY && card.getCardType() == CardType.DILEMMA) {
                return false;
            }
            return !cannotEnterPlayPerUniqueness(cardGame, card);
        } else {
            return false;
        }
    }

    protected boolean cannotEnterPlayPerUniqueness(DefaultGame cardGame, PhysicalCard cardToEnterPlay) {
        if (cardToEnterPlay.isUniversal()) {
            return false;
        }
        for (PhysicalCard cardInPlay : cardGame.getAllCardsInPlay()) {
            if (cardInPlay.isCopyOf(cardToEnterPlay) || cardInPlay.isPersonaVersionOf(cardToEnterPlay)) {
                if (cardInPlay.isOwnedBy(cardToEnterPlay.getOwnerName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public Map<PhysicalCard, List<Affiliation>> getDestinationAndAffiliationMapForReportingCard(
            ReportableCard reportingCard, ST1EGame cardGame, boolean specialReporting
    ) {
        return PlayCardDestinationRules.getDestinationAndAffiliationMapForReportingCards(
                reportingCard, cardGame, specialReporting);
    }

    public Map<PhysicalCard, List<Affiliation>> getDestinationAndAffiliationMapForReportingCard(
            ReportableCard reportingCard, ST1EGame cardGame, Collection<PhysicalCard> eligibleDestinations,
            boolean specialReporting
    ) {
        return PlayCardDestinationRules.getDestinationAndAffiliationMapForReportingCards(
                reportingCard, cardGame, eligibleDestinations, specialReporting);
    }


    public Map<PhysicalCard, List<Affiliation>> getDestinationMapForSeedingFacilityWithGameTextDestinations(
            FacilityCard facility, String performingPlayerName, Collection<MissionLocation> gameTextDestinations,
            ST1EGame cardGame) {
        Map<PhysicalCard, List<Affiliation>> result = new HashMap<>();
        for (MissionLocation location : gameTextDestinations) {
            try {
                Collection<PhysicalCard> facilitiesOwnedByPlayerHere = Filters.filterCardsInPlay(
                        cardGame, Filters.owner(performingPlayerName), CardType.FACILITY,
                        Filters.atLocation(location));
                if (facilitiesOwnedByPlayerHere.isEmpty() &&
                        (!location.isHomeworld() || facility.getFacilityType() != FacilityType.OUTPOST) &&
                        location.isInQuadrant(facility.getNativeQuadrant())
                ) {
                    MissionCard card = location.getMissionForPlayer(performingPlayerName);
                    result.put(card, facility.getCurrentAffiliations());
                }
            } catch(InvalidGameLogicException ignored) {

            }
        }
        return result;
    }

    public Map<PhysicalCard, List<Affiliation>> getDestinationMapForSeedingFacilityPerRules(
            FacilityCard facility, String performingPlayerName, ST1EGame stGame) {
        Map<PhysicalCard, List<Affiliation>> result = new HashMap<>();
        for (MissionLocation location : stGame.getGameState().getSpacelineLocations()) {
            try {
                Collection<PhysicalCard> facilitiesOwnedByPlayerHere = Filters.filterCardsInPlay(
                        stGame, Filters.owner(performingPlayerName), CardType.FACILITY,
                        Filters.atLocation(location));
                if (facilitiesOwnedByPlayerHere.isEmpty() &&
                        (!location.isHomeworld() || facility.getFacilityType() != FacilityType.OUTPOST) &&
                        location.isInQuadrant(facility.getNativeQuadrant())
                ) {

                    List<Affiliation> affiliationsToAdd = new ArrayList<>();
                    for (Affiliation affiliationToAdd : facility.getAffiliationOptions()) {
                        if (location.hasMatchingAffiliationIcon(stGame, performingPlayerName, List.of(affiliationToAdd))) {
                            affiliationsToAdd.add(affiliationToAdd);
                        }
                    }
                    if (!affiliationsToAdd.isEmpty()) {
                        MissionCard card = location.getMissionForPlayer(performingPlayerName);
                        result.put(card, affiliationsToAdd);
                    }
                }
            } catch(InvalidGameLogicException ignored) {

            }
        }
        return result;
    }
}