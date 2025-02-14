package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectAffiliationAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.google.common.collect.Iterables;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeedOutpostAction extends PlayCardAction {
    private ActionCardResolver _destinationTarget;
    private AffiliationResolver _affiliationTarget;

    private enum Progress { cardWasSeeded, placementChosen, affiliationSelected }

    public SeedOutpostAction(FacilityCard cardToSeed) {
        super(cardToSeed, cardToSeed, cardToSeed.getOwner(), Zone.AT_LOCATION,
                ActionType.SEED_CARD, Progress.values());
        setText("Seed " + _cardEnteringPlay.getFullName());
        if (!cardToSeed.isMultiAffiliation()) {
            setProgress(Progress.affiliationSelected);
            _affiliationTarget = new AffiliationResolver(Iterables.getOnlyElement(cardToSeed.getAffiliationOptions()));
        }
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (_cardEnteringPlay instanceof FacilityCard facility) {
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            ST1EGameState gameState = facility.getGame().getGameState();

            Set<PhysicalCard> availableMissions = new HashSet<>();
            for (MissionLocation location : gameState.getSpacelineLocations()) {
                MissionCard missionCard = location.getMissionForPlayer(_performingPlayerId);
                if (facility.canSeedAtMission(location)) {
                    availableMissions.add(missionCard);
                }
            }

            if (!getProgress(Progress.placementChosen)) {

                if (_destinationTarget == null) {
                    _destinationTarget = new SelectCardsResolver(new SelectVisibleCardsAction(cardGame, performingPlayer,
                            "Choose a mission to seed " + _cardEnteringPlay.getCardLink() + " at",
                            Filters.in(availableMissions), 1, 1));
                }

                if (!_destinationTarget.getSelectionAction().wasCarriedOut()) {
                    return _destinationTarget.getSelectionAction();
                } else {
                    _destinationTarget.resolve(cardGame);
                    setProgress(Progress.placementChosen);
                }
            }

            if (!getProgress(Progress.affiliationSelected)) {

                Set<Affiliation> affiliationOptions = new HashSet<>();

                MissionCard selectedMission =
                        (MissionCard) Iterables.getOnlyElement(_destinationTarget.getCards(cardGame));
                if (!getProgress(Progress.affiliationSelected)) {
                    for (Affiliation affiliation : facility.getAffiliationOptions()) {
                        if (facility.canSeedAtMissionAsAffiliation(selectedMission.getGameLocation(),
                                affiliation))
                            affiliationOptions.add(affiliation);
                    }
                }

                if (affiliationOptions.size() > 1) {
                    if (_affiliationTarget == null) {
                        _affiliationTarget = new AffiliationResolver(
                                new SelectAffiliationAction(cardGame, performingPlayer, affiliationOptions));
                    }

                    if (!_affiliationTarget.getSelectionAction().wasCarriedOut()) {
                        return _affiliationTarget.getSelectionAction();
                    } else {
                        _affiliationTarget.resolve();
                        setProgress(Progress.affiliationSelected);
                    }

                } else if (_affiliationTarget == null) {
                    _affiliationTarget = new AffiliationResolver(Iterables.getOnlyElement(affiliationOptions));
                    setProgress(Progress.affiliationSelected);
                }
            }

            if (!getProgress(Progress.cardWasSeeded)) {
                Affiliation selectedAffiliation = _affiliationTarget.getAffiliation();
                facility.changeAffiliation(selectedAffiliation);

                gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, List.of(_cardEnteringPlay));
                performingPlayer.addPlayedAffiliation(facility.getCurrentAffiliation());
                PhysicalCard destinationCard = Iterables.getOnlyElement(_destinationTarget.getCards(cardGame));
                GameLocation destinationLocation = destinationCard.getGameLocation();
                gameState.seedFacilityAtLocation(facility, destinationLocation);
                cardGame.getActionsEnvironment().emitEffectResult(
                        new PlayCardResult(this, _cardEnteringPlay));
                setProgress(Progress.cardWasSeeded);
                setAsSuccessful();
                return getNextAction();
            }
            return null;
        } else {
            throw new InvalidGameLogicException("Tried to process SeedOutpostAction with a non-facility card");
        }
    }

    public void setDestination(MissionLocation location) {
        _destinationTarget = new FixedCardResolver(location.getTopMissionCard());
        setProgress(Progress.placementChosen);
    }

    @Override
    public boolean wasCarriedOut() { return getProgress(Progress.cardWasSeeded);}
}