package com.gempukku.stccg.cards;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.EffectResult;

import java.util.Collection;

public interface ActionContext {
    void setValueToMemory(String memory, String value);

    String getValueFromMemory(String memory);

    void setCardMemory(String memory, PhysicalCard card);

    void setCardMemory(String memory, Collection<? extends PhysicalCard> cards);

    Collection<? extends PhysicalCard> getCardsFromMemory(String memory);

    PhysicalCard getCardFromMemory(String memory);

    String getPerformingPlayer();

    <AbstractGame extends DefaultGame> AbstractGame getGame();

    PhysicalCard getSource();

    EffectResult getEffectResult();

    Effect getEffect();
}
