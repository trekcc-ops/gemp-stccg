package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectAffiliationAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.InCardListFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
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
    private enum Progress { placementChosen, affiliationSelected }

    public SeedOutpostAction(DefaultGame cardGame, FacilityCard cardToSeed) {
        super(cardGame, cardToSeed, cardToSeed, cardToSeed.getOwnerName(), Zone.AT_LOCATION,
                ActionType.SEED_CARD, Progress.values());
        if (!cardToSeed.isMultiAffiliation()) {
            setProgress(Progress.affiliationSelected);
            _affiliationTarget = new AffiliationResolver(Iterables.getOnlyElement(cardToSeed.getAffiliationOptions()));
        }
    }

    public SeedOutpostAction(DefaultGame cardGame, FacilityCard cardToSeed, MissionCard destinationCard) {
        super(cardGame, cardToSeed, cardToSeed, cardToSeed.getOwnerName(), Zone.AT_LOCATION,
                ActionType.SEED_CARD, Progress.values());
        if (!cardToSeed.isMultiAffiliation()) {
            setProgress(Progress.affiliationSelected);
            _affiliationTarget = new AffiliationResolver(Iterables.getOnlyElement(cardToSeed.getAffiliationOptions()));
        }
        setProgress(Progress.placementChosen);
        _destinationTarget = new FixedCardResolver(destinationCard);
    }


    @Override
    protected void continueInitiation(DefaultGame cardGame) throws InvalidGameLogicException {
        if (_cardEnteringPlay instanceof FacilityCard facility && cardGame instanceof ST1EGame stGame) {
            ST1EGameState gameState = stGame.getGameState();

            Set<PhysicalCard> availableMissions = new HashSet<>();
            for (MissionLocation location : gameState.getSpacelineLocations()) {
                MissionCard missionCard = location.getMissionForPlayer(_performingPlayerId);
                boolean canPlayHere = stGame.getRules().isLocationValidPlayCardDestinationPerRules(
                        stGame, facility, location, SeedCardAction.class, facility.getOwnerName(),
                        facility.getAffiliationOptions());
                if (canPlayHere) {
                    availableMissions.add(missionCard);
                }
            }

            if (!getProgress(Progress.placementChosen)) {

                if (_destinationTarget == null) {
                    _destinationTarget = new SelectCardsResolver(new SelectVisibleCardsAction(cardGame,
                            _performingPlayerId,
                            "Choose a mission to seed " + _cardEnteringPlay.getCardLink() + " at",
                            new InCardListFilter(availableMissions),1, 1));
                }

                if (!_destinationTarget.getSelectionAction().wasCarriedOut()) {
                    cardGame.addActionToStack(_destinationTarget.getSelectionAction());
                } else {
                    _destinationTarget.resolve(cardGame);
                    setProgress(Progress.placementChosen);
                }
            } else if (!getProgress(Progress.affiliationSelected)) {

                Set<Affiliation> affiliationOptions = new HashSet<>();

                MissionCard selectedMission =
                        (MissionCard) Iterables.getOnlyElement(_destinationTarget.getCards(cardGame));
                if (!getProgress(Progress.affiliationSelected)) {
                    for (Affiliation affiliation : facility.getAffiliationOptions()) {
                        boolean canSeedHereAsThisAffiliation = stGame.getRules().
                                isLocationValidPlayCardDestinationPerRules(stGame, facility,
                                        selectedMission.getGameLocation(stGame), SeedCardAction.class,
                                        facility.getOwnerName(), List.of(affiliation));
                        if (canSeedHereAsThisAffiliation)
                            affiliationOptions.add(affiliation);
                    }
                }

                if (affiliationOptions.size() > 1) {
                    if (_affiliationTarget == null) {
                        _affiliationTarget = new AffiliationResolver(
                                new SelectAffiliationAction(cardGame, _performingPlayerId, affiliationOptions));
                    }

                    if (!_affiliationTarget.getSelectionAction().wasCarriedOut()) {
                        cardGame.addActionToStack(_affiliationTarget.getSelectionAction());
                    } else {
                        _affiliationTarget.resolve();
                        setProgress(Progress.affiliationSelected);
                    }

                } else if (_affiliationTarget == null) {
                    _affiliationTarget = new AffiliationResolver(Iterables.getOnlyElement(affiliationOptions));
                    setProgress(Progress.affiliationSelected);
                }
            } else {
                setAsInitiated();
            }
        } else {
            throw new InvalidGameLogicException("Tried to process SeedOutpostAction with a non-facility card");
        }
    }

    public void processEffect(DefaultGame cardGame) {
        try {
            if (cardGame instanceof ST1EGame stGame) {
                Player performingPlayer = stGame.getPlayer(_performingPlayerId);

                Affiliation selectedAffiliation = _affiliationTarget.getAffiliation();
                FacilityCard facility = (FacilityCard) _cardEnteringPlay;
                facility.changeAffiliation(stGame, selectedAffiliation);

                stGame.getGameState().removeCardsFromZoneWithoutSendingToClient(stGame, List.of(_cardEnteringPlay));
                performingPlayer.addPlayedAffiliation(selectedAffiliation);
                PhysicalCard destinationCard = Iterables.getOnlyElement(_destinationTarget.getCards(stGame));
                facility.setLocationId(stGame, destinationCard.getLocationId());
                stGame.getGameState().addCardToZone(stGame, facility, Zone.AT_LOCATION, _actionContext);
                saveResult(new PlayCardResult(this, _cardEnteringPlay));
                setAsSuccessful();
            } else {
                cardGame.sendErrorMessage("Unable to process seed outpost action in a non-1E game");
                setAsFailed();
            }
        } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    public void setDestination(MissionCard mission) {
        _destinationTarget = new FixedCardResolver(mission);
        setProgress(Progress.placementChosen);
    }

}