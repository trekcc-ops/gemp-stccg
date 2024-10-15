package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.AbstractCostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.LinkedList;
import java.util.List;

public class SeedMissionCardsAction extends AbstractCostToEffectAction {

    private final List<PhysicalCard> _cardsSeeding = new LinkedList<>();
    private final PhysicalCard _topCard;

    public SeedMissionCardsAction(Player player, PhysicalCard topCard) {
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
    public Effect nextEffect() throws InvalidGameLogicException {
        return null;
    }

    @Override
    public DefaultGame getGame() { return _topCard.getGame(); }
}