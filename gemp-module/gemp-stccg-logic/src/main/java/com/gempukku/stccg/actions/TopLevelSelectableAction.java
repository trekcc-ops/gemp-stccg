package com.gempukku.stccg.actions;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public interface TopLevelSelectableAction extends Action, CardPerformedAction {
    String getActionSelectionText(DefaultGame game) throws InvalidGameLogicException;
    int getCardIdForActionSelection();
}