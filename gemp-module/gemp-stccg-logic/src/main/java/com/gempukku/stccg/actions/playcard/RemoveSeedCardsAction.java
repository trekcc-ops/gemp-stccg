package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

public class RemoveSeedCardsAction extends ActionyAction implements TopLevelSelectableAction {

    private final PhysicalCard _topCard;

    public RemoveSeedCardsAction(Player player, PhysicalCard topCard) {
        super(player, "Remove seed cards from " + topCard.getFullName(), ActionType.OTHER);
        _topCard = topCard;
    }

    public boolean requirementsAreMet(DefaultGame cardGame) { return true; }

    @Override
    public PhysicalCard getPerformingCard() {
        return _topCard;
    }

    @Override
    public int getCardIdForActionSelection() {
        return _topCard.getCardId();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        return getNextAction();
    }

}