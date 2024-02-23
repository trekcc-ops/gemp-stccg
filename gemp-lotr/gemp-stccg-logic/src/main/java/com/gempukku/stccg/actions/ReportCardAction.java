package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalFacilityCard;
import com.gempukku.stccg.cards.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.choose.ChooseAffiliationEffect;
import com.gempukku.stccg.effects.choose.ChooseCardsOnTableEffect;
import com.gempukku.stccg.effects.defaulteffect.ReportCardEffect;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ReportCardAction extends AbstractPlayCardAction {
    private ReportCardEffect _playCardEffect;
    private final PhysicalReportableCard1E _cardToReport;
    private PhysicalFacilityCard _reportingDestination;
    private boolean _cardPlayed;
    private int _locationZoneIndex;
    private final Zone _fromZone;
    private final ST1EGame _game;
    private boolean _destinationChosen;
    private final Set<Affiliation> _affiliationOptions = new HashSet<>();
    private boolean _affiliationWasChosen;
    private Affiliation _selectedAffiliation;

    public ReportCardAction(PhysicalReportableCard1E cardToPlay) {
        super(cardToPlay);
        _cardToReport = cardToPlay;
        _fromZone = cardToPlay.getZone();
        setText("Play " + _cardToReport.getFullName());
        setPerformingPlayer(_cardToReport.getOwnerName());
        _game = cardToPlay.getGame();
        _destinationChosen = false;
        if (_cardToReport.isMultiAffiliation()) {
            _affiliationWasChosen = false;
        } else {
            _affiliationWasChosen = true;
            _selectedAffiliation = _cardToReport.getCurrentAffiliation();
        }
    }

    @Override
    public boolean canBeInitiated() {
        if (!_cardToReport.canBePlayed())
            return false;
        for (ST1ELocation location : _game.getGameState().getSpacelineLocations()) {
            for (PhysicalFacilityCard facility : location.getOutposts())
                if (_cardToReport.canReportToFacility(facility))
                    return true;
        }
        return false;
    }

    @Override
    public PhysicalReportableCard1E getPlayedCard() { return _cardToReport; }
    
    public ActionType getActionType() { return ActionType.PLAY_CARD; }
    
    @Override
    public Effect nextEffect() {
        // TODO - Add affiliation of personnel to affiliations player is playing
        String playerId = getPerformingPlayer();
        ST1EGameState gameState = _game.getGameState();

        Collection<PhysicalCard> availableFacilities = new HashSet<>();
        for (ST1ELocation location : gameState.getSpacelineLocations()) {
            for (PhysicalFacilityCard facility : location.getOutposts()) {
                if (_cardToReport.canReportToFacility(facility))
                    availableFacilities.add(facility);
            }
        }

        if (!_destinationChosen) {
            appendCost(new ChooseCardsOnTableEffect(
                    _thisAction, getPerformingPlayer(),
                    "Choose a facility to report " + _cardToReport.getCardLink() + " to", availableFacilities
            ) {
                @Override
                protected void cardsSelected(Collection<PhysicalCard> selectedCards) {
                    assert selectedCards.size() == 1;
                    PhysicalFacilityCard selectedFacility = (PhysicalFacilityCard) Iterables.getOnlyElement(selectedCards);
                    _reportingDestination = selectedFacility;
                    _destinationChosen = true;
                    if (!_affiliationWasChosen) {
                        for (Affiliation affiliation : _cardToReport.getAffiliationOptions()) {
                            if (_cardToReport.canReportToFacilityAsAffiliation(selectedFacility, affiliation))
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
        if (!_cardPlayed) {
            _cardToReport.setCurrentAffiliation(_selectedAffiliation);
            _cardPlayed = true;
            _playCardEffect = new ReportCardEffect(_game, _fromZone, _cardToReport, _reportingDestination);
            return _playCardEffect;
        }
        return null;

    }

    public boolean wasCarriedOut() {
        return _cardPlayed && _playCardEffect.wasCarriedOut();
    }

    @Override
    public ST1EGame getGame() { return _game; }
}