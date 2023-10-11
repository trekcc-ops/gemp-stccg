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