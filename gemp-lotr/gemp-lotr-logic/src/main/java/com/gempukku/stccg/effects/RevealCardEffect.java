package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.game.PlayOrder;

import java.util.Collection;
import java.util.Collections;

public class RevealCardEffect extends AbstractSuccessfulEffect {
    private final PhysicalCard _source;
    private final Collection<? extends PhysicalCard> _cards;

    public RevealCardEffect(PhysicalCard source, PhysicalCard card) {
        this(source, Collections.singleton(card));
    }

    public RevealCardEffect(PhysicalCard source, Collection<? extends PhysicalCard> cards) {
        _source = source;
        _cards = cards;
    }

    @Override
    public String getText(DefaultGame game) {
        return null;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public void playEffect(DefaultGame game) {
        if (_cards.size() > 0) {
            final PlayOrder playerOrder = game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_source.getOwner(), false);

            String nextPlayer;
            while ((nextPlayer = playerOrder.getNextPlayer()) != null) {
                game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                        new ArbitraryCardsSelectionDecision(1, "Revealed card(s)", _cards, Collections.emptySet(), 0, 0) {
                            @Override
                            public void decisionMade(String result) {
                            }
                        });
            }

            game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " revealed cards - " + getAppendedNames(_cards));
        }
    }
}
