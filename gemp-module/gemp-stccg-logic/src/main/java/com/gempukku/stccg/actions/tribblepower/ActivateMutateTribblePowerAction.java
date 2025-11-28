package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.placecard.PlaceTopCardOfDrawDeckOnTopOfPlayPileAction;
import com.gempukku.stccg.actions.placecard.ShuffleCardsIntoDrawDeckAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;

import java.util.Collection;
import java.util.LinkedList;


public class ActivateMutateTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateMutateTribblePowerAction(TribblesGame cardGame, TribblesActionContext actionContext)
            throws PlayerNotFoundException {
        super(cardGame, actionContext, TribblePower.MUTATE);
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (!_wasCarriedOut) {
            if (cardGame instanceof TribblesGame game) {
                TribblesGameState gameState = game.getGameState();
                Collection<PhysicalCard> playPile = new LinkedList<>(gameState.getPlayPile(_performingPlayerId));

                // Count the number of cards in your play pile.
                int cardsInPlayPile = playPile.size();
                Player performingPlayer = cardGame.getPlayer(_performingPlayerId);

                // Shuffle your play pile into your draw deck
                appendEffect(new ShuffleCardsIntoDrawDeckAction(
                        _performingCard, performingPlayer, Filters.in(performingPlayer.getCardGroupCards(Zone.PLAY_PILE))));

                // Then put that many cards from the top of your draw deck in your play pile
                appendEffect(new PlaceTopCardOfDrawDeckOnTopOfPlayPileAction(game, performingPlayer, cardsInPlayPile));
            } else {
                throw new InvalidGameLogicException("Could not use tribble power Mutate in a non-Tribbles game");
            }
            _wasCarriedOut = true;
        }
        return getNextAction();
    }

}