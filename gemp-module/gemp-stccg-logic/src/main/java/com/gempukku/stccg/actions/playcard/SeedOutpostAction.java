package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
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
import java.util.Set;

public class SeedOutpostAction extends PlayCardAction {
    private boolean _cardWasSeeded, _placementWasChosen, _affiliationWasChosen;
    private int _locationZoneIndex;
    private final Set<Affiliation> _affiliationOptions = new HashSet<>();
    private Affiliation _selectedAffiliation;
    private final FacilityCard _cardEnteringPlay;
    private SelectAffiliationAction _selectAffiliationAction;
    private SelectCardsAction _selectDestinationAction;
    public SeedOutpostAction(FacilityCard cardToSeed) {
        super(cardToSeed, cardToSeed, cardToSeed.getOwnerName(), Zone.AT_LOCATION, ActionType.SEED_CARD);
        _cardEnteringPlay = cardToSeed;
        setText("Seed " + _cardEnteringPlay.getFullName());
        if (cardToSeed.isMultiAffiliation()) {
            _affiliationWasChosen = false;
        } else {
            _affiliationWasChosen = true;
            _selectedAffiliation = cardToSeed.getCurrentAffiliation();
        }
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        ST1EGameState gameState = _cardEnteringPlay.getGame().getGameState();

        Set<PhysicalCard> availableMissions = new HashSet<>();
        for (MissionLocation location : gameState.getSpacelineLocations()) {
            MissionCard missionCard = location.getMissionForPlayer(_performingPlayerId);
            if (_cardEnteringPlay.canSeedAtMission(location)) {
                availableMissions.add(missionCard);
            }
        }

        if (!_placementWasChosen) {

            if (_selectDestinationAction == null) {
                _selectDestinationAction = new SelectVisibleCardsAction(performingPlayer,
                        "Choose a mission to seed " + _cardEnteringPlay.getCardLink() + " at",
                        Filters.in(availableMissions), 1, 1);
                return _selectDestinationAction;
            } else if (_selectDestinationAction.wasCarriedOut()) {
                MissionCard selectedMission =
                        (MissionCard) Iterables.getOnlyElement(_selectDestinationAction.getSelectedCards());
                _locationZoneIndex = selectedMission.getLocationZoneIndex();
                _placementWasChosen = true;
                if (!_affiliationWasChosen) {
                    for (Affiliation affiliation : _cardEnteringPlay.getAffiliationOptions()) {
                        try {
                            if (_cardEnteringPlay.canSeedAtMissionAsAffiliation(selectedMission.getLocation(),
                                    affiliation))
                                _affiliationOptions.add(affiliation);
                        } catch(InvalidGameLogicException exp) {
                            cardGame.sendErrorMessage(exp);
                        }
                    }
                    if (_affiliationOptions.size() == 1) {
                        _affiliationWasChosen = true;
                        _selectedAffiliation = Iterables.getOnlyElement(_affiliationOptions);
                    }
                }
            }
        }

        if (!_affiliationWasChosen) {

            if (_affiliationOptions.size() > 1) {
                if (_selectAffiliationAction == null) {
                    _selectAffiliationAction =
                            new SelectAffiliationAction(performingPlayer, _affiliationOptions);
                    return _selectAffiliationAction;
                } else if (_selectAffiliationAction.wasCarriedOut()) {
                    _selectedAffiliation = _selectAffiliationAction.getSelectedAffiliation();
                    _affiliationWasChosen = true;
                }
            } else {
                _selectedAffiliation = Iterables.getOnlyElement(_affiliationOptions);
                _affiliationWasChosen = true;
            }
        }

        if (!_cardWasSeeded) {
            _cardEnteringPlay.changeAffiliation(_selectedAffiliation);

            cardGame.sendMessage(_cardEnteringPlay.getOwnerName() + " seeded " + _cardEnteringPlay.getCardLink());
            gameState.removeCardFromZone(_cardEnteringPlay);
            gameState.getPlayer(_cardEnteringPlay.getOwnerName())
                    .addPlayedAffiliation(_cardEnteringPlay.getCurrentAffiliation());
            gameState.seedFacilityAtLocation(_cardEnteringPlay, _locationZoneIndex);
            cardGame.getActionsEnvironment().emitEffectResult(
                    new PlayCardResult(this, _fromZone, _cardEnteringPlay));
            _cardWasSeeded = true;
            return getNextAction();
        }
        return null;
    }

    public void setDestination(MissionLocation location) {
        _locationZoneIndex = location.getLocationZoneIndex();
        _placementWasChosen = true;
    }

    public PhysicalCard getCardToSeed() { return _cardEnteringPlay; }
}