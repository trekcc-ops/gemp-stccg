package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.common.filterable.Region;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.PlayOutDecisionEffect;
import com.gempukku.stccg.effects.defaulteffect.PlayMissionEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Objects;

public class PlayMissionAction extends AbstractPlayCardAction {
    private PlayMissionEffect _playCardEffect;
    private boolean _cardPlayed;
    private int _locationZoneIndex;
    private final Zone _fromZone;
    private boolean _placementChosen;
    private final ST1EGame _game;

    public PlayMissionAction(ST1EGame game, PhysicalCard missionPlayed) {
        super(missionPlayed, missionPlayed);
        setText("Play " + GameUtils.getFullName(_cardToPlay));
        setPerformingPlayer(_cardToPlay.getOwner());
        _fromZone = _cardToPlay.getZone();
        _game = game;
    }
    
    public ActionType getActionType() { return ActionType.PLAY_CARD; }
    
    @Override
    public Effect nextEffect(DefaultGame game) {
        Quadrant quadrant = _cardToPlay.getBlueprint().getQuadrant();
        String missionLocation = _cardToPlay.getBlueprint().getLocation();
        Region region = _cardToPlay.getBlueprint().getRegion();
        String playerId = getPerformingPlayer();
        ST1EGameState gameState = _game.getGameState();

        boolean _sharedMission = gameState.indexOfLocation(missionLocation, quadrant) != null &&
                !_cardToPlay.getBlueprint().isUniversal();
        String[] directions = {"LEFT", "RIGHT"};

        if (!_placementChosen) {
            if (!gameState.hasLocationsInQuadrant(quadrant)) {
                appendCost(new PlayOutDecisionEffect(game, playerId,
                        new MultipleChoiceAwaitingDecision(1, "Add new quadrant to which end of the table?", directions) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                _placementChosen = true;
                                if (Objects.equals(result, "LEFT")) {
                                    _locationZoneIndex = 0;
                                } else {
                                    _locationZoneIndex = gameState.getSpacelineLocationsSize();
                                }
                            }
                        }));
                return getNextCost();
            } else if (_sharedMission) {
                _locationZoneIndex = gameState.indexOfLocation(missionLocation, quadrant);
                _placementChosen = true;
            } else if (gameState.firstInRegion(region, quadrant) != null) {
                appendCost(new PlayOutDecisionEffect(game, playerId,
                        new MultipleChoiceAwaitingDecision(1, "Insert on which end of the region?", directions) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                _placementChosen = true;
                                if (Objects.equals(result, "LEFT")) {
                                    _locationZoneIndex = gameState.firstInRegion(region, quadrant);
                                } else {
                                    _locationZoneIndex = gameState.lastInRegion(region, quadrant);
                                }
                            }
                        }));
                return getNextCost();
            } else if (_cardToPlay.canInsertIntoSpaceline() && gameState.getQuadrantLocationsSize(quadrant) >= 2) {
                // TODO: canInsertIntoSpaceline method not defined
            } else {
                appendCost(new PlayOutDecisionEffect(game, getPerformingPlayer(),
                        new MultipleChoiceAwaitingDecision(1, "Insert on which end of the quadrant?", directions) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                _placementChosen = true;
                                if (Objects.equals(result, "LEFT")) {
                                    _locationZoneIndex = gameState.firstInQuadrant(quadrant);
                                } else {
                                    _locationZoneIndex = gameState.lastInQuadrant(quadrant);
                                }
                            }
                        }));
                return getNextCost();
            }
        }
        if (!_cardPlayed) {
            _cardPlayed = true;
            _playCardEffect = new PlayMissionEffect(_game, _fromZone, _cardToPlay, _locationZoneIndex, _sharedMission);
            return _playCardEffect;
        }
        return null;
    }

    public boolean wasCarriedOut() {
        return _cardPlayed && _playCardEffect.wasCarriedOut();
    }
}







    // TODO: Following player order, starting with first player

    /* The starting player draws the top mission from his or her pile and places it face up on the table,
        beginning the first spaceline. */


// TODO: Empok Nor can seed during mission phase at a universal mission
// TODO: Other actions may happen during mission phase (such as activating Save Stranded Crew text to download All-Consuming Evil)

// Code below borrowed from other process
/*        final List<Action> playableActions = game.getActionsEnvironment().getPhaseActions(_playerId);

        if (playableActions.size() == 0 && game.shouldAutoPass(_playerId, game.getGameState().getCurrentPhase())) {
            _nextProcess = new TribblesPlayerDrawsAndCanPlayProcess(_playerId);
        } else {
            String userMessage;
            if (playableActions.size() == 0) {
                userMessage = "No Tribbles can be played. Click 'Pass' to draw a card.";
            } else {
                userMessage = "Select Tribble to play or click 'Pass' to draw a card.";
            }
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardActionSelectionDecision(game, 1, userMessage, playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _nextProcess = new TribblesEndOfTurnGameProcess();
                                game.getActionsEnvironment().addActionToStack(action);
                            } else
                                _nextProcess = new TribblesPlayerDrawsAndCanPlayProcess(_playerId);
                        }
                    });
        }*/


// The second player then draws and places his or her first mission.
// A mission can be placed on either end of the appropriate spaceline.
// If it is the first mission in the quadrant, it is placed on a new spaceline, separate from the others.
        /* Cards that specify they are INSERTED into the spaceline may be placed anywhere in their native quadrant,
            including between two missions already seeded. */
// This continues until both players are finished.

// Cards that specify they are part of a region must be next to each other, forming a single, contiguous region within the quadrant.
// The first location in a region is placed normally.
// Subsequent locations within that region may be inserted into the spaceline at either end of the region.

// If two players seed the same location in the same quadrant, it becomes a shared mission.
// The first version to appear is seeded normally, but the second version is placed on top of the original, wherever it is on the spaceline, leaving half of the original exposed.
// The two missions form only one location and may be completed only once.
// Each player uses their own mission card for gameplay purposes; players may not use the "opponent's end" of their opponent's mission card at a shared mission.

// Missions with the Universal symbol may seed multiple times as multiple locations.
