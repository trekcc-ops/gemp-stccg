package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.effects.defaulteffect.ActivateTribblePowerEffect;
import com.gempukku.stccg.effects.defaulteffect.PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect;
import com.gempukku.stccg.gamestate.TribblesGameState;

import java.util.LinkedList;
import java.util.List;

public class ActivateMutateTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateMutateTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        SubAction subAction = new SubAction(_action);
        TribblesGameState gameState = _game.getGameState();
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
        subAction.appendEffect(new PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect(_game, _activatingPlayer, cardsInPlayPile));
        return addActionAndReturnResult(_game, subAction);
    }
}