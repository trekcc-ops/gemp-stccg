package com.gempukku.stccg.actions.targetresolver;

import com.gempukku.stccg.actions.choose.SelectAffiliationAction;
import com.gempukku.stccg.actions.choose.SelectCardAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.playcard.PlayFacilityAction;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.InCardListFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.google.common.collect.Iterables;

import java.util.*;

public class PlayFacilityResolver implements ActionTargetResolver {

    private final FacilityCard _cardEnteringPlay;
    private Affiliation _affiliationToPlayAs;
    private final String _performingPlayerName;
    private SelectCardAction _selectDestinationAction;
    private MissionCard _destinationMission;
    private boolean _isFailed;
    private SelectAffiliationAction _affiliationSelectionAction;
    private final Map<MissionCard, Set<Affiliation>> _destinationMap = new HashMap<>();

    public PlayFacilityResolver(ST1EGame stGame, FacilityCard facilityCard, CardFilter additionalDestinationFilter) {
        _cardEnteringPlay = facilityCard;
        _performingPlayerName = _cardEnteringPlay.getOwnerName();
        for (MissionLocation location : stGame.getGameState().getSpacelineLocations()) {
            try {
                MissionCard missionCard = location.getMissionForPlayer(facilityCard.getOwnerName());
                boolean canPlayHere = stGame.getRules().isLocationValidPlayCardDestinationPerRules(
                        stGame, _cardEnteringPlay, location, PlayFacilityAction.class, _performingPlayerName,
                        _cardEnteringPlay.getAffiliationOptions());
                if (canPlayHere && additionalDestinationFilter.accepts(stGame, missionCard)) {
                    _destinationMap.put(missionCard, facilityCard.getAffiliationOptions());
                }
            } catch(InvalidGameLogicException ignored) {

            }
        }
        if (_destinationMap.size() == 1) {
            _destinationMission = Iterables.getOnlyElement(_destinationMap.keySet());
        }
    }

    @Override
    public void resolve(DefaultGame cardGame) throws InvalidGameLogicException {
        if (cardGame instanceof ST1EGame stGame) {
            if (_destinationMission == null) {
                selectDestination(stGame);
            } else if (_affiliationToPlayAs == null) {
                selectAffiliation(stGame);
            }
        } else {
            _isFailed = true;
        }
    }

    private void selectDestination(ST1EGame stGame) {
        if (_selectDestinationAction == null) {
            _selectDestinationAction = new SelectVisibleCardAction(stGame, _performingPlayerName,
                    "Choose a mission to seed " + _cardEnteringPlay.getCardLink() + " at",
                    new InCardListFilter(_destinationMap.keySet()));
            stGame.addActionToStack(_selectDestinationAction);
        } else if (_selectDestinationAction.wasSuccessful()) {
            if (_selectDestinationAction.getSelectedCard() instanceof MissionCard mission) {
                _destinationMission = mission;
            } else {
                _isFailed = true;
            }
        } else if (_selectDestinationAction.wasFailed()) {
            _isFailed = true;
        }
    }

    private void selectAffiliation(ST1EGame stGame) {
        Set<Affiliation> affiliationOptions = new HashSet<>();
        for (Affiliation affiliation : _cardEnteringPlay.getAffiliationOptions()) {
            boolean canSeedHereAsThisAffiliation = stGame.getRules().
                    isLocationValidPlayCardDestinationPerRules(stGame, _cardEnteringPlay,
                            _destinationMission.getGameLocation(stGame), PlayFacilityAction.class,
                            _cardEnteringPlay.getOwnerName(), List.of(affiliation));
            if (canSeedHereAsThisAffiliation)
                affiliationOptions.add(affiliation);
        }
        if (affiliationOptions.size() == 1) {
            _affiliationToPlayAs = Iterables.getOnlyElement(affiliationOptions);
        } else if (affiliationOptions.size() > 1) {
            if (_affiliationSelectionAction == null) {
                _affiliationSelectionAction =
                        new SelectAffiliationAction(stGame, _performingPlayerName, affiliationOptions);
                stGame.addActionToStack(_affiliationSelectionAction);
            } else if (_affiliationSelectionAction.wasSuccessful()) {
                _affiliationToPlayAs = _affiliationSelectionAction.getSelectedAffiliation();
            } else if (_affiliationSelectionAction.wasFailed()) {
                _isFailed = true;
            }
        } else {
            _isFailed = true;
        }
    }


    @Override
    public boolean isResolved() {
        return _destinationMission != null && _affiliationToPlayAs != null;
    }

    @Override
    public boolean cannotBeResolved(DefaultGame cardGame) {
        return _destinationMap.isEmpty() || _isFailed;
    }

    public MissionCard getDestinationMission() {
        return _destinationMission;
    }

    public Affiliation getAffiliationToPlayAs() {
        return _affiliationToPlayAs;
    }

    public void setDestination(MissionCard mission) {
        _destinationMission = mission;
    }

    public void setAffiliation(Affiliation affiliation) {
        _affiliationToPlayAs = affiliation;
    }

}