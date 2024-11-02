package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.AbstractCostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.choose.ChooseCardsFromZoneEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
    public Effect nextEffect() throws InvalidGameLogicException {
        return getNextEffect();
    }

    @Override
    public DefaultGame getGame() { return _topCard.getGame(); }
}