package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalMissionCard;
import com.gempukku.stccg.cards.PhysicalFacilityCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.choose.ChooseAffiliationEffect;
import com.gempukku.stccg.effects.choose.ChooseCardsOnTableEffect;
import com.gempukku.stccg.effects.defaulteffect.SeedOutpostEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.gempukku.stccg.rules.GameUtils;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SeedOutpostAction extends AbstractPlayCardAction {
    private SeedOutpostEffect _seedCardEffect;
    private final PhysicalFacilityCard _seedCard;
    private boolean _cardWasSeeded;
    private int _locationZoneIndex;
    private final Zone _fromZone;
    private boolean _placementChosen;
    private boolean _affiliationWasChosen;
    private final Set<Affiliation> _affiliationOptions = new HashSet<>();
    private Affiliation _selectedAffiliation;
    private final ST1EGame _game;
    public SeedOutpostAction(PhysicalFacilityCard cardToSeed) {
        super(cardToSeed);
        _seedCard = cardToSeed;
        setText("Seed " + _seedCard.getFullName());
        setPerformingPlayer(_seedCard.getOwnerName());
        _fromZone = _seedCard.getZone();
        _game = cardToSeed.getGame();
        _placementChosen = false;
        if (cardToSeed.isMultiAffiliation()) {
            _affiliationWasChosen = false;
        } else {
            _affiliationWasChosen = true;
            _selectedAffiliation = _seedCard.getCurrentAffiliation();
        }
    }

    @Override
    public PhysicalFacilityCard getPlayedCard() { return _seedCard; }
    
    public ActionType getActionType() { return ActionType.PLAY_CARD; }
    
    @Override
    public Effect nextEffect(DefaultGame game) {
            String playerId = getPerformingPlayer();
        ST1EGameState gameState = _game.getGameState();

        Set<PhysicalCard> availableMissions = new HashSet<>();
        for (ST1ELocation location : gameState.getSpacelineLocations()) {
            PhysicalMissionCard missionCard = location.getMissionForPlayer(playerId);
            if (_seedCard.canSeedAtMission(missionCard)) {
                availableMissions.add(missionCard);
            }
        }

        if (!_placementChosen) {
            appendCost(new ChooseCardsOnTableEffect(_game, _thisAction, getPerformingPlayer(), "Choose a mission to seed " + GameUtils.getCardLink(_seedCard) + " at", availableMissions) {
                @Override
                protected void cardsSelected(Collection<PhysicalCard> selectedCards) {
                    assert selectedCards.size() == 1;
                    PhysicalMissionCard selectedMission = (PhysicalMissionCard) Iterables.getOnlyElement(selectedCards);
                    _locationZoneIndex = selectedMission.getLocationZoneIndex();
                    _placementChosen = true;
                    if (!_affiliationWasChosen) {
                        for (Affiliation affiliation : _seedCard.getAffiliationOptions()) {
                            if (_seedCard.canSeedAtMissionAsAffiliation(selectedMission, affiliation))
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
            appendCost(new ChooseAffiliationEffect(_game, getPerformingPlayer(), new ArrayList<>(_affiliationOptions)) {
                @Override
                protected void affiliationChosen(Affiliation affiliation) {
                    _affiliationWasChosen = true;
                    _selectedAffiliation = affiliation;
                }
            });
        }
        if (!_cardWasSeeded) {
            _seedCard.setCurrentAffiliation(_selectedAffiliation);
            _cardWasSeeded = true;
            _seedCardEffect = new SeedOutpostEffect(_game, _fromZone, _seedCard, _locationZoneIndex);
            return _seedCardEffect;
        }
        return null;
    }

    public boolean wasCarriedOut() {
        return _cardWasSeeded && _seedCardEffect.wasCarriedOut();
    }
}