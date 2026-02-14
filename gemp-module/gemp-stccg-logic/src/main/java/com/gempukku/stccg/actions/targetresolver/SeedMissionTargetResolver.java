package com.gempukku.stccg.actions.targetresolver;

import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.common.filterable.Region;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SeedMissionTargetResolver implements ActionTargetResolver {

    private Integer _locationZoneIndex;
    private final MissionCard _cardEnteringPlay;
    private final String _performingPlayerName;
    private boolean _isFailed;
    private AwaitingDecision _decision;

    public SeedMissionTargetResolver(MissionCard cardEnteringPlay, Integer locationZoneIndex) {
        _cardEnteringPlay = cardEnteringPlay;
        _performingPlayerName = cardEnteringPlay.getOwnerName();
        _locationZoneIndex = locationZoneIndex;
    }

    public SeedMissionTargetResolver(MissionCard cardEnteringPlay) {
        _cardEnteringPlay = cardEnteringPlay;
        _performingPlayerName = cardEnteringPlay.getOwnerName();
    }
    @Override
    public void resolve(DefaultGame cardGame) throws InvalidGameLogicException {
        if (cardGame instanceof ST1EGame stGame) {
            if (_locationZoneIndex == null) {
                if (_decision == null) {
                    Quadrant quadrant = _cardEnteringPlay.getBlueprint().getQuadrant();
                    ST1EGameState gameState = stGame.getGameState();
                    Region region = _cardEnteringPlay.getBlueprint().getRegion();
                    List<Integer> possibleIndices = new ArrayList<>();
                    String missionLocationName = _cardEnteringPlay.getBlueprint().getLocation();
                    boolean sharedMission = gameState.indexOfLocation(missionLocationName, quadrant) != null &&
                            !_cardEnteringPlay.getBlueprint().isUniversal();

                    List<GameLocation> spacelineLocations = gameState.getOrderedSpacelineLocations();
                    if (sharedMission) {
                        possibleIndices.add(gameState.indexOfLocation(missionLocationName, quadrant));
                    } else if (spacelineLocations.isEmpty()) {
                        possibleIndices.add(0);
                    } else if (!gameState.hasLocationsInQuadrant(quadrant)) {
                        possibleIndices.add(0);
                        possibleIndices.add(spacelineLocations.size());
                    } else if (gameState.firstInRegion(region, quadrant) != null) {
                        possibleIndices.add(gameState.firstInRegion(region, quadrant));
                        possibleIndices.add(gameState.lastInRegion(region, quadrant) + 1);
                    } else {
                        possibleIndices.add(gameState.firstInQuadrant(quadrant));
                        possibleIndices.add(gameState.lastInQuadrant(quadrant) + 1);
                    }
                    if (possibleIndices.size() == 1) {
                        _locationZoneIndex = possibleIndices.getFirst();
                    } else {
                        List<String> optionsToPass = new ArrayList<>();
                        for (Integer option : possibleIndices) {
                            if (option != null) {
                                optionsToPass.add(String.valueOf(option));
                            }
                        }
                        _decision = new MultipleChoiceAwaitingDecision(_performingPlayerName,
                                optionsToPass, cardGame, DecisionContext.SEED_MISSION_INDEX_SELECTION) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                _locationZoneIndex = Integer.valueOf(result);
                            }
                        };
                        cardGame.sendAwaitingDecision(_decision);
                    }
                } else  {
                    // Should never reach this code if the decision was sent to the client
                    _isFailed = true;
                }
            } else {
                _isFailed = true;
            }
        }
    }

    @Override
    public boolean isResolved() {
        return _locationZoneIndex != null;
    }

    @Override
    public boolean cannotBeResolved(DefaultGame cardGame) {
        return _isFailed;
    }

    public int getLocationZoneIndex() {
        return Objects.requireNonNullElse(_locationZoneIndex, -999);
    }
}