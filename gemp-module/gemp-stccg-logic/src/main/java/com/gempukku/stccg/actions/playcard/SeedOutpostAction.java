package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.choose.ChooseAffiliationEffect;
import com.gempukku.stccg.actions.choose.ChooseCardsOnTableEffect;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SeedOutpostAction extends PlayCardAction {
    private boolean _cardWasSeeded, _placementWasChosen, _affiliationWasChosen;
    private int _locationZoneIndex;
    private final Set<Affiliation> _affiliationOptions = new HashSet<>();
    private Affiliation _selectedAffiliation;
    private final FacilityCard _cardEnteringPlay;
    public SeedOutpostAction(FacilityCard cardToSeed) {
        super(cardToSeed, cardToSeed, cardToSeed.getOwnerName(), Zone.AT_LOCATION, ActionType.SEED_CARD);
        _cardEnteringPlay = cardToSeed;
        setText("Seed " + _cardEnteringPlay.getFullName());
        if (cardToSeed.isMultiAffiliation()) {
            _affiliationWasChosen = false;
        } else {
            _affiliationWasChosen = true;
            _selectedAffiliation = cardToSeed.getAffiliation();
        }
    }

    @Override
    protected Effect getFinalEffect() {
        return new SeedFacilityEffect(_performingPlayerId, _cardEnteringPlay, _locationZoneIndex, this);
    }

    @Override
    public Effect nextEffect(DefaultGame cardGame) throws InvalidGameLogicException {
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        ST1EGameState gameState = _cardEnteringPlay.getGame().getGameState();

        Set<PhysicalCard> availableMissions = new HashSet<>();
        for (ST1ELocation location : gameState.getSpacelineLocations()) {
            MissionCard missionCard = location.getMissionForPlayer(_performingPlayerId);
            if (_cardEnteringPlay.canSeedAtMission(missionCard)) {
                availableMissions.add(missionCard);
            }
        }

        if (!_placementWasChosen) {
            appendCost(new ChooseCardsOnTableEffect(this, performingPlayer,
                    "Choose a mission to seed " + _cardEnteringPlay.getCardLink() + " at", availableMissions) {
                @Override
                protected void cardsSelected(Collection<PhysicalCard> selectedCards) {
                    assert selectedCards.size() == 1;
                    MissionCard selectedMission = (MissionCard) Iterables.getOnlyElement(selectedCards);
                    _locationZoneIndex = selectedMission.getLocationZoneIndex();
                    _placementWasChosen = true;
                    if (!_affiliationWasChosen) {
                        for (Affiliation affiliation : _cardEnteringPlay.getAffiliationOptions()) {
                            if (_cardEnteringPlay.canSeedAtMissionAsAffiliation(selectedMission, affiliation))
                                _affiliationOptions.add(affiliation);
                        }
                        if (_affiliationOptions.size() == 1) {
                            _affiliationWasChosen = true;
                            _selectedAffiliation = Iterables.getOnlyElement(_affiliationOptions);
                        }
                    }
                }
            });
            return getNextCost();
        }
        if (!_affiliationWasChosen) {
            appendCost(new ChooseAffiliationEffect(performingPlayer, new ArrayList<>(_affiliationOptions)) {
                @Override
                protected void affiliationChosen(Affiliation affiliation) {
                    _affiliationWasChosen = true;
                    _selectedAffiliation = affiliation;
                }
            });
        }
        if (!_cardWasSeeded) {
            _cardEnteringPlay.changeAffiliation(_selectedAffiliation);
            _cardWasSeeded = true;
            _finalEffect = getFinalEffect();
            return _finalEffect;
        }
        return null;
    }

    public void setDestination(ST1ELocation location) {
        _locationZoneIndex = location.getLocationZoneIndex();
        _placementWasChosen = true;
    }

    public PhysicalCard getCardToSeed() { return _cardEnteringPlay; }
}