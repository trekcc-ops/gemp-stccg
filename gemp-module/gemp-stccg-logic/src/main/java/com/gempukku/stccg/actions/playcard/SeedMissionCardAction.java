package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.choose.MakeDecisionAction;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.common.filterable.Region;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;
import java.util.Objects;

public class SeedMissionCardAction extends PlayCardAction {
        // TODO - Extend STCCGPlayCardAction

    private boolean _cardPlayed;
    private boolean _placementChosen;
    private int _locationZoneIndex;
    private final boolean _sharedMission;
    private final String _missionLocation;
    private boolean _actionCarriedOut;

    public SeedMissionCardAction(MissionCard cardToPlay) {
        super(cardToPlay, cardToPlay, cardToPlay.getOwner(), Zone.SPACELINE, ActionType.SEED_CARD);
        setText("Seed " + _cardEnteringPlay.getFullName());
        Quadrant quadrant = _cardEnteringPlay.getBlueprint().getQuadrant();
        _missionLocation = _cardEnteringPlay.getBlueprint().getLocation();
        ST1EGameState gameState = cardToPlay.getGame().getGameState();
        _sharedMission = gameState.indexOfLocation(_missionLocation, quadrant) != null &&
                !_cardEnteringPlay.getBlueprint().isUniversal();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        Quadrant quadrant = _cardEnteringPlay.getBlueprint().getQuadrant();
        ST1EGameState gameState = ((ST1EGame) cardGame).getGameState();
        Region region = _cardEnteringPlay.getBlueprint().getRegion();
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        String[] directions = {"LEFT", "RIGHT"};

        if (!_placementChosen) {
            if (!gameState.hasLocationsInQuadrant(quadrant)) {
                if (gameState.getSpacelineLocationsSize() == 0) {
                    _placementChosen = true;
                    _locationZoneIndex = 0;
                } else {
                    appendCost(new MakeDecisionAction(cardGame, performingPlayer,
                            "Add new quadrant to which end of the table?") {

                        @Override
                        protected AwaitingDecision getDecision(DefaultGame cardGame) {
                            return new MultipleChoiceAwaitingDecision(performingPlayer,
                                    "Add new quadrant to which end of the table?", directions, cardGame) {
                                @Override
                                protected void validDecisionMade(int index, String result) {
                                    _placementChosen = true;
                                    if (Objects.equals(result, "LEFT")) {
                                        _locationZoneIndex = 0;
                                    } else {
                                        _locationZoneIndex = gameState.getSpacelineLocationsSize();
                                    }
                                }
                            };
                        }
                    });
                    return getNextCost();
                }
            } else if (_sharedMission) {
                _locationZoneIndex = gameState.indexOfLocation(_missionLocation, quadrant);
                _placementChosen = true;
            } else if (gameState.firstInRegion(region, quadrant) != null) {
                appendCost(new MakeDecisionAction(cardGame, performingPlayer,
                        "Insert on which end of the region?") {
                    @Override
                    protected AwaitingDecision getDecision(DefaultGame cardGame) {
                        return new MultipleChoiceAwaitingDecision(performingPlayer,
                                "Insert on which end of the region?", directions, cardGame) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                _placementChosen = true;
                                if (Objects.equals(result, "LEFT")) {
                                    _locationZoneIndex = gameState.firstInRegion(region, quadrant);
                                } else {
                                    _locationZoneIndex = gameState.lastInRegion(region, quadrant) + 1;
                                }
                            }
                        };
                    }
                });
                return getNextCost();
            } else if (_cardEnteringPlay.canInsertIntoSpaceline() && gameState.getQuadrantLocationsSize(quadrant) >= 2) {
                // TODO: canInsertIntoSpaceline method not defined
                throw new InvalidGameLogicException("No method defined for cards to insert into spaceline");
            } else {
                appendCost(new MakeDecisionAction(cardGame, performingPlayer,
                        "Insert on which end of the quadrant?") {
                    @Override
                    protected AwaitingDecision getDecision(DefaultGame cardGame) {
                        return new MultipleChoiceAwaitingDecision(performingPlayer,
                                "Insert on which end of the quadrant?", directions, cardGame) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                _placementChosen = true;
                                if (Objects.equals(result, "LEFT")) {
                                    _locationZoneIndex = gameState.firstInQuadrant(quadrant);
                                } else {
                                    _locationZoneIndex = gameState.lastInQuadrant(quadrant) + 1;
                                }
                            }
                        };
                    }
                });
                return getNextCost();
            }
        }

        if (!_cardPlayed) {
            seedCard(cardGame);
        }
        return getNextAction();
    }

    private void seedCard(DefaultGame game) {
        if (game.getGameState() instanceof ST1EGameState gameState && _cardEnteringPlay instanceof MissionCard mission) {

            gameState.removeCardsFromZoneWithoutSendingToClient(game, List.of(_cardEnteringPlay));

            try {
                if (_sharedMission)
                    gameState.addMissionCardToSharedMission(mission, _locationZoneIndex);
                else
                    gameState.addMissionLocationToSpaceline(mission, _locationZoneIndex);
                game.getActionsEnvironment().emitEffectResult(
                        new PlayCardResult(this, _cardEnteringPlay));
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
}