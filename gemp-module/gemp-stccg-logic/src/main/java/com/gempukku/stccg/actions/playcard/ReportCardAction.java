package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.choose.ChooseAffiliationEffect;
import com.gempukku.stccg.actions.choose.ChooseCardsOnTableEffect;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ReportCardAction extends STCCGPlayCardAction {
    private FacilityCard _reportingDestination;
    private boolean _cardPlayed;
    private boolean _destinationOptionsIdentified;
    private Collection<PhysicalCard> _destinationOptions;
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
        if (cardToPlay.isMultiAffiliation()) {
            _affiliationWasChosen = false;
        } else {
            _affiliationWasChosen = true;
            _selectedAffiliation = cardToPlay.getAffiliation();
        }
    }

    public ReportCardAction(PhysicalReportableCard1E cardToPlay, boolean forFree,
                            Collection<PhysicalCard> destinationOptions) {
        this(cardToPlay, forFree);
        _destinationOptionsIdentified = true;
        _destinationOptions = destinationOptions;

    }


    protected Collection<PhysicalCard> getDestinationOptions(DefaultGame game) throws InvalidGameLogicException {
        if (game.getGameState() instanceof ST1EGameState gameState) {
            if (_destinationOptionsIdentified)
                return _destinationOptions;
            else {
                Collection<PhysicalCard> availableFacilities = new HashSet<>();
                for (ST1ELocation location : gameState.getSpacelineLocations()) {
                    Collection<PhysicalCard> facilities =
                            Filters.filterActive(game, FacilityType.OUTPOST, Filters.atLocation(location));
                    for (PhysicalCard card : facilities) {
                        if (card instanceof FacilityCard facility && _cardEnteringPlay.canReportToFacility(facility))
                            availableFacilities.add(facility);
                    }
                }
                return availableFacilities;
            }
        } else {
            throw new InvalidGameLogicException("Tried to process a report card action in a non-1E game");
        }
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        boolean result;
        try {
            Collection<PhysicalCard> destinationOptions = getDestinationOptions(cardGame);
            result = _cardEnteringPlay.canBePlayed(cardGame) && !destinationOptions.isEmpty();
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            result = false;
        }
        return result;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        DefaultGame game = _cardEnteringPlay.getGame();
        Player performingPlayer = game.getPlayer(_performingPlayerId);

        if (!_destinationChosen) {
            appendCost(new ChooseCardsOnTableEffect(
                    this, performingPlayer,
                    "Choose a facility to report " + _cardEnteringPlay.getCardLink() + " to",
                    getDestinationOptions(cardGame)
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
            appendCost(new ChooseAffiliationEffect(performingPlayer, new ArrayList<>(_affiliationOptions)) {
                @Override
                protected void affiliationChosen(Affiliation affiliation) {
                    _affiliationWasChosen = true;
                    _selectedAffiliation = affiliation;
                }
            });
            return getNextCost();
        }
        if (!_cardPlayed) {
            _cardEnteringPlay.changeAffiliation(_selectedAffiliation);
            _cardPlayed = true;
            GameState gameState = cardGame.getGameState();

            cardGame.sendMessage(_cardEnteringPlay.getOwnerName() + " played " + _cardEnteringPlay.getCardLink());

            gameState.removeCardFromZone(_cardEnteringPlay);
            cardGame.getGameState().getPlayer(_cardEnteringPlay.getOwnerName()).addPlayedAffiliation(_cardEnteringPlay.getAffiliation());
            _cardEnteringPlay.reportToFacility(_reportingDestination);
            cardGame.getActionsEnvironment().emitEffectResult(
                    new PlayCardResult(this, _fromZone, _cardEnteringPlay));
        }

        return getNextAction();
    }

    public PhysicalCard getCardReporting() { return _cardEnteringPlay; }

    public void setDestination(FacilityCard card) {
        _destinationChosen = true;
        _reportingDestination = card;
    }

}