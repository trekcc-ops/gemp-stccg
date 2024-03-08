package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.playcard.PlayCardEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.ST1ELocation;

import java.util.Collection;
import java.util.LinkedList;

public abstract class ShipBattleAction extends AbstractCostToEffectAction {
        // TODO - For now, ignores affiliation attack restrictions, as well as tactics, as well as leadership requirements
        // TODO - Very much not complete
                // i.e. it is just an action to compare numbers

    protected final PhysicalCard _actionSource;
    private boolean _attackingForceSelected;
    private boolean _battleWasInitiated;
    private boolean _responsesCarriedOut;
    private boolean _tacticsSelected;
    private boolean _openedFire;
    private boolean _returnedFire;
    private boolean _winnerDetermined;
    private boolean _battleResolved;
    private boolean _actionWasInitiated = false, _cardWasRemoved = false, _cardHasEnteredPlay = false;
    protected boolean _reshuffle;
    protected String _text;
    private boolean _virtualCardAction;
    protected final PhysicalCard _cardEnteringPlay;
    protected final DefaultGame _game;
    protected final Zone _fromZone;
    protected final Zone _toZone;
    protected Effect _finalEffect;
    private ST1ELocation _location;
    private final Player _performingPlayer;

    /**
     * Creates an action for playing the specified card.
     * @param actionSource the card to initiate the deployment
     */
    public ShipBattleAction(PhysicalCard actionSource, PhysicalCard cardEnteringPlay, Player performingPlayer,
                            Zone toZone, ActionType actionType, ST1ELocation location) {
        super(performingPlayer.getPlayerId(), actionType);
        _actionSource = actionSource;
        _cardEnteringPlay = cardEnteringPlay;
        _game = actionSource.getGame();
        _fromZone = cardEnteringPlay.getZone();
        _toZone = toZone;
        _location = location;
        _performingPlayer = performingPlayer;
    }

/*    public ShipBattleAction(PhysicalCard card) {
        this(card, card, card.getOwner(), Zone.TABLE, ActionType.SHIP_BATTLE, card.getLocation());
    }*/

    @Override
    public boolean canBeInitiated() {
        if (!_cardEnteringPlay.canBePlayed())
            return false;
        return costsCanBePaid();
    }

    @Override
    public PhysicalCard getActionSource() {
        return _actionSource;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return getCardEnteringPlay();
    }

    public PhysicalCard getCardEnteringPlay() { return _cardEnteringPlay; }

    public String getText() {
        return _text;
    }

    /**
     * Sets the text shown for the action selection on the User Interface.
     * @param text the text to show for the action selection
     */
    public void setText(String text) {
        _text = text;
    }

    /**
     * Sets if the card pile the card is played from is reshuffled.
     * @param reshuffle true if pile the card is played from is reshuffled, otherwise false
     */
    public void setReshuffle(boolean reshuffle) {
        _reshuffle = reshuffle;
    }

    protected Effect getFinalEffect() { return new PlayCardEffect(_performingPlayerId, _fromZone, _cardEnteringPlay, _toZone); }
    private Collection<PhysicalCard> getAttackingCardsOptions() {
        Collection<PhysicalCard> options = new LinkedList<>();
            // TODO - Every ship needs a leader aboard
            // TODO - Each ship must have at least one matching, compatible personnel aboard
            // TODO - Attacking cards must be compatible
        return Filters.filterYourActive(_performingPlayer, Filters.ship, Filters.atLocation(_location));
                // at least one matching, compatible personnel aboard each
    }
/*
    private Effect selectAttackingForceEffect() {
        return new ChooseCardsOnTableEffect(
                _thisAction, getPerformingPlayerId(),
                "Choose cards to join attacking force",
                getAttackingCardsOptions()
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
        };
    } */

    public Effect nextEffect() {
/*
        if (!_attackingForceSelected) {
            appendCost(selectAttackingForceEffect());
            return getNextCost();
        }


        if (!_battleWasInitiated) {
            return new initiateBattleEffect();
        }
*/
        if (!_actionWasInitiated) {
            _actionWasInitiated = true;
        }

        Effect cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_cardWasRemoved) {
            _cardWasRemoved = true;
            _game.sendMessage(_cardEnteringPlay.getOwnerName() + " plays " +
                    _cardEnteringPlay.getCardLink() +  " from " + _fromZone.getHumanReadable() +
                    " to " + _toZone.getHumanReadable());
            if (_fromZone == Zone.DRAW_DECK) {
                _game.sendMessage(_cardEnteringPlay.getOwnerName() + " shuffles their deck");
                _game.getGameState().shuffleDeck(_cardEnteringPlay.getOwnerName());
            }
        }

        if (!_cardHasEnteredPlay) {
            _cardHasEnteredPlay = true;
            _finalEffect = getFinalEffect();
            return _finalEffect;
        }

        return getNextEffect();
    }

    public void setVirtualCardAction(boolean virtualCardAction) { _virtualCardAction = virtualCardAction; }
    public boolean isVirtualCardAction() { return _virtualCardAction; }

    public boolean wasCarriedOut() {
        return _cardHasEnteredPlay && _finalEffect != null && _finalEffect.wasCarriedOut();
    }

}
