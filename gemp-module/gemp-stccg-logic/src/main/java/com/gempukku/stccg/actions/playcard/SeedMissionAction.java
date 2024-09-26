package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.common.filterable.Region;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.PlayOutDecisionEffect;
import com.gempukku.stccg.actions.choose.ChooseCardsOnTableEffect;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class SeedMissionAction extends PlayCardAction {
        // TODO - Extend STCCGPlayCardAction
    private final MissionCard _cardEnteringPlay;
    private boolean _cardPlayed;
    private int _locationZoneIndex;
    private boolean _placementChosen;
    private boolean _directionChosen;
    private final ST1EGame _game;
    private final ST1EGameState _gameState;
    private final boolean _sharedMission;
    private PhysicalCard _neighborCard;
    private final Quadrant _quadrant;
    private final String _missionLocation;

    public SeedMissionAction(MissionCard cardToPlay) {
        super(cardToPlay, cardToPlay, cardToPlay.getOwnerName(), Zone.SPACELINE, ActionType.SEED_CARD);
        _cardEnteringPlay = cardToPlay;
        setText("Seed " + _cardEnteringPlay.getFullName());
        _game = cardToPlay.getGame();
        _quadrant = _cardEnteringPlay.getBlueprint().getQuadrant();
        _missionLocation = _cardEnteringPlay.getBlueprint().getLocation();
        _gameState = _game.getGameState();
        _sharedMission = _gameState.indexOfLocation(_missionLocation, _quadrant) != null &&
                !_cardEnteringPlay.getBlueprint().isUniversal();
    }

    @Override
    public ST1EGame getGame() { return _game; }

    @Override
    public Effect nextEffect() throws InvalidGameLogicException {
        Region region = _cardEnteringPlay.getBlueprint().getRegion();
        String playerId = getPerformingPlayerId();
        String[] directions = {"LEFT", "RIGHT"};

        if (!_placementChosen) {
            if (!_gameState.hasLocationsInQuadrant(_quadrant)) {
                if (_gameState.getSpacelineLocationsSize() == 0) {
                    _placementChosen = true;
                    _directionChosen = true;
                    _locationZoneIndex = 0;
                } else {
                    appendCost(new PlayOutDecisionEffect(_game, playerId,
                            new MultipleChoiceAwaitingDecision("Add new quadrant to which end of the table?", directions) {
                                @Override
                                protected void validDecisionMade(int index, String result) {
                                    _placementChosen = true;
                                    _directionChosen = true;
                                    if (Objects.equals(result, "LEFT")) {
                                        _locationZoneIndex = 0;
                                    } else {
                                        _locationZoneIndex = _gameState.getSpacelineLocationsSize();
                                    }
                                }
                            }));
                    return getNextCost();
                }
            } else if (_sharedMission) {
                _locationZoneIndex = _gameState.indexOfLocation(_missionLocation, _quadrant);
                _placementChosen = true;
                _directionChosen = true;
            } else if (_gameState.firstInRegion(region, _quadrant) != null) {
                appendCost(new PlayOutDecisionEffect(_game, playerId,
                        new MultipleChoiceAwaitingDecision("Insert on which end of the region?", directions) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                _placementChosen = true;
                                _directionChosen = true;
                                if (Objects.equals(result, "LEFT")) {
                                    _locationZoneIndex = _gameState.firstInRegion(region, _quadrant);
                                } else {
                                    _locationZoneIndex = _gameState.lastInRegion(region, _quadrant) + 1;
                                }
                            }
                        }));
                return getNextCost();
            } else if (_cardEnteringPlay.canInsertIntoSpaceline() && _gameState.getQuadrantLocationsSize(_quadrant) >= 2) {
                // TODO: canInsertIntoSpaceline method not defined
                Set<PhysicalCard> otherCards = _game.getGameState().getQuadrantLocationCards(_quadrant);
                appendCost(new ChooseCardsOnTableEffect(_thisAction, getPerformingPlayerId(),
                        "Choose a location to seed " + _cardEnteringPlay.getCardLink() + " next to", otherCards) {
                    @Override
                    protected void cardsSelected(Collection<PhysicalCard> selectedCards) {
                        assert selectedCards.size() == 1;
                        _neighborCard = Iterables.getOnlyElement(selectedCards);
                        _placementChosen = true;
                    }
                });
                return getNextCost();
            } else {
                appendCost(new PlayOutDecisionEffect(_game, getPerformingPlayerId(),
                        new MultipleChoiceAwaitingDecision("Insert on which end of the quadrant?", directions) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                _placementChosen = true;
                                _directionChosen = true;
                                if (Objects.equals(result, "LEFT")) {
                                    _locationZoneIndex = _gameState.firstInQuadrant(_quadrant);
                                } else {
                                    _locationZoneIndex = _gameState.lastInQuadrant(_quadrant) + 1;
                                }
                            }
                        }));
                return getNextCost();
            }
        }
        if (!_directionChosen) {
            appendCost(new PlayOutDecisionEffect(_game, playerId,
                    new MultipleChoiceAwaitingDecision("Insert on which side of " + _neighborCard.getTitle(), directions) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            _directionChosen = true;
                            if (Objects.equals(result, "LEFT")) {
                                _locationZoneIndex = _neighborCard.getLocationZoneIndex();
                            } else {
                                _locationZoneIndex = _neighborCard.getLocationZoneIndex() + 1;
                            }
                        }
                    }));
            return getNextCost();

        }
        if (!_cardPlayed) {
            _cardPlayed = true;
            _finalEffect = getFinalEffect();
            return _finalEffect;
        }
        return null;
    }

    protected Effect getFinalEffect() { return new SeedMissionEffect(_performingPlayerId, _cardEnteringPlay,
            _locationZoneIndex, _sharedMission); }

    public boolean wasCarriedOut() {
        return _cardPlayed && _finalEffect.wasCarriedOut();
    }
}