package com.gempukku.stccg.rules.generic;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.RequiredType;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class OptionalTriggersRule extends GenericRule {
    public OptionalTriggersRule(DefaultGame game) {
        super(game);
    }

    @Override
    public List<? extends Action> getOptionalBeforeTriggerActions(String playerId, Effect effect) {
        Player player = _game.getGameState().getPlayer(playerId);
        List<Action> result = new LinkedList<>();
        for (PhysicalCard card : Filters.filterYourActive(player)) {
            if (!card.hasTextRemoved())
                result.addAll(card.getBeforeTriggerActions(playerId, effect, RequiredType.OPTIONAL));
        }
        return result;
    }
}