package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.ActivateCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.LinkedList;
import java.util.List;

public class Blueprint155_026 extends CardBlueprint {
    // Get It Done
    Blueprint155_026() {
        super("155_021");
    }

    @Override
    public List<? extends ActivateCardAction> getGameTextActionsWhileInPlay(Player player, PhysicalCard card) {
        DefaultGame game = player.getGame();
        Phase currentPhase = game.getCurrentPhase();
        List<ActivateCardAction> actions = new LinkedList<>();

/*        if (currentPhase == Phase.EXECUTE_ORDERS) {
                // Choice1 - make a unique [TNG] personnel attributes +2 until end of turn
            Action choice1 = new AddUntilModifierEffect();
                // Choice2 - make a [TNG] ship RANGE +2 until end of turn
            Action choice2 = new AddUntilModifierEffect();
                // Choice3 - shuffle the bottom three personnel and/or ships from your discard pile into your draw deck
            Action choice3 = new Action();

            // add cost to all 3 actions - place two [TNG] cards from hand on bottom of your deck

            // after any use, discard incident OR discard a card from hand
        } */
        return actions;
    }
}