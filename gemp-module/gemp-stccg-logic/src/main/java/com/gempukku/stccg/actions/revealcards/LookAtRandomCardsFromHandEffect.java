package com.gempukku.stccg.actions.revealcards;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.TextUtils;

import java.util.Collections;
import java.util.List;

public abstract class LookAtRandomCardsFromHandEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final int _count;
    private final String _actingPlayer;
    private final String _playerHand;

    public LookAtRandomCardsFromHandEffect(ActionContext actionContext, String handOfPlayer, int count) {
        super(actionContext);
        _source = actionContext.getSource();
        _count = count;
        _actingPlayer = actionContext.getPerformingPlayerId();
        _playerHand = handOfPlayer;
    }

    @Override
    public String getText() {
        return "Look at random cards from hand";
    }

    @Override
    public boolean isPlayableInFull() {
        if (_game.getGameState().getHand(_playerHand).size() < _count)
            return false;
        return _actingPlayer.equals(_playerHand) || _game.getModifiersQuerying().canLookOrRevealCardsInHand(_game, _playerHand, _actingPlayer);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (_game.getModifiersQuerying().canLookOrRevealCardsInHand(_game, _playerHand, _actingPlayer)) {
            List<PhysicalCard> randomCards = TextUtils.getRandomFromList(_game.getGameState().getHand(_playerHand), _count);

            if (!randomCards.isEmpty()) {
                _game.getUserFeedback().sendAwaitingDecision(_actingPlayer,
                        new ArbitraryCardsSelectionDecision(1, "Random cards from opponent's hand", randomCards, Collections.emptyList(), 0, 0) {
                            @Override
                            public void decisionMade(String result) {
                            }
                        });

                _game.sendMessage(_source.getCardLink() + " looked at " + randomCards.size() + " cards from " + _playerHand + " hand at random");
            }
            else {
                _game.sendMessage("No cards in " + _playerHand + " hand to look at");
            }

            cardsSeen(randomCards);

            return new FullEffectResult(randomCards.size() == _count);
        }
        return new FullEffectResult(false);
    }

    protected abstract void cardsSeen(List<PhysicalCard> revealedCards);
}
