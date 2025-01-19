package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.Objects;

public class AddSeedCardsAction extends ActionyAction implements TopLevelSelectableAction {

    private final PhysicalCard _performingCard;

    public AddSeedCardsAction(Player player, PhysicalCard topCard) {
        super(player, "Seed cards under " + topCard.getFullName(), ActionType.OTHER);
        _performingCard = Objects.requireNonNull(topCard);
    }

    public boolean requirementsAreMet(DefaultGame game) { return true; }
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