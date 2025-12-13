package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public abstract class PlayCardAction extends ActionyAction implements TopLevelSelectableAction {
    private final PhysicalCard _performingCard;
    protected final PhysicalCard _cardEnteringPlay;
    @JsonProperty("destinationZone")
    protected Zone _destinationZone;

    public PlayCardAction(DefaultGame cardGame, PhysicalCard actionSource, PhysicalCard cardEnteringPlay,
                          String performingPlayerName, Zone toZone, ActionType actionType, ActionContext context) {
        super(cardGame, performingPlayerName, actionType, context);
        _performingCard = actionSource;
        _cardEnteringPlay = cardEnteringPlay;
        _destinationZone = toZone;
    }

    protected PlayCardAction(int cardId, PhysicalCard actionSource, PhysicalCard cardEnteringPlay,
                          String performingPlayerName, Zone toZone, ActionType actionType) {
        super(cardId, actionType, performingPlayerName);
        _performingCard = actionSource;
        _cardEnteringPlay = cardEnteringPlay;
        _destinationZone = toZone;
    }



    public PlayCardAction(DefaultGame cardGame, PhysicalCard actionSource, PhysicalCard cardEnteringPlay,
                          String performingPlayerName, Zone toZone, ActionType actionType) {
        super(cardGame, performingPlayerName, actionType);
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

    protected void putCardIntoPlay(DefaultGame cardGame) throws PlayerNotFoundException {
        _cardEnteringPlay.removeFromCardGroup(cardGame);
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);

        if (performingPlayer.getCardsInDrawDeck().contains(_cardEnteringPlay)) {
            cardGame.sendMessage(_cardEnteringPlay.getOwnerName() + " shuffles their deck");
            performingPlayer.shuffleDrawDeck(cardGame);
        }
        GameState gameState = cardGame.getGameState();
        gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, List.of(_cardEnteringPlay));
        gameState.addCardToZone(cardGame, _cardEnteringPlay, _destinationZone, _actionContext);
        saveResult(new PlayCardResult(this, _cardEnteringPlay), cardGame);
    }

    protected void processEffect(DefaultGame cardGame) {
        try {
            putCardIntoPlay(cardGame);
            setAsSuccessful();
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

}