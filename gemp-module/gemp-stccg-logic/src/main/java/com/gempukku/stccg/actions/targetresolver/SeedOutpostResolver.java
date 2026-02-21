package com.gempukku.stccg.actions.targetresolver;

import com.gempukku.stccg.actions.choose.SelectAffiliationAction;
import com.gempukku.stccg.actions.choose.SelectCardAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SeedOutpostResolver implements ActionTargetResolver {

    private final FacilityCard _cardEnteringPlay;
    private Affiliation _affiliationToPlayAs;
    private final String _performingPlayerName;
    private SelectCardAction _selectDestinationAction;
    private MissionCard _destinationMission;
    private boolean _isFailed;
    private SelectAffiliationAction _affiliationSelectionAction;
    private final Map<PhysicalCard, List<Affiliation>> _destinationMap;

    public SeedOutpostResolver(FacilityCard cardToSeed, Map<PhysicalCard, List<Affiliation>> destinationMap) {
        _destinationMap = destinationMap;
        _cardEnteringPlay = cardToSeed;
        _performingPlayerName = cardToSeed.getOwnerName();
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
            Set<PhysicalCard> availableMissions = _destinationMap.keySet();
            _selectDestinationAction = new SelectVisibleCardAction(stGame, _performingPlayerName,
                    "Choose a destination for " + _cardEnteringPlay.getTitle(), availableMissions);
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
        List<Affiliation> affiliationOptions = _destinationMap.get(_destinationMission);
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
        // TODO add logic for an empty map, no valid targets
        return _isFailed;
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