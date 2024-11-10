package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.List;

public class EncounterSeedCardAction extends ActionyAction {
    private boolean _seedCardWasRevealed;
    private final List<String> _seedCards;

    public EncounterSeedCardAction(Player player, List<String> seedCards) {
        super(player, ActionType.OTHER);
            // TODO - can get attemptingUnit from action.getAttemptingEntity
        _seedCards = seedCards;
    }

    @Override
    public PhysicalCard getActionSource() {
        return null;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return null;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {

        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_seedCardWasRevealed) {
            String cardEncountered = _seedCards.getFirst();
            cardGame.sendMessage("Seed card encountered: " + cardEncountered);
            _seedCardWasRevealed = true;
            _seedCards.removeFirst();
        }

        return getNextAction();
    }

}