package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.gamestate.TribblesGameState;

import java.util.Collection;
import java.util.LinkedList;

public class ActivateMutateTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateMutateTribblePowerEffect(Action action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        SubAction subAction = new SubAction(_action, _game);
        TribblesGameState gameState = getGame().getGameState();
        Collection<PhysicalCard> playPile = new LinkedList<>(gameState.getPlayPile(_activatingPlayer));

        // Count the number of cards in your play pile.
        int cardsInPlayPile = playPile.size();

        // Shuffle your play pile into your draw deck
        gameState.removeCardsFromZone(_activatingPlayer, playPile);
        for (PhysicalCard physicalCard : playPile) {
            gameState.putCardOnBottomOfDeck(physicalCard);
        }
        gameState.shuffleDeck(_activatingPlayer);

        // Then put that many cards from the top of your draw deck in your play pile
        subAction.appendEffect(new PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect(getGame(), _activatingPlayer, cardsInPlayPile));
        return addActionAndReturnResult(getGame(), subAction);
    }
}