package com.gempukku.lotro.effects;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.TribblesGame;
import com.gempukku.lotro.gamestate.TribblesGameState;

import java.util.LinkedList;
import java.util.List;

public class ActivateMutateTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateMutateTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(TribblesGame game) {
        SubAction subAction = new SubAction(_action);
        TribblesGameState gameState = game.getGameState();
        List<PhysicalCard> playPile = new LinkedList<>(gameState.getPlayPile(_activatingPlayer));

        // Count the number of cards in your play pile.
        int cardsInPlayPile = playPile.size();

        // Shuffle your play pile into your draw deck
        gameState.removeCardsFromZone(_activatingPlayer, playPile);
        for (PhysicalCard physicalCard : playPile) {
            gameState.putCardOnBottomOfDeck(physicalCard);
        }
        gameState.shuffleDeck(_activatingPlayer);

        // Then put that many cards from the top of your draw deck in your play pile
        subAction.appendEffect(new PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect(_activatingPlayer, cardsInPlayPile));
        return addActionAndReturnResult(game, subAction);
    }
}