package com.gempukku.stccg.cards;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.EffectResult;

import java.util.Collection;

public class DelegateActionContext<AbstractGame extends DefaultGame> extends DefaultActionContext<AbstractGame> {
    private final DefaultActionContext<AbstractGame> delegate;

    public DelegateActionContext(DefaultActionContext<AbstractGame> delegate, String performingPlayer, AbstractGame game,
                                 PhysicalCard source, EffectResult effectResult, Effect effect) {
        super(performingPlayer, game, source, effectResult, effect);
        this.delegate = delegate;
    }

    @Override
    public void setValueToMemory(String memory, String value) {
        delegate.setValueToMemory(memory, value);
    }

    @Override
    public String getValueFromMemory(String memory) {
        return delegate.getValueFromMemory(memory);
    }

    @Override
    public void setCardMemory(String memory, PhysicalCard card) {
        delegate.setCardMemory(memory, card);
    }

    @Override
    public void setCardMemory(String memory, Collection<? extends PhysicalCard> cards) {
        delegate.setCardMemory(memory, cards);
    }

    @Override
    public Collection<PhysicalCard> getCardsFromMemory(String memory) {
        return delegate.getCardsFromMemory(memory);
    }

    @Override
    public PhysicalCard getCardFromMemory(String memory) {
        return delegate.getCardFromMemory(memory);
    }

}
