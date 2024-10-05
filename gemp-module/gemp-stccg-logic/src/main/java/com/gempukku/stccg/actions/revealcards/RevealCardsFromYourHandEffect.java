package com.gempukku.stccg.actions.revealcards;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.TextUtils;

import java.util.Collection;
import java.util.Collections;

public class RevealCardsFromYourHandEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final String _handPlayerId;
    private final Collection<? extends PhysicalCard> _cards;
    private final DefaultGame _game;

    public RevealCardsFromYourHandEffect(ActionContext actionContext, Collection<? extends PhysicalCard> cards) {
        super(actionContext);
        _source = actionContext.getSource();
        _handPlayerId = actionContext.getPerformingPlayerId();
        _game = actionContext.getGame();
        _cards = cards;
    }

    @Override
    public String getText() {
        return "Reveal cards from hand";
    }

    @Override
    public boolean isPlayableInFull() {
        for (PhysicalCard card : _cards) {
            if (card.getZone() != Zone.HAND)
                return false;
        }

        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        _game.sendMessage(_source.getCardLink() + " revealed " + _handPlayerId + " cards in hand - " +
                TextUtils.concatenateStrings(_cards.stream().map(PhysicalCard::getCardLink)));

        final ActionOrder playerOrder = _game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_handPlayerId, false);
        // Skip hand owner
        playerOrder.getNextPlayer();

        String nextPlayer;
        while ((nextPlayer = playerOrder.getNextPlayer()) != null) {
            _game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                    new ArbitraryCardsSelectionDecision(1, _handPlayerId + " revealed card(s) in hand", _cards, Collections.emptySet(), 0, 0) {
                        @Override
                        public void decisionMade(String result) {
                        }
                    });
        }

        for (PhysicalCard card : _cards) {
            _game.getActionsEnvironment().emitEffectResult(new RevealCardFromHandResult(_source));
        }

        return new FullEffectResult(true);
    }
}
