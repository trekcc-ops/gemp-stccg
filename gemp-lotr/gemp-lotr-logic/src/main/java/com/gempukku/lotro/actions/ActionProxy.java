package com.gempukku.lotro.actions;

import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.EffectResult;
import com.gempukku.lotro.game.DefaultGame;

import java.util.List;

public interface ActionProxy<AbstractGame extends DefaultGame> {
    List<? extends Action> getPhaseActions(String playerId, AbstractGame game);

    List<? extends Action> getOptionalBeforeActions(String playerId, AbstractGame game, Effect effect);

    List<? extends RequiredTriggerAction> getRequiredBeforeTriggers(AbstractGame game, Effect effect);

    List<? extends OptionalTriggerAction> getOptionalBeforeTriggers(String playerId, AbstractGame game, Effect effect);

    List<? extends Action> getOptionalAfterActions(String playerId, AbstractGame game, EffectResult effectResult);

    List<? extends RequiredTriggerAction> getRequiredAfterTriggers(AbstractGame game, EffectResult effectResult);

    List<? extends OptionalTriggerAction> getOptionalAfterTriggerActions(String playerId, AbstractGame game, EffectResult effectResult);
}
