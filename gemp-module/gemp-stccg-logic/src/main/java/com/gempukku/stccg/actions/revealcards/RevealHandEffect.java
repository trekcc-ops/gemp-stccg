package com.gempukku.stccg.actions.revealcards;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collections;
import java.util.List;

public class RevealHandEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final String _actingPlayer;
    private final String _handPlayerId;
    private final DefaultGame _game;
    private final ActionContext _context;
    private final String _memoryId;

    public RevealHandEffect(ActionContext actionContext, String handPlayerId, String memoryId) {
        super(actionContext);
        _source = actionContext.getSource();
        _actingPlayer = actionContext.getPerformingPlayerId();
        _handPlayerId = handPlayerId;
        _game = actionContext.getGame();
        _memoryId = memoryId;
        _context = actionContext;
    }


    @Override
    public String getText() {
        return "Reveal cards from hand";
    }

    @Override
    public boolean isPlayableInFull() {
        return _game.getModifiersQuerying().canLookOrRevealCardsInHand(_handPlayerId, _actingPlayer);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (_game.getModifiersQuerying().canLookOrRevealCardsInHand(_handPlayerId, _actingPlayer)) {
            final List<? extends PhysicalCard> hand = _game.getGameState().getHand(_handPlayerId);
            _game.sendMessage(
                    _source.getCardLink() + " revealed " + _handPlayerId + " cards in hand - " +
                            TextUtils.getConcatenatedCardLinks(hand)
            );

            final ActionOrder playerOrder = _game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_handPlayerId, false);
            // Skip hand owner
            playerOrder.getNextPlayer();

            String nextPlayer;
            while ((nextPlayer = playerOrder.getNextPlayer()) != null) {
                _game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                        new ArbitraryCardsSelectionDecision(1, "Hand of " + _handPlayerId, hand, Collections.emptySet(), 0, 0) {
                            @Override
                            public void decisionMade(String result) {
                            }
                        });
            }

            _context.setCardMemory(_memoryId, hand);

            for (PhysicalCard card : hand) {
                _game.getActionsEnvironment().emitEffectResult(new RevealCardFromHandResult(_source));
            }

            return new FullEffectResult(true);
        } else {
            return new FullEffectResult(false);
        }
    }

}