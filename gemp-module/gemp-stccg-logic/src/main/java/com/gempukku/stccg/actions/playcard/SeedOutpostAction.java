package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.actions.choose.SelectAffiliationAction;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.google.common.collect.Iterables;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeedOutpostAction extends PlayCardAction {
    private ActionCardResolver _destinationTarget;
    private SelectAffiliationAction _selectAffiliationAction;
    private enum Progress { cardWasSeeded, placementChosen, affiliationSelected }

    public SeedOutpostAction(FacilityCard cardToSeed) {
        super(cardToSeed, cardToSeed, cardToSeed.getOwnerName(), Zone.AT_LOCATION,
                ActionType.SEED_CARD, Progress.values());
        setText("Seed " + _cardEnteringPlay.getFullName());
        if (!cardToSeed.isMultiAffiliation()) {
            setProgress(Progress.affiliationSelected);
            _selectAffiliationAction =
                    new SelectAffiliationAction(cardToSeed.getOwner(), cardToSeed.getAffiliationOptions());
        }
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
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
                    _destinationTarget = new ActionCardResolver(new SelectVisibleCardsAction(performingPlayer,
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
                        try {
                            if (facility.canSeedAtMissionAsAffiliation(selectedMission.getLocation(),
                                    affiliation))
                                affiliationOptions.add(affiliation);
                        } catch (InvalidGameLogicException exp) {
                            cardGame.sendErrorMessage(exp);
                        }
                    }
                }

                if (affiliationOptions.size() > 1) {
                    if (_selectAffiliationAction == null) {
                        _selectAffiliationAction =
                                new SelectAffiliationAction(performingPlayer, affiliationOptions);
                    }

                    if (!_selectAffiliationAction.wasCarriedOut()) {
                        setProgress(Progress.affiliationSelected);
                        return _selectAffiliationAction;
                    }

                } else {
                    _selectAffiliationAction =
                            new SelectAffiliationAction(performingPlayer, affiliationOptions);
                    setProgress(Progress.affiliationSelected);
                }
            }

            if (!getProgress(Progress.cardWasSeeded)) {
                Zone originalZone = _cardEnteringPlay.getZone();
                Affiliation selectedAffiliation = _selectAffiliationAction.getSelectedAffiliation();
                facility.changeAffiliation(selectedAffiliation);

                cardGame.sendMessage(_cardEnteringPlay.getOwnerName() + " seeded " + _cardEnteringPlay.getCardLink());
                gameState.removeCardFromZone(_cardEnteringPlay);
                gameState.getPlayer(_cardEnteringPlay.getOwnerName())
                        .addPlayedAffiliation(facility.getAffiliation());
                gameState.seedFacilityAtLocation(facility,
                        Iterables.getOnlyElement(_destinationTarget.getCards(cardGame)).getLocationZoneIndex());
                cardGame.getActionsEnvironment().emitEffectResult(
                        new PlayCardResult(this, originalZone, _cardEnteringPlay));
                setProgress(Progress.cardWasSeeded);
                return getNextAction();
            }
            return null;
        } else {
            throw new InvalidGameLogicException("Tried to process SeedOutpostAction with a non-facility card");
        }
    }

    public void setDestination(MissionLocation location) {
        _destinationTarget = new ActionCardResolver(location.getTopMission());
        setProgress(Progress.placementChosen);
    }

    public PhysicalCard getCardToSeed() { return _cardEnteringPlay; }

    @Override
    public boolean wasCarriedOut() { return getProgress(Progress.cardWasSeeded);}
}