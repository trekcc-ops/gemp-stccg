package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collections;
import java.util.List;

public class PutRandomCardFromHandOnBottomOfDeckEffect extends DefaultEffect {
    private final String _playerId;
    private final DefaultGame _game;

    public PutRandomCardFromHandOnBottomOfDeckEffect(DefaultGame game, String playerId) {
        _playerId = playerId;
        _game = game;
    }

    @Override
    public boolean isPlayableInFull() {
        return _game.getGameState().getHand(_playerId).size() > 0;
    }

    @Override
    public String getText() {
        return "Put random card from hand on bottom of deck";
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            GameState gameState = _game.getGameState();
            final List<PhysicalCard> randomCards = GameUtils.getRandomCards(gameState.getHand(_playerId), 1);
            for (PhysicalCard randomCard : randomCards) {
                gameState.sendMessage(randomCard.getOwner() + " puts a card at random from hand on bottom of their deck");
                gameState.removeCardsFromZone(randomCard.getOwner(), Collections.singleton(randomCard));
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
