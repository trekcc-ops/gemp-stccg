package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.AbstractCostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

public class AddSeedCardsAction extends AbstractCostToEffectAction {

    private final PhysicalCard _topCard;

    public AddSeedCardsAction(Player player, ST1EPhysicalCard topCard) {
        super(player, ActionType.OTHER);
        setText("Seed cards under " + topCard.getFullName());
        _topCard = topCard;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _topCard;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _topCard;
    }

    @Override
    public Effect nextEffect(DefaultGame cardGame) throws InvalidGameLogicException {
        return getNextEffect();
    }

}