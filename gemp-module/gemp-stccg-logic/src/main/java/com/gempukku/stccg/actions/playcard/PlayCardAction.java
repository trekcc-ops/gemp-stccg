package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.gamestate.GameState;

import java.util.Collections;

public abstract class PlayCardAction extends ActionyAction implements TopLevelSelectableAction {

    final PhysicalCard _performingCard;
    protected final PhysicalCard _cardEnteringPlay;
    final Zone _destinationZone;

    public PlayCardAction(PhysicalCard actionSource, PhysicalCard cardEnteringPlay, Player performingPlayer,
                          Zone toZone, ActionType actionType) {
        super(actionSource.getGame(), performingPlayer, actionType);
        _performingCard = actionSource;
        _cardEnteringPlay = cardEnteringPlay;
        _destinationZone = toZone;
    }


    public PlayCardAction(PhysicalCard actionSource, PhysicalCard cardEnteringPlay, Player performingPlayer,
                          Zone toZone, ActionType actionType, Enum<?>[] progressValues) {
        super(actionSource.getGame(), performingPlayer, actionType, progressValues);
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

    @JsonIdentityReference(alwaysAsId=true)
    public PhysicalCard getCardEnteringPlay() { return _cardEnteringPlay; }

    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        Action cost = getNextCost();
        if (cost != null)
            return cost;

        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);

        if (performingPlayer.getCardsInDrawDeck().contains(_cardEnteringPlay)) {
            cardGame.sendMessage(_cardEnteringPlay.getOwnerName() + " shuffles their deck");
            _cardEnteringPlay.getOwner().shuffleDrawDeck(cardGame);
        }
        putCardIntoPlay(cardGame);
        _wasCarriedOut = true;
        return null;
    }
    
    protected void putCardIntoPlay(DefaultGame game) {
        GameState gameState = game.getGameState();
        game.removeCardsFromZone(_cardEnteringPlay.getOwner(), Collections.singleton(_cardEnteringPlay));
        gameState.addCardToZone(_cardEnteringPlay, _destinationZone);
        game.getActionsEnvironment().emitEffectResult(new PlayCardResult(this, _cardEnteringPlay));
        game.sendMessage(_cardEnteringPlay.getOwnerName() + " played " +
                _cardEnteringPlay.getCardLink());
        setAsSuccessful();
    }

    public boolean wasCarriedOut() {
        return _wasCarriedOut;
    }

}