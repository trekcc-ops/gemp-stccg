package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.choose.SelectMissionSeedIndexAction;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.common.filterable.Region;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class SeedMissionCardAction extends PlayCardAction {
        // TODO - Extend STCCGPlayCardAction

    private boolean _cardPlayed;
    private boolean _placementChosen;
    private boolean _placementDecisionSent;
    private SelectMissionSeedIndexAction _placementSelectionAction;
    private int _locationZoneIndex;
    private final boolean _sharedMission;
    private final String _missionLocation;
    private boolean _actionCarriedOut;

    public SeedMissionCardAction(ST1EGame cardGame, MissionCard cardToPlay) {
        super(cardGame, cardToPlay, cardToPlay, cardToPlay.getOwnerName(), Zone.SPACELINE, ActionType.SEED_CARD);
        Quadrant quadrant = _cardEnteringPlay.getBlueprint().getQuadrant();
        _missionLocation = _cardEnteringPlay.getBlueprint().getLocation();
        ST1EGameState gameState = cardGame.getGameState();
        _sharedMission = gameState.indexOfLocation(_missionLocation, quadrant) != null &&
                !_cardEnteringPlay.getBlueprint().isUniversal();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        Quadrant quadrant = _cardEnteringPlay.getBlueprint().getQuadrant();
        ST1EGameState gameState = ((ST1EGame) cardGame).getGameState();
        Region region = _cardEnteringPlay.getBlueprint().getRegion();
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        List<Integer> possibleIndices = new ArrayList<>();

        if (!_placementChosen) {
            if (!_placementDecisionSent) {
                List<MissionLocation> spacelineLocations = gameState.getSpacelineLocations();
                if (_sharedMission) {
                    possibleIndices.add(gameState.indexOfLocation(_missionLocation, quadrant));
                } else if (spacelineLocations.isEmpty()) {
                    possibleIndices.add(0);
                } else if (!gameState.hasLocationsInQuadrant(quadrant)) {
                    possibleIndices.add(0);
                    possibleIndices.add(spacelineLocations.size());
/*                    for (MissionLocation location : spacelineLocations) {
                        int locationIndex = spacelineLocations.indexOf(location);
                        if (locationIndex < spacelineLocations.size() - 1) {
                            MissionLocation nextLocation = spacelineLocations.get(locationIndex + 1);
                            if (location.getQuadrant() != nextLocation.getQuadrant()) {
                                possibleIndices.add(locationIndex + 1);
                            }
                        }
                    } */
                } else if (gameState.firstInRegion(region, quadrant) != null) {
                    possibleIndices.add(gameState.firstInRegion(region, quadrant));
                    possibleIndices.add(gameState.lastInRegion(region, quadrant) + 1);
                } else {
                    possibleIndices.add(gameState.firstInQuadrant(quadrant));
                    possibleIndices.add(gameState.lastInQuadrant(quadrant) + 1);
                }
                if (possibleIndices.size() == 1) {
                    _locationZoneIndex = possibleIndices.getFirst();
                    _placementChosen = true;
                } else {
                    _placementSelectionAction =
                            new SelectMissionSeedIndexAction(cardGame, performingPlayer, possibleIndices);
                    _placementDecisionSent = true;
                    return _placementSelectionAction;
                }
            } else {
                try {
                    _locationZoneIndex = _placementSelectionAction.getSelectedIndex();
                    _placementChosen = true;
                } catch(DecisionResultInvalidException exp) {
                    throw new InvalidGameLogicException(exp.getMessage());
                }
            }
        }

        if (!_cardPlayed) {
            seedCard(cardGame);
        }
        return getNextAction();
    }

    public void seedCard(DefaultGame game) {
        if (game.getGameState() instanceof ST1EGameState gameState && _cardEnteringPlay instanceof MissionCard mission) {

            gameState.removeCardsFromZoneWithoutSendingToClient(game, List.of(_cardEnteringPlay));

            try {
                if (_sharedMission)
                    gameState.addMissionCardToSharedMission(mission, _locationZoneIndex);
                else
                    gameState.addMissionLocationToSpaceline(mission, _locationZoneIndex);
                saveResult(new PlayCardResult(this, _cardEnteringPlay));
                _actionCarriedOut = true;
                _cardPlayed = true;
                setAsSuccessful();
            } catch (InvalidGameLogicException exp) {
                game.sendErrorMessage(exp);
            }
        } else {
            setAsFailed();
            _actionCarriedOut = false;
            game.sendErrorMessage("Seed mission action attempted in a non-1E game");
        }
    }

    public boolean wasCarriedOut() {
        return _cardPlayed && _actionCarriedOut;
    }

    public void setLocationZoneIndex(int indexNum) {
        _locationZoneIndex = indexNum;
    }
}