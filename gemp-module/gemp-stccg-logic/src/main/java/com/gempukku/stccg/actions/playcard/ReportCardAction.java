package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.choose.ChooseAffiliationEffect;
import com.gempukku.stccg.actions.choose.ChooseCardsOnTableEffect;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ReportCardAction extends STCCGPlayCardAction {
    private FacilityCard _reportingDestination;
    private boolean _cardPlayed;
    private final ST1EGame _game;
    private boolean _destinationChosen = false;
    private final Set<Affiliation> _affiliationOptions = new HashSet<>();
    private boolean _affiliationWasChosen;
    private Affiliation _selectedAffiliation;
    private final PhysicalReportableCard1E _cardEnteringPlay;

    public ReportCardAction(PhysicalReportableCard1E cardToPlay, boolean forFree) {
                    // TODO - Zone is null because these will be attached and the implementation is weird
        super(cardToPlay, null, cardToPlay.getOwner(), forFree);
        _cardEnteringPlay = cardToPlay;
        setText("Play " + _cardEnteringPlay.getFullName());
        _game = cardToPlay.getGame();
        if (cardToPlay.isMultiAffiliation()) {
            _affiliationWasChosen = false;
        } else {
            _affiliationWasChosen = true;
            _selectedAffiliation = cardToPlay.getAffiliation();
        }
    }

    protected Collection<PhysicalCard> getDestinationOptions() {
        Collection<PhysicalCard> availableFacilities = new HashSet<>();
        for (ST1ELocation location : _game.getGameState().getSpacelineLocations()) {
            Collection<PhysicalCard> facilities =
                    Filters.filterActive(_game, FacilityType.OUTPOST, Filters.atLocation(location));
            for (PhysicalCard card : facilities) {
                if (card instanceof FacilityCard facility && _cardEnteringPlay.canReportToFacility(facility))
                    availableFacilities.add(facility);
            }
        }
        return availableFacilities;
    }

    @Override
    public boolean canBeInitiated() {
        return _cardEnteringPlay.canBePlayed() && !getDestinationOptions().isEmpty() && costsCanBePaid();
    }

    @Override
    protected Effect getFinalEffect() {
        return new ReportCardEffect(_performingPlayerId, _fromZone, _cardEnteringPlay, _reportingDestination);
    }

    @Override    
    public Effect nextEffect() {

        if (!_destinationChosen) {
            appendCost(new ChooseCardsOnTableEffect(
                    _thisAction, getPerformingPlayerId(),
                    "Choose a facility to report " + _cardEnteringPlay.getCardLink() + " to",
                    getDestinationOptions()
            ) {
                @Override
                protected void cardsSelected(Collection<PhysicalCard> selectedCards) {
                    assert selectedCards.size() == 1;
                    FacilityCard selectedFacility =
                            (FacilityCard) Iterables.getOnlyElement(selectedCards);
                    _reportingDestination = selectedFacility;
                    _destinationChosen = true;
                    if (!_affiliationWasChosen) {
                        for (Affiliation affiliation : _cardEnteringPlay.getAffiliationOptions()) {
                            if (_cardEnteringPlay.canReportToFacilityAsAffiliation(selectedFacility, affiliation))
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
            appendCost(new ChooseAffiliationEffect(_game, getPerformingPlayerId(), new ArrayList<>(_affiliationOptions)) {
                @Override
                protected void affiliationChosen(Affiliation affiliation) {
                    _affiliationWasChosen = true;
                    _selectedAffiliation = affiliation;
                }
            });
            return getNextCost();
        }
        if (!_cardPlayed) {
            _cardEnteringPlay.setCurrentAffiliation(_selectedAffiliation);
            _cardPlayed = true;
            _finalEffect = getFinalEffect();
            return _finalEffect;
        }
        return null;

    }

    @Override
    public ST1EGame getGame() { return _game; }
}