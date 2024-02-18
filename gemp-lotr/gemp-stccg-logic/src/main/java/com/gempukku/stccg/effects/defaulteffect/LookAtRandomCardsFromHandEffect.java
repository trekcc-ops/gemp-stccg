package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collections;
import java.util.List;

public abstract class LookAtRandomCardsFromHandEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final int _count;
    private final String _actingPlayer;
    private final String _playerHand;
    private final DefaultGame _game;

    public LookAtRandomCardsFromHandEffect(ActionContext actionContext, String handOfPlayer, int count) {
        _source = actionContext.getSource();
        _game = actionContext.getGame();
        _count = count;
        _actingPlayer = actionContext.getPerformingPlayer();
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
            List<PhysicalCard> randomCards = GameUtils.getRandomCards(_game.getGameState().getHand(_playerHand), _count);

            if (!randomCards.isEmpty()) {
                _game.getUserFeedback().sendAwaitingDecision(_actingPlayer,
                        new ArbitraryCardsSelectionDecision(1, "Random cards from opponent's hand", randomCards, Collections.emptyList(), 0, 0) {
                            @Override
                            public void decisionMade(String result) {
                            }
                        });

                _game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " looked at " + randomCards.size() + " cards from " + _playerHand + " hand at random");
            }
            else {
                _game.getGameState().sendMessage("No cards in " + _playerHand + " hand to look at");
            }

            cardsSeen(randomCards);

            return new FullEffectResult(randomCards.size() == _count);
        }
        return new FullEffectResult(false);
    }

    protected abstract void cardsSeen(List<PhysicalCard> revealedCards);
}
