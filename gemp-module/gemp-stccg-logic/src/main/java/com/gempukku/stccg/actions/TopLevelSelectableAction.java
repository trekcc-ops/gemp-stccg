package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public interface TopLevelSelectableAction extends Action, AppendableAction {
    String getActionSelectionText(DefaultGame game) throws InvalidGameLogicException;
    PhysicalCard getCardForActionSelection();
}