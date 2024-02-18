package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayOrder;
import com.gempukku.stccg.rules.GameUtils;

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
            final PlayOrder playerOrder = _game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_source.getOwnerName(), false);

            String nextPlayer;
            while ((nextPlayer = playerOrder.getNextPlayer()) != null) {
                _game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                        new ArbitraryCardsSelectionDecision(1, "Revealed card(s)", _cards, Collections.emptySet(), 0, 0) {
                            @Override
                            public void decisionMade(String result) {
                            }
                        });
            }

            _game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " revealed cards - " + GameUtils.getAppendedNames(_cards));
        }
    }
}
