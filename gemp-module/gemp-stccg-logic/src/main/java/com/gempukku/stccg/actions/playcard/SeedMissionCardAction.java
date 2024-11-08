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
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class SeedMissionCardAction extends PlayCardAction {
        // TODO - Extend STCCGPlayCardAction
    private final MissionCard _cardEnteringPlay;
    private boolean _cardPlayed, _placementChosen, _directionChosen;
    private int _locationZoneIndex;
    private final boolean _sharedMission;
    private PhysicalCard _neighborCard;
    private final String _missionLocation;

    public SeedMissionCardAction(MissionCard cardToPlay) {
        super(cardToPlay, cardToPlay, cardToPlay.getOwnerName(), Zone.SPACELINE, ActionType.SEED_CARD);
        _cardEnteringPlay = cardToPlay;
        setText("Seed " + _cardEnteringPlay.getFullName());
        Quadrant quadrant = _cardEnteringPlay.getBlueprint().getQuadrant();
        _missionLocation = _cardEnteringPlay.getBlueprint().getLocation();
        ST1EGameState gameState = cardToPlay.getGame().getGameState();
        _sharedMission = gameState.indexOfLocation(_missionLocation, quadrant) != null &&
                !_cardEnteringPlay.getBlueprint().isUniversal();
    }

    @Override
    public ST1EGame getGame() { return _cardEnteringPlay.getGame(); }

    @Override
    public Effect nextEffect() {
        Quadrant quadrant = _cardEnteringPlay.getBlueprint().getQuadrant();
        ST1EGameState gameState = _cardEnteringPlay.getGame().getGameState();
        Region region = _cardEnteringPlay.getBlueprint().getRegion();
        String playerId = getPerformingPlayerId();
        String[] directions = {"LEFT", "RIGHT"};

        if (!_placementChosen) {
            if (!gameState.hasLocationsInQuadrant(quadrant)) {
                if (gameState.getSpacelineLocationsSize() == 0) {
                    _placementChosen = true;
                    _directionChosen = true;
                    _locationZoneIndex = 0;
                } else {
                    appendCost(new PlayOutDecisionEffect(
                            new MultipleChoiceAwaitingDecision(_game.getPlayer(playerId),
                                    "Add new quadrant to which end of the table?", directions) {
                                @Override
                                protected void validDecisionMade(int index, String result) {
                                    _placementChosen = true;
                                    _directionChosen = true;
                                    if (Objects.equals(result, "LEFT")) {
                                        _locationZoneIndex = 0;
                                    } else {
                                        _locationZoneIndex = gameState.getSpacelineLocationsSize();
                                    }
                                }
                            }));
                    return getNextCost();
                }
            } else if (_sharedMission) {
                _locationZoneIndex = gameState.indexOfLocation(_missionLocation, quadrant);
                _placementChosen = true;
                _directionChosen = true;
            } else if (gameState.firstInRegion(region, quadrant) != null) {
                appendCost(new PlayOutDecisionEffect(
                        new MultipleChoiceAwaitingDecision(_game.getPlayer(playerId),
                                "Insert on which end of the region?", directions) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                _placementChosen = true;
                                _directionChosen = true;
                                if (Objects.equals(result, "LEFT")) {
                                    _locationZoneIndex = gameState.firstInRegion(region, quadrant);
                                } else {
                                    _locationZoneIndex = gameState.lastInRegion(region, quadrant) + 1;
                                }
                            }
                        }));
                return getNextCost();
            } else if (_cardEnteringPlay.canInsertIntoSpaceline() && gameState.getQuadrantLocationsSize(quadrant) >= 2) {
                // TODO: canInsertIntoSpaceline method not defined
                Set<PhysicalCard> otherCards = gameState.getQuadrantLocationCards(quadrant);
                appendCost(new ChooseCardsOnTableEffect(this, getPerformingPlayerId(),
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
                appendCost(new PlayOutDecisionEffect(
                        new MultipleChoiceAwaitingDecision(_game.getPlayer(getPerformingPlayerId()),
                                "Insert on which end of the quadrant?", directions) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                _placementChosen = true;
                                _directionChosen = true;
                                if (Objects.equals(result, "LEFT")) {
                                    _locationZoneIndex = gameState.firstInQuadrant(quadrant);
                                } else {
                                    _locationZoneIndex = gameState.lastInQuadrant(quadrant) + 1;
                                }
                            }
                        }));
                return getNextCost();
            }
        }
        if (!_directionChosen) {
            appendCost(new PlayOutDecisionEffect(
                    new MultipleChoiceAwaitingDecision(_game.getPlayer(playerId),
                            "Insert on which side of " + _neighborCard.getTitle(), directions) {
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

    protected Effect getFinalEffect() { return new SeedMissionCardEffect(_performingPlayerId, _cardEnteringPlay,
            _locationZoneIndex, _sharedMission, this); }

    public boolean wasCarriedOut() {
        return _cardPlayed && _finalEffect.wasCarriedOut();
    }
}