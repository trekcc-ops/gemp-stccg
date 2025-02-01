package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

public class RemoveSeedCardsAction extends ActionyAction implements TopLevelSelectableAction {

    private final PhysicalCard _performingCard;

    public RemoveSeedCardsAction(Player player, PhysicalCard topCard) {
        super(topCard.getGame(), player, "Remove seed cards from " + topCard.getFullName(), ActionType.SELECT_CARDS);
        _performingCard = topCard;
    }

    public boolean requirementsAreMet(DefaultGame cardGame) { return true; }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public int getCardIdForActionSelection() {
        return _performingCard.getCardId();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        return getNextAction();
    }

}