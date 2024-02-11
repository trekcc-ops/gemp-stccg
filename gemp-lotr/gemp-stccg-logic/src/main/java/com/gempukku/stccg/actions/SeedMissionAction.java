package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalMissionCard;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.common.filterable.Region;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.PlayOutDecisionEffect;
import com.gempukku.stccg.effects.choose.ChooseCardsOnTableEffect;
import com.gempukku.stccg.effects.defaulteffect.PlayMissionEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.rules.GameUtils;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class SeedMissionAction extends AbstractPlayCardAction {
    private PlayMissionEffect _playCardEffect;
    private final PhysicalMissionCard _cardToPlay;
    private boolean _cardPlayed;
    private int _locationZoneIndex;
    private final Zone _fromZone;
    private boolean _placementChosen;
    private boolean _directionChosen;
    private final ST1EGame _game;
    private PhysicalCard _neighborCard;

    public SeedMissionAction(ST1EGame game, PhysicalMissionCard cardToPlay) {
        super(cardToPlay);
        _cardToPlay = cardToPlay;
        _fromZone = cardToPlay.getZone();
        setText("Seed " + _cardToPlay.getFullName());
        setPerformingPlayer(_cardToPlay.getOwnerName());
        _game = game;
    }

    @Override
    public PhysicalMissionCard getPlayedCard() { return _cardToPlay; }
    
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
                if (gameState.getSpacelineLocationsSize() == 0) {
                    _placementChosen = true;
                    _directionChosen = true;
                    _locationZoneIndex = 0;
                } else {
                    appendCost(new PlayOutDecisionEffect(game, playerId,
                            new MultipleChoiceAwaitingDecision(1, "Add new quadrant to which end of the table?", directions) {
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
                _locationZoneIndex = gameState.indexOfLocation(missionLocation, quadrant);
                _placementChosen = true;
                _directionChosen = true;
            } else if (gameState.firstInRegion(region, quadrant) != null) {
                appendCost(new PlayOutDecisionEffect(game, playerId,
                        new MultipleChoiceAwaitingDecision(1, "Insert on which end of the region?", directions) {
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
            } else if (_cardToPlay.canInsertIntoSpaceline() && gameState.getQuadrantLocationsSize(quadrant) >= 2) {
                // TODO: canInsertIntoSpaceline method not defined
                Set<PhysicalCard> otherCards = _game.getGameState().getQuadrantLocationCards(quadrant);
                appendCost(new ChooseCardsOnTableEffect(_game, _thisAction, getPerformingPlayer(), "Choose a location to seed " + GameUtils.getCardLink(_cardToPlay) + " next to", otherCards) {
                    @Override
                    protected void cardsSelected(Collection<PhysicalCard> selectedCards) {
                        assert selectedCards.size() == 1;
                        _neighborCard = Iterables.getOnlyElement(selectedCards);
                        _placementChosen = true;
                    }
                });
                return getNextCost();
            } else {
                appendCost(new PlayOutDecisionEffect(game, getPerformingPlayer(),
                        new MultipleChoiceAwaitingDecision(1, "Insert on which end of the quadrant?", directions) {
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
            appendCost(new PlayOutDecisionEffect(game, playerId,
                    new MultipleChoiceAwaitingDecision(1, "Insert on which side of " + _neighborCard.getTitle(), directions) {
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
            _playCardEffect = new PlayMissionEffect(_game, _fromZone, _cardToPlay, _locationZoneIndex, _sharedMission);
            return _playCardEffect;
        }
        return null;
    }

    public boolean wasCarriedOut() {
        return _cardPlayed && _playCardEffect.wasCarriedOut();
    }
}