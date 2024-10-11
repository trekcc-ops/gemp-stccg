package com.gempukku.stccg.actions.revealcards;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collections;
import java.util.List;

public class LookAtTopCardOfADeckEffect extends DefaultEffect {
    private final String _playerId;
    private final int _count;
    private final String _playerDeckId;
    private final ActionContext _actionContext;
    private final DefaultGame _game;
    private String _memoryId = null;

    public LookAtTopCardOfADeckEffect(ActionContext actionContext, int count, String playerDeckId) {
        super(actionContext);
        _playerId = actionContext.getPerformingPlayerId();
        _count = count;
        _playerDeckId = playerDeckId;
        _actionContext = actionContext;
        _game = actionContext.getGame();
    }

    public LookAtTopCardOfADeckEffect(ActionContext actionContext, int count, String playerDeckId, String memoryId) {
        this(actionContext, count, playerDeckId);
        _memoryId = memoryId;
    }

    @Override
    public String getText() {
        if(_count == _actionContext.getGameState().getDrawDeck(_playerDeckId).size())
            return _playerId + " looks at " + _playerDeckId + "'s draw deck.";

        return _playerId + " looks at the top " + _count + " cards of " + _playerDeckId + "'s draw deck.";
    }

    @Override
    public boolean isPlayableInFull() {
        return _actionContext.getGameState().getDrawDeck(_playerDeckId).size() >= _count;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        List<? extends PhysicalCard> deck = _game.getGameState().getDrawDeck(_playerDeckId);
        List<? extends PhysicalCard> cards = _game.getGameState().getDrawDeck(_playerDeckId).subList(0, Math.min(deck.size(), _count));

        String message = "Cards on top of deck (left is top)";
        if(_count == deck.size())
            message = "Cards in deck";

        _game.getUserFeedback().sendAwaitingDecision(_playerId,
                new ArbitraryCardsSelectionDecision(1, message, cards, Collections.emptyList(), 0, 0) {
                    @Override
                    public void decisionMade(String result) {
                    }
                });
        if (_memoryId != null)
            _actionContext.setCardMemory(_memoryId, cards);
        cardsLookedAt(cards);
        return new FullEffectResult(deck.size() >= _count);
    }

    protected void cardsLookedAt(List<? extends PhysicalCard> cards) {
    }
}
