package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

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
                        LegalActionList result = new LegalActionList();
                        if (playerId.equals(_game.getCurrentPlayerId())) {
                            for (PhysicalCard card : Filters.filterYourActive(player)) {
                                if (card instanceof AffiliatedCard affiliatedCard) {
                                    result.addIfLegal(new ChangeAffiliationAction(player, affiliatedCard));
                                }
                            }
                        }
                        return result;
                    }
                }
        );
    }
}