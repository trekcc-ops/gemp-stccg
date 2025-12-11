package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import java.util.Objects;

public class SeedMissionCardAction extends PlayCardAction {
    private boolean _placementChosen;
    private boolean _placementDecisionSent;
    private SelectMissionSeedIndexAction _placementSelectionAction;
    private int _locationZoneIndex;

    @JsonCreator
    @SuppressWarnings("unused")
    private SeedMissionCardAction(@JsonProperty("actionId") int actionId,
                           @JsonProperty("targetCardId") @JsonIdentityReference(alwaysAsId=true)
                           MissionCard cardEnteringPlay,
                          @JsonProperty("performingCardId") @JsonIdentityReference(alwaysAsId=true)
                          MissionCard performingCard,
                           @JsonProperty("performingPlayerId") String performingPlayerName,
                           @JsonProperty("destinationZone") Zone destinationZone) {
        super(actionId, performingCard, cardEnteringPlay, performingPlayerName, destinationZone, ActionType.SEED_CARD);
    }


    public SeedMissionCardAction(DefaultGame cardGame, MissionCard cardToPlay) {
        super(cardGame, cardToPlay, cardToPlay, cardToPlay.getOwnerName(), Zone.SPACELINE, ActionType.SEED_CARD);
    }

    public SeedMissionCardAction(ST1EGame cardGame, MissionCard cardToPlay, int locationZoneIndex) {
        this(cardGame, cardToPlay);
        _placementChosen = true;
        _locationZoneIndex = locationZoneIndex;
    }


    @Override
    public void continueInitiation(DefaultGame cardGame) throws PlayerNotFoundException, InvalidGameLogicException {
        Quadrant quadrant = _cardEnteringPlay.getBlueprint().getQuadrant();
        ST1EGameState gameState = ((ST1EGame) cardGame).getGameState();
        Region region = _cardEnteringPlay.getBlueprint().getRegion();
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        List<Integer> possibleIndices = new ArrayList<>();
        String missionLocationName = _cardEnteringPlay.getBlueprint().getLocation();
        boolean sharedMission = gameState.indexOfLocation(missionLocationName, quadrant) != null &&
                !_cardEnteringPlay.getBlueprint().isUniversal();


        if (!_placementChosen) {
            if (!_placementDecisionSent) {
                List<MissionLocation> spacelineLocations = gameState.getSpacelineLocations();
                if (sharedMission) {
                    possibleIndices.add(gameState.indexOfLocation(missionLocationName, quadrant));
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
                    cardGame.addActionToStack(_placementSelectionAction);
                }
            } else {
                try {
                    _locationZoneIndex = _placementSelectionAction.getSelectedIndex();
                    _placementChosen = true;
                } catch(DecisionResultInvalidException exp) {
                    throw new InvalidGameLogicException(exp.getMessage());
                }
            }
        } else {
            setAsInitiated();
        }
    }

    public void processEffect(DefaultGame game) {
        Quadrant quadrant = _cardEnteringPlay.getBlueprint().getQuadrant();
        String missionLocationName = _cardEnteringPlay.getBlueprint().getLocation();

        if (game.getGameState() instanceof ST1EGameState gameState && _cardEnteringPlay instanceof MissionCard mission) {
            boolean sharedMission = gameState.indexOfLocation(missionLocationName, quadrant) != null &&
                    !_cardEnteringPlay.getBlueprint().isUniversal();

            gameState.removeCardsFromZoneWithoutSendingToClient(game, List.of(_cardEnteringPlay));
            List<MissionLocation> spaceline = gameState.getSpacelineLocations();

            try {
                if (sharedMission) {
                    MissionLocation location = spaceline.get(_locationZoneIndex);
                    List<MissionCard> missionsAtLocation = location.getMissionCards();
                    if (missionsAtLocation.size() != 1 ||
                            Objects.equals(missionsAtLocation.getFirst().getOwnerName(), mission.getOwnerName()))
                        throw new InvalidGameLogicException("Cannot seed " + mission.getTitle() + " because " +
                                mission.getOwnerName() + " already has a mission at " +
                                mission.getBlueprint().getLocation());
                    location.addMission(game, mission);
                    gameState.addCardToZone(game, mission, Zone.SPACELINE, _actionContext);
                }
                else {
                    int newLocationId = gameState.getNextLocationId();
                    MissionLocation location = new MissionLocation(game, mission, newLocationId);
                    gameState.addSpacelineLocation(_locationZoneIndex, location);
                    gameState.addCardToZone(game, mission, Zone.SPACELINE, _actionContext);
                }
                saveResult(new PlayCardResult(this, _cardEnteringPlay));
                setAsSuccessful();
            } catch (InvalidGameLogicException exp) {
                game.sendErrorMessage(exp);
            }
        } else {
            setAsFailed();
            game.sendErrorMessage("Seed mission action attempted in a non-1E game");
        }
    }

    public void setLocationZoneIndex(int indexNum) {
        _locationZoneIndex = indexNum;
    }
}