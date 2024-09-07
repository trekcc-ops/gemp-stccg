package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;

public class OnceEachTurnEffect extends CheckTurnLimitEffect {

    public OnceEachTurnEffect(Action action) { super(action, 1); }
}