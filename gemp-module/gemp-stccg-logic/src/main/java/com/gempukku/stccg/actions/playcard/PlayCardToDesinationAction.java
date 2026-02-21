package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.targetresolver.EnterPlayAtDestinationResolver;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

import java.util.Collection;
import java.util.List;

public class PlayCardToDesinationAction extends PlayCardAction {
    private final EnterPlayAtDestinationResolver _targetResolver;

    public PlayCardToDesinationAction(DefaultGame cardGame, String performingPlayerName,
                                      PhysicalCard cardEnteringPlay,
                                      Collection<PhysicalCard> destinationOptions,
                                      ActionContext context) {
        super(cardGame, cardEnteringPlay, cardEnteringPlay, performingPlayerName, null, ActionType.PLAY_CARD, context);
        _targetResolver = new EnterPlayAtDestinationResolver(performingPlayerName, cardEnteringPlay, destinationOptions);
        _cardTargets.add(_targetResolver);
    }

    public void processEffect(DefaultGame cardGame) {
        GameState gameState = cardGame.getGameState();
        PhysicalCard destination = _targetResolver.getDestination();
        cardGame.removeCardsFromZone(List.of(_cardEnteringPlay));
        gameState.addCardToZone(cardGame, _cardEnteringPlay, destination.getZone(), _actionContext);
        _cardEnteringPlay.setAsAtop(destination);
        setAsSuccessful();
    }

}