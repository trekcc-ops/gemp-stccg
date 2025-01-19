package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.GameState;

import java.util.Collections;

public abstract class PlayCardAction extends ActionyAction implements TopLevelSelectableAction {

    final PhysicalCard _performingCard;
    protected final PhysicalCard _cardEnteringPlay;
    final Zone _destinationZone;

    /**
     * Creates an action for playing the specified card.
     * @param actionSource the card to initiate the deployment
     */
    public PlayCardAction(PhysicalCard actionSource, PhysicalCard cardEnteringPlay, String performingPlayerId,
                          Zone toZone, ActionType actionType) {
        super(cardEnteringPlay.getGame().getPlayer(performingPlayerId), actionType);
        _performingCard = actionSource;
        _cardEnteringPlay = cardEnteringPlay;
        _destinationZone = toZone;
    }

    public PlayCardAction(PhysicalCard actionSource, PhysicalCard cardEnteringPlay, String performingPlayerId,
                          Zone toZone, ActionType actionType, Enum<?>[] progressValues) {
        super(cardEnteringPlay.getGame().getPlayer(performingPlayerId), actionType, progressValues);
        _performingCard = actionSource;
        _cardEnteringPlay = cardEnteringPlay;
        _destinationZone = toZone;
    }


    public boolean requirementsAreMet(DefaultGame cardGame) {
        return _cardEnteringPlay.canBePlayed(cardGame);
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    public int getCardIdForActionSelection() {
        return _cardEnteringPlay.getCardId();
    }

    public PhysicalCard getCardEnteringPlay() { return _cardEnteringPlay; }

    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Action cost = getNextCost();
        if (cost != null)
            return cost;

        Zone currentZone = _cardEnteringPlay.getZone();

        if (currentZone == Zone.DRAW_DECK) {
            cardGame.sendMessage(_cardEnteringPlay.getOwnerName() + " shuffles their deck");
            cardGame.getGameState().shuffleDeck(_cardEnteringPlay.getOwnerName());
        }
        putCardIntoPlay(cardGame);
        _wasCarriedOut = true;
        return null;
    }
    
    protected void putCardIntoPlay(DefaultGame game) {
        Zone originalZone = _cardEnteringPlay.getZone();
        GameState gameState = game.getGameState();
        gameState.removeCardsFromZone(_cardEnteringPlay.getOwnerName(), Collections.singleton(_cardEnteringPlay));
        gameState.addCardToZone(_cardEnteringPlay, _destinationZone);
        game.getActionsEnvironment().emitEffectResult(new PlayCardResult(this, originalZone, _cardEnteringPlay));
        game.sendMessage(_cardEnteringPlay.getOwnerName() + " played " +
                _cardEnteringPlay.getCardLink() +  " from " + originalZone.getHumanReadable() +
                " to " + _destinationZone.getHumanReadable());
    }

    public boolean wasCarriedOut() {
        return _wasCarriedOut;
    }

}