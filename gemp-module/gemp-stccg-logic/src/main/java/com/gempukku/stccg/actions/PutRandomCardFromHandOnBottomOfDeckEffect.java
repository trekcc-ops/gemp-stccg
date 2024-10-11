package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.TextUtils;

import java.util.Collections;
import java.util.List;

public class PutRandomCardFromHandOnBottomOfDeckEffect extends DefaultEffect {
    private final String _playerId;

    public PutRandomCardFromHandOnBottomOfDeckEffect(DefaultGame game, String playerId) {
        super(game, playerId);
        _playerId = playerId;
    }

    @Override
    public boolean isPlayableInFull() {
        return !_game.getGameState().getHand(_playerId).isEmpty();
    }

    @Override
    public String getText() {
        return "Put random card from hand on bottom of deck";
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            GameState gameState = _game.getGameState();
            final List<PhysicalCard> randomCards = TextUtils.getRandomFromList(gameState.getHand(_playerId), 1);
            for (PhysicalCard randomCard : randomCards) {
                gameState.sendMessage(randomCard.getOwnerName() + " puts a card at random from hand on bottom of their deck");
                gameState.removeCardsFromZone(randomCard.getOwnerName(), Collections.singleton(randomCard));
                gameState.putCardOnBottomOfDeck(randomCard);
                putCardFromHandOnBottomOfDeckCallback(randomCard);
            }

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }

    protected void putCardFromHandOnBottomOfDeckCallback(PhysicalCard card) {

    }
}