package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.PlayOrder;
import com.gempukku.lotro.logic.PlayerOrder;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.AbstractSuccessfulEffect;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.Collection;
import java.util.Collections;

public class RevealCardEffect extends AbstractSuccessfulEffect {
    private PhysicalCard _source;
    private Collection<? extends PhysicalCard> _cards;

    public RevealCardEffect(PhysicalCard source, PhysicalCard card) {
        this(source, Collections.singleton(card));
    }

    public RevealCardEffect(PhysicalCard source, Collection<? extends PhysicalCard> cards) {
        _source = source;
        _cards = cards;
    }

    @Override
    public String getText(LotroGame game) {
        return null;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public void playEffect(LotroGame game) {
        if (_cards.size() > 0) {
            final PlayOrder playerOrder = game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_source.getOwner(), false);

            String nextPlayer;
            while ((nextPlayer = playerOrder.getNextPlayer()) != null) {
                game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                        new ArbitraryCardsSelectionDecision(1, "Revealed card(s)", _cards, Collections.<PhysicalCard>emptySet(), 0, 0) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                            }
                        });
            }

            game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " revealed cards - " + getAppendedNames(_cards));
        }
    }
}
