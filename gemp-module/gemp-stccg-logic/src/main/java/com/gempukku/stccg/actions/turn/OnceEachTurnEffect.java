package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.game.DefaultGame;

public class OnceEachTurnEffect extends CheckTurnLimitEffect {

    public OnceEachTurnEffect(DefaultGame game, Action action) { super(game, action, 1); }
}