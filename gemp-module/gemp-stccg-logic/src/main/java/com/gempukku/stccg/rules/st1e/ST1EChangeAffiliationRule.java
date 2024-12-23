package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ChangeAffiliationAction;
import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

import java.util.LinkedList;
import java.util.List;

public class ST1EChangeAffiliationRule extends ST1ERule {

    ST1EChangeAffiliationRule(ST1EGame game) {
        super(game);
    }
    @Override
    public List<Action> getPhaseActions(String playerId) {
        Player player = _game.getGameState().getPlayer(playerId);
        LinkedList<Action> result = new LinkedList<>();
        if (playerId.equals(_game.getCurrentPlayerId())) {
            for (PhysicalCard card : Filters.filterYourActive(player)) {
                if (card instanceof AffiliatedCard affiliatedCard) {
                    ChangeAffiliationAction action = new ChangeAffiliationAction(player, affiliatedCard);
                    if (action.canBeInitiated(_game))
                        result.add(action);
                }
            }
        }
        return result;
    }
}