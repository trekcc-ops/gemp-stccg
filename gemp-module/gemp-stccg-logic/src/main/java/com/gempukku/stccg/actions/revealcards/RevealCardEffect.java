package com.gempukku.stccg.actions.revealcards;

import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectType;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.TextUtils;

import java.util.Collection;
import java.util.Collections;

public class RevealCardEffect implements Effect {
    private final PhysicalCard _source;
    private final Collection<? extends PhysicalCard> _cards;
    private final DefaultGame _game;

    public RevealCardEffect(ActionContext actionContext, Collection<? extends PhysicalCard> cards) {
        _source = actionContext.getSource();
        _game = actionContext.getGame();
        _cards = cards;
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public EffectType getType() {
        return null;
    }
    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    public boolean wasCarriedOut() {
        return true;
    }

    @Override
    public void playEffect() {
        if (!_cards.isEmpty()) {
            final ActionOrder playerOrder = _game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_source.getOwnerName(), false);

            String nextPlayer;
            while ((nextPlayer = playerOrder.getNextPlayer()) != null) {
                _game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                        new ArbitraryCardsSelectionDecision(1, "Revealed card(s)", _cards, Collections.emptySet(), 0, 0) {
                            @Override
                            public void decisionMade(String result) {
                            }
                        });
            }

            _game.sendMessage(_source.getCardLink() + " revealed cards - " +
                    TextUtils.concatenateStrings(_cards.stream().map(PhysicalCard::getCardLink)));
        }
    }

    // TODO - Added null here so this class implements this method. Not clear how it should be determined.
    public String getPerformingPlayerId() { return null; }
}
