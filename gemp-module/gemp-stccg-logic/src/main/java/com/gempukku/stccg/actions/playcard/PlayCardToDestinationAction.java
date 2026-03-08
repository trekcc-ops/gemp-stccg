package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.targetresolver.EnterPlayAtDestinationResolver;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ProxyCoreCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayCardToDestinationAction extends PlayCardAction {
    private final EnterPlayAtDestinationResolver _targetResolver;
    private final boolean _onPlanet;
    private boolean _played;

    public PlayCardToDestinationAction(DefaultGame cardGame, String performingPlayerName,
                                       PhysicalCard cardEnteringPlay,
                                       Collection<PhysicalCard> destinationOptions,
                                       GameTextContext context, boolean onPlanet) {
        super(cardGame, cardEnteringPlay, cardEnteringPlay, performingPlayerName, null, ActionType.PLAY_CARD, context);
        Map<PhysicalCard, Collection<PhysicalCard>> destinationMap = new HashMap<>();
        destinationMap.put(cardEnteringPlay, destinationOptions);
        _targetResolver = new EnterPlayAtDestinationResolver(performingPlayerName, destinationMap);
        _cardTargets.add(_targetResolver);
        _onPlanet = onPlanet;
    }

    public PlayCardToDestinationAction(DefaultGame cardGame, String performingPlayerName,
                                       PhysicalCard cardEnteringPlay,
                                       PhysicalCard destinationCard,
                                       GameTextContext context, boolean onPlanet) {
        super(cardGame, cardEnteringPlay, cardEnteringPlay, performingPlayerName, null, ActionType.PLAY_CARD, context);
        Map<PhysicalCard, Collection<PhysicalCard>> destinationMap = new HashMap<>();
        destinationMap.put(cardEnteringPlay, List.of(destinationCard));
        _targetResolver = new EnterPlayAtDestinationResolver(performingPlayerName, destinationMap);
        _cardTargets.add(_targetResolver);
        _onPlanet = onPlanet;
    }

    public void processEffect(DefaultGame cardGame) {
        if (!_played) {
            GameState gameState = cardGame.getGameState();
            PhysicalCard destination = _targetResolver.getDestination();

            boolean wasInDrawDeck = _cardEnteringPlay.isInDrawDeck(cardGame);

            _cardEnteringPlay.removeFromCardGroup(cardGame);
            cardGame.removeCardsFromZone(List.of(_cardEnteringPlay));

            if (wasInDrawDeck) {
                try {
                    Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
                    cardGame.shuffleCardPile(performingPlayer.getDrawDeck());
                } catch(PlayerNotFoundException exp) {
                    cardGame.sendErrorMessage(exp);
                }
            }

            gameState.addCardToZone(cardGame, _cardEnteringPlay, destination.getZone(), _actionContext);
            if (_onPlanet) {
                _cardEnteringPlay.setAsOnPlanet(destination);
            } else if (!(destination instanceof ProxyCoreCard)) {
                _cardEnteringPlay.setAsAtop(destination);
            }
            saveResult(new PlayCardResult(cardGame, this, _cardEnteringPlay, destination, ActionType.PLAY_CARD), cardGame);
            _played = true;
        } else {
            super.processEffect(cardGame);
        }
    }

}