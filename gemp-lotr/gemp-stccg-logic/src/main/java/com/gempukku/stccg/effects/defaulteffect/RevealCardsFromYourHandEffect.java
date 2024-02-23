package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayOrder;
import com.gempukku.stccg.results.RevealCardFromHandResult;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collection;
import java.util.Collections;

public class RevealCardsFromYourHandEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final String _handPlayerId;
    private final Collection<? extends PhysicalCard> _cards;
    private final DefaultGame _game;

    public RevealCardsFromYourHandEffect(ActionContext actionContext, Collection<? extends PhysicalCard> cards) {
        _source = actionContext.getSource();
        _handPlayerId = actionContext.getPerformingPlayer();
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
        _game.getGameState().sendMessage(_source.getCardLink() + " revealed " + _handPlayerId + " cards in hand - " +
                GameUtils.concatenateStrings(_cards.stream().map(PhysicalCard::getCardLink)));

        final PlayOrder playerOrder = _game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_handPlayerId, false);
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
            _game.getActionsEnvironment().emitEffectResult(new RevealCardFromHandResult(_source, _handPlayerId, card));
        }

        return new FullEffectResult(true);
    }
}
