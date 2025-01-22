package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.placecard.PlaceTopCardOfDrawDeckOnTopOfPlayPileAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;

import java.util.Collection;
import java.util.LinkedList;


public class ActivateMutateTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateMutateTribblePowerAction(TribblesActionContext actionContext, TribblePower power) throws PlayerNotFoundException {
        super(actionContext, power);
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (!_wasCarriedOut) {
            if (cardGame instanceof TribblesGame game) {
                TribblesGameState gameState = game.getGameState();
                Collection<PhysicalCard> playPile = new LinkedList<>(gameState.getPlayPile(_performingPlayerId));

                // Count the number of cards in your play pile.
                int cardsInPlayPile = playPile.size();

                // Shuffle your play pile into your draw deck
                gameState.removeCardsFromZone(_performingPlayerId, playPile);
                for (PhysicalCard physicalCard : playPile) {
                    gameState.putCardOnBottomOfDeck(physicalCard);
                }
                cardGame.getPlayer(_performingPlayerId).shuffleDrawDeck(cardGame);

                // Then put that many cards from the top of your draw deck in your play pile
                appendEffect(new PlaceTopCardOfDrawDeckOnTopOfPlayPileAction(game, game.getPlayer(_performingPlayerId),
                        cardsInPlayPile));
            } else {
                throw new InvalidGameLogicException("Could not use tribble power Mutate in a non-Tribbles game");
            }
            _wasCarriedOut = true;
        }
        return getNextAction();
    }

}