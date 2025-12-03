package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.List;

public abstract class PlayCardAction extends ActionyAction implements TopLevelSelectableAction {

    final PhysicalCard _performingCard;
    protected final PhysicalCard _cardEnteringPlay;
    final Zone _destinationZone;
    private boolean _initiated;
    private boolean _cardPlayed;
    private final List<Action> _immediateGameTextActions = new ArrayList<>();

    public PlayCardAction(DefaultGame cardGame, PhysicalCard actionSource, PhysicalCard cardEnteringPlay,
                          String performingPlayerName, Zone toZone, ActionType actionType) {
        super(cardGame, performingPlayerName, actionType);
        _performingCard = actionSource;
        _cardEnteringPlay = cardEnteringPlay;
        _destinationZone = toZone;
    }

    public PlayCardAction(DefaultGame cardGame, PhysicalCard actionSource, PhysicalCard cardEnteringPlay,
                          String performingPlayerName, Zone toZone, ActionType actionType, Enum<?>[] progressValues) {
        super(cardGame, performingPlayerName, actionType, progressValues);
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

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    public PhysicalCard getCardEnteringPlay() { return _cardEnteringPlay; }

    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_initiated) {
            _initiated = true;
            _cardEnteringPlay.removeFromCardGroup(cardGame);
            ActionResult playCardInitiationResult = new PlayCardInitiationResult(this, _cardEnteringPlay);
            saveResult(playCardInitiationResult);
            return null;
        }

        if (isInProgress() && !_cardPlayed) {
            _cardPlayed = true;
            putCardIntoPlay(cardGame);
        }

        if (isInProgress()) {
            Action nextAction = getNextAction();
            if (nextAction == null) {
                setAsSuccessful();
            }
            return nextAction;
        }
        return null;
    }
    
    protected void putCardIntoPlay(DefaultGame cardGame) throws PlayerNotFoundException {
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);

        if (performingPlayer.getCardsInDrawDeck().contains(_cardEnteringPlay)) {
            cardGame.sendMessage(_cardEnteringPlay.getOwnerName() + " shuffles their deck");
            performingPlayer.shuffleDrawDeck(cardGame);
        }
        GameState gameState = cardGame.getGameState();
        gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, List.of(_cardEnteringPlay));
        gameState.addCardToZone(cardGame, _cardEnteringPlay, _destinationZone);
        saveResult(new PlayCardResult(this, _cardEnteringPlay));
        _wasCarriedOut = true;
    }

    public boolean wasCarriedOut() {
        return _wasCarriedOut;
    }

    public void addImmediateGameTextAction(Action action) {
        _immediateGameTextActions.add(action);
    }

}