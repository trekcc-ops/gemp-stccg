package com.gempukku.stccg.rules.generic;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.cardgroup.PhysicalCardGroup;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.ActionProxy;
import com.gempukku.stccg.gamestate.ActionsEnvironment;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class RuleSet<T extends DefaultGame> {

    private void applyGenericRules(T cardGame) {
        applyActionProxiesAsRules(cardGame,
                new RequiredTriggersRule(),
                new DiscardedCardRule()
        );
    }

    protected void applySpecificRules(T cardGame) { }


    public void applyRuleSet(T cardGame) {
        applyGenericRules(cardGame);
        applySpecificRules(cardGame);
    }

    protected final void applyActionProxiesAsRules(T cardGame, ActionProxy... rules) {
        for (ActionProxy rule : rules) {
            ActionsEnvironment actionsEnvironment = cardGame.getActionsEnvironment();
            actionsEnvironment.addAlwaysOnActionProxy(rule);
        }
    }

    public ActionOrder getPlayerOrderForActionResponse(ActionResult actionResult, DefaultGame cardGame) {
        // Start with opponent of the player that performed the action being responded to
        List<String> respondingPlayerOrder = new ArrayList<>();
        String actionPlayerId = actionResult.getPerformingPlayerId();
        String opponentPlayerId = cardGame.getOpponent(actionPlayerId);
        respondingPlayerOrder.add(opponentPlayerId);
        respondingPlayerOrder.add(actionPlayerId);
        return new ActionOrder(respondingPlayerOrder, true);
    }

    public List<Modifier> getModifiersWhileCardIsInPlay(PhysicalCard card) {
        return new ArrayList<>();
    }

    public Zone getDiscardZone(boolean cardWorthPoints) {
        return Zone.DISCARD;
    }

    public PhysicalCardGroup getDiscardToScorePointsGroup(DefaultGame cardGame,
                                                          PhysicalCard card, String performingPlayerId)
            throws PlayerNotFoundException {
        return cardGame.getPlayer(card.getOwnerName()).getCardGroup(Zone.DISCARD);
    }
}