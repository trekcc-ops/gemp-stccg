package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.*;

public abstract class AffiliatedCard extends ST1EPhysicalCard implements CardWithCompatibility {

    protected List<Affiliation> _currentAffiliations = new ArrayList<>();
    private Affiliation _defaultCardArtAffiliation;

    AffiliatedCard(int cardId, String ownerName, CardBlueprint blueprint) {
        super(cardId, ownerName, blueprint);
        _currentAffiliations.addAll(blueprint.getAffiliations());
        _defaultCardArtAffiliation = _currentAffiliations.getFirst();
    }

    public Quadrant getNativeQuadrant() {
        return _blueprint.getQuadrant();
    }

    public boolean isInQuadrant(ST1EGame cardGame, Quadrant quadrant) {
        GameLocation location = cardGame.getGameState().getLocationById(getLocationId());
        return location instanceof MissionLocation mission && mission.isInQuadrant(quadrant);
    }

    @JsonIgnore
    public boolean isMultiAffiliation() {
        return getAffiliationOptions().size() > 1;
    }

    public Affiliation getAffiliationForCardArt() {
        return _defaultCardArtAffiliation;
    }

    @JsonProperty("affiliation")
    public List<Affiliation> getCurrentAffiliations() {
        return _currentAffiliations;
    }
    

    @JsonProperty("affiliation")
    public void setCurrentAffiliation(Affiliation... affiliations) {
        /* Do not add any additional functionality to this method, because it is used to test compatibility under
                multiple affiliations */
        _currentAffiliations.clear();
        _currentAffiliations.addAll(Arrays.asList(affiliations));
    }

    public void changeAffiliation(ST1EGame cardGame, Affiliation affiliation) {
        setCurrentAffiliation(affiliation);
        if (getAffiliationOptions().contains(affiliation)) {
            _defaultCardArtAffiliation = affiliation;
        }
        if (getAffiliationOptions().size() > 1) {
            if (this instanceof ReportableCard reportable &&
                    cardGame.getGameState().getLocationById(getLocationId()) instanceof MissionLocation missionLocation) {
                AwayTeam awayTeam = cardGame.getGameState().getAwayTeamForCard(reportable);
                if (awayTeam != null) {
                    if (awayTeam.canBeDisbanded(cardGame)) {
                        awayTeam.disband(cardGame);
                    } else {
                        if (!awayTeam.isCompatibleWith(cardGame, reportable)) {
                            cardGame.getGameState().removeCardFromAwayTeam(cardGame, reportable);
                        }
                        if (cardGame.getGameState().getAwayTeamForCard(reportable) == null) {
                            cardGame.getGameState().addCardToEligibleAwayTeam(cardGame, reportable, missionLocation);
                        }
                    }
                }
            }
        }
    }


    public Set<Affiliation> getAffiliationOptions() {
        return _blueprint.getAffiliations();
    }

    boolean doesNotWorkWith(AffiliatedCard otherCard) {
        return getBlueprint().doesNotWorkWithPerRestrictionBox(this, otherCard);
    }

    public boolean matchesAffiliationOf(AffiliatedCard otherCard) {
        for (Affiliation affiliation : _currentAffiliations) {
            if (otherCard.isAffiliation(affiliation)) {
                return true;
            }
        }
        return false;
    }

    public boolean canReportToCrewAsAffiliation(CardWithCrew cardWithCrew, Affiliation affiliation, ST1EGame stGame) {
            /* Normally, Personnel, Ship, and Equipment cards play at a usable, compatible outpost or headquarters
                in their native quadrant. */
        if (this instanceof ReportableCard) {
            // TODO - Does not perform any compatibility checks other than affiliation
            if (cardWithCrew instanceof ShipCard shipCard) {
                Collection<CardWithCompatibility> otherCards = new ArrayList<>();
                otherCards.add(shipCard);
                otherCards.addAll(shipCard.getPersonnelInCrew(stGame));
                return isCompatibleWithOtherCardsAsAffiliation(affiliation, otherCards, stGame);
            } else if (cardWithCrew instanceof FacilityCard facilityCard) {
                if ((facilityCard.getFacilityType() == FacilityType.OUTPOST || facilityCard.getFacilityType() == FacilityType.HEADQUARTERS) &&
                        facilityCard.isUsableBy(getOwnerName()) && facilityCard.isInQuadrant(stGame, getNativeQuadrant())) {
                    Collection<CardWithCompatibility> otherCards = new ArrayList<>();
                    otherCards.add(facilityCard);
                    otherCards.addAll(cardWithCrew.getPersonnelInCrew(stGame));
                    return isCompatibleWithOtherCardsAsAffiliation(affiliation, otherCards, stGame);
                }
            }
        }
        return false;
    }


    public boolean isCompatibleWithOtherCardsAsAffiliation(Affiliation affiliation,
                                                           Collection<? extends CardWithCompatibility> otherCards,
                                                           ST1EGame stGame) {
        Affiliation[] currentAffiliations = getCurrentAffiliations().toArray(new Affiliation[0]);
        setCurrentAffiliation(affiliation);
        boolean allCompatible = true;
        for (CardWithCompatibility otherCard : otherCards) {
            if (!isCompatibleWith(stGame, otherCard)) {
                allCompatible = false;
            }
        }
            // Set the affiliation back to what it was originally!
        setCurrentAffiliation(currentAffiliations);
        return allCompatible;
    }


    public boolean isAffiliation(Affiliation affiliation) {
        return _currentAffiliations.contains(affiliation);
    }

    @Override
    public boolean hasTransporters() {
        return _blueprint.getCardType() == CardType.SHIP || _blueprint.getCardType() == CardType.FACILITY; // TODO - Cards with no transporters
    }

}