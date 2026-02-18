package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.rules.UndefinedRuleException;

import java.util.*;

public class PlayCardDestinationRules {

    public static boolean isLocationValidPlayCardDestinationForFacilityPerRules(
            ST1EGame cardGame, GameLocation location, FacilityCard facilityEnteringPlay,
            Class<? extends PlayCardAction> actionClass, Player performingPlayer,
            Collection<Affiliation> affiliationOptions
    ) throws UndefinedRuleException {

        final boolean isSeeding = SeedCardAction.class.isAssignableFrom(actionClass);

        // Each facility must be seeded in its native quadrant
        if (isSeeding && !location.isInQuadrant(facilityEnteringPlay.getNativeQuadrant())) {
            return false;
        }

        /* You may not seed or build a facility at a location where you already own one (unless permitted by a
            card that allows them to co-exist).
         */
        Collection<PhysicalCard> facilitiesOwnedByPlayerHere = Filters.filteredCardsYouOwnInPlay(
                cardGame, performingPlayer, CardType.FACILITY, Filters.atLocation(location));
        if (!facilitiesOwnedByPlayerHere.isEmpty()) {
            return false;
            // TODO - Need to add a check for cards that can coexist
        }

        if (!facilityEnteringPlay.isOutpost()) {
            // TODO - Most Facilities state on their cards where they may be seeded
            throw new UndefinedRuleException("No rule defined yet for non-outpost facilities entering play");
        } else if (isSeeding) {
            // Outposts may never be seeded at the homeworld of any affiliation
            if (location.isHomeworld()) {
                return false;
            }

            // Otherwise, outposts may be seeded at any mission in their native quadrant with a matching affiliation icon
            // (No need to check for native quadrant again; that was performed above)
            // TODO - This may not be a sufficient check for multi-affiliation cards under special reporting
            return location instanceof MissionLocation mission &&
                    mission.hasMatchingAffiliationIcon(cardGame, performingPlayer.getPlayerId(), affiliationOptions);
        } else {
            return location instanceof MissionLocation missionLocation && !missionLocation.isHomeworld();
        }
    }

    private static boolean canReportableBePlayedToFacilityOrShip(CardWithCrew destination,
                                                                ReportableCard reportingCard,
                                                                boolean specialReporting,
                                                                ST1EGame cardGame) {
        String performingPlayerName = reportingCard.getOwnerName();
        if (reportingCard instanceof PersonnelCard affilCard &&
                !canPersonnelBoardShipOrFacility(affilCard, destination, cardGame)) {
            return false;
        } else if (reportingCard instanceof ShipCard shipCard && !shipCard.isCompatibleWith(cardGame, destination)) {
            return false;
        } else if (specialReporting) {
            return true;
        } else if (destination instanceof FacilityCard facility) {
            if (List.of(FacilityType.OUTPOST, FacilityType.HEADQUARTERS).contains(facility.getFacilityType()) &&
                    facility.isUsableBy(performingPlayerName)) {
                if (reportingCard instanceof EquipmentCard) {
                    return true;
                } else return reportingCard instanceof AffiliatedCard affilCard &&
                        facility.isInQuadrant(cardGame, affilCard.getNativeQuadrant());
            }
        }
        return false;
    }

    private static boolean canPersonnelBoardShipOrFacility(PersonnelCard reportingCard,
                                                           CardWithCrew destination, ST1EGame cardGame) {
        String performingPlayerName = reportingCard.getOwnerName();
        for (PersonnelCard cardAboard : destination.getPersonnelAboard(cardGame)) {
            if (cardAboard.isControlledBy(performingPlayerName) && !cardAboard.isCompatibleWith(cardGame, reportingCard)) {
                return false;
            }
        }
        return !destination.isControlledBy(reportingCard.getOwnerName()) ||
                reportingCard.isCompatibleWith(cardGame, destination);
    }

    public static Map<PhysicalCard, List<Affiliation>> getDestinationAndAffiliationMapForReportingCards(
            ReportableCard reportingCard, ST1EGame cardGame, Collection<PhysicalCard> eligibleDestinations,
            boolean specialReporting, CardFilter applicableFilterForReportingCard
    ) {
        Map<PhysicalCard, List<Affiliation>> result = new HashMap<>();
        String performingPlayerName = reportingCard.getOwnerName();
        List<Affiliation> currentAffiliationOptions = new ArrayList<>();
        if (reportingCard instanceof AffiliatedCard affiliatedCard) {
            currentAffiliationOptions.addAll(affiliatedCard.getCurrentAffiliations());
        }
        for (PhysicalCard destinationOption : eligibleDestinations) {
            try {
                if (destinationOption instanceof MissionCard missionCard &&
                        missionCard.getGameLocation(cardGame) instanceof MissionLocation missionLocation &&
                        missionLocation.getMissionForPlayer(performingPlayerName) == missionCard &&
                        specialReporting) {

                    List<Affiliation> allowedAffiliations = new ArrayList<>();
                    if (reportingCard instanceof AffiliatedCard affiliatedCard) {
                        for (Affiliation affiliation : currentAffiliationOptions) {
                            affiliatedCard.setProxyAffiliation(affiliation);
                            if (applicableFilterForReportingCard.accepts(cardGame, affiliatedCard)) {
                                allowedAffiliations.add(affiliation);
                            }
                        }
                    }

                    if (reportingCard instanceof ShipCard && missionLocation.isSpace()) {
                        result.put(destinationOption, allowedAffiliations);
                    } else if (missionCard.isPlanet()) {
                        result.put(destinationOption, allowedAffiliations);
                    }
                } else if (destinationOption instanceof CardWithCrew cardWithCrew) {

                    if(reportingCard instanceof AffiliatedCard affilCard) {
                        List<Affiliation> affiliationOptions = new ArrayList<>();
                        for (Affiliation selectedAffiliation : currentAffiliationOptions) {
                            affilCard.setProxyAffiliation(selectedAffiliation);
                            if (applicableFilterForReportingCard.accepts(cardGame, affilCard) &&
                                    canReportableBePlayedToFacilityOrShip(cardWithCrew, reportingCard, specialReporting, cardGame)) {
                                affiliationOptions.add(selectedAffiliation);
                            }
                        }
                        if (!affiliationOptions.isEmpty()) {
                            result.put(destinationOption, affiliationOptions);
                        }
                    } else {
                        if (canReportableBePlayedToFacilityOrShip(cardWithCrew, reportingCard, specialReporting, cardGame)) {
                            result.put(destinationOption, currentAffiliationOptions);
                        }
                    }
                }
            } catch(InvalidGameLogicException ignored) {
            }
        }
        if (reportingCard instanceof AffiliatedCard affilCard) {
            affilCard.clearProxyAffiliation();
        }
        return result;
    }

    public static Map<PhysicalCard, List<Affiliation>> getDestinationAndAffiliationMapForReportingCards(
            ReportableCard reportingCard, ST1EGame cardGame, boolean specialReporting
    ) {
        Map<PhysicalCard, List<Affiliation>> result = new HashMap<>();
        String performingPlayerName = reportingCard.getOwnerName();
        List<Affiliation> currentAffiliationOptions = new ArrayList<>();
        if (reportingCard instanceof AffiliatedCard affiliatedCard) {
            currentAffiliationOptions.addAll(affiliatedCard.getCurrentAffiliations());
        }

        for (PhysicalCard destinationOption : cardGame.getAllCardsInPlay()) {
            try {
                if (destinationOption instanceof MissionCard missionCard &&
                        missionCard.getGameLocation(cardGame) instanceof MissionLocation missionLocation &&
                        missionLocation.isPlanet() &&
                        missionLocation.getMissionForPlayer(performingPlayerName) == missionCard &&
                        specialReporting) {
                    result.put(destinationOption, currentAffiliationOptions);
                } else if (destinationOption instanceof CardWithCrew cardWithCrew) {

                    if(reportingCard instanceof AffiliatedCard affilCard) {
                        List<Affiliation> affiliationOptions = new ArrayList<>();
                        for (Affiliation selectedAffiliation : currentAffiliationOptions) {
                            affilCard.setProxyAffiliation(selectedAffiliation);
                            if (canReportableBePlayedToFacilityOrShip(cardWithCrew, reportingCard, specialReporting, cardGame)) {
                                affiliationOptions.add(selectedAffiliation);
                            }
                        }
                        if (!affiliationOptions.isEmpty()) {
                            result.put(destinationOption, affiliationOptions);
                        }
                    } else {
                        if (canReportableBePlayedToFacilityOrShip(cardWithCrew, reportingCard, specialReporting, cardGame)) {
                            result.put(destinationOption, currentAffiliationOptions);
                        }
                    }
                }
            } catch(InvalidGameLogicException ignored) {
            }
        }
        if (reportingCard instanceof AffiliatedCard affilCard) {
            affilCard.clearProxyAffiliation();
        }
        return result;
    }

}