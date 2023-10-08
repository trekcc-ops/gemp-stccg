package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collections;
import java.util.List;

public class LookAtTopCardOfADeckEffect extends AbstractEffect {
    private final String _playerId;
    private final int _count;
    private final String _playerDeckId;

    public LookAtTopCardOfADeckEffect(String playerId, int count, String playerDeckId) {
        _playerId = playerId;
        _count = count;
        _playerDeckId = playerDeckId;
    }

    @Override
    public String getText(DefaultGame game) {
        if(_count == game.getGameState().getDrawDeck(_playerDeckId).size())
            return _playerId + " looks at " + _playerDeckId + "'s draw deck.";

        return _playerId + " looks at the top " + _count + " cards of " + _playerDeckId + "'s draw deck.";
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return game.getGameState().getDrawDeck(_playerDeckId).size() >= _count;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        List<? extends PhysicalCard> deck = game.getGameState().getDrawDeck(_playerDeckId);
        List<? extends PhysicalCard> cards = game.getGameState().getDrawDeck(_playerDeckId).subList(0, Math.min(deck.size(), _count));

        String message = "Cards on top of deck (left is top)";
        if(_count == deck.size())
            message = "Cards in deck";

        game.getUserFeedback().sendAwaitingDecision(_playerId,
                new ArbitraryCardsSelectionDecision(1, message, cards, Collections.emptyList(), 0, 0) {
                    @Override
                    public void decisionMade(String result) {
                    }
                });
        cardsLookedAt(cards);
        return new FullEffectResult(deck.size() >= _count);
    }

    protected void cardsLookedAt(List<? extends PhysicalCard> cards) {

    }
}
