package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.LinkedList;
import java.util.List;

public class ST1EChangeAffiliationRule {
    private final ActionsEnvironment actionsEnvironment;
    private final DefaultGame _game;

    public ST1EChangeAffiliationRule(ActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
        _game = actionsEnvironment.getGame();
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<Action> getPhaseActions(String playerId) {
                        Player player = _game.getGameState().getPlayer(playerId);
                        LinkedList<Action> result = new LinkedList<>();
                        if (playerId.equals(_game.getCurrentPlayerId())) {
                            for (PhysicalCard card : Filters.filterYourActive(player)) {
                                if (card instanceof AffiliatedCard affiliatedCard) {
                                    ChangeAffiliationAction action = new ChangeAffiliationAction(player, affiliatedCard);
                                    if (action.canBeInitiated())
                                        result.add(action);
                                }
                            }
                        }
                        return result;
                    }
                }
        );
    }
}