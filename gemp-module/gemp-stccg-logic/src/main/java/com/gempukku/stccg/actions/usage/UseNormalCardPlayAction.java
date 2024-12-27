package com.gempukku.stccg.actions.usage;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

public class UseNormalCardPlayAction extends ActionyAction {
    private boolean _wasCarriedOut;
    private final PhysicalCard _performingCard;

    public UseNormalCardPlayAction(PhysicalCard performingCard, Player performingPlayer) {
        super(performingPlayer, ActionType.USAGE_LIMIT);
        _performingCard = performingCard;
    }

    @Override
    public String getActionSelectionText(DefaultGame cardGame) { return "Use normal card play"; }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public boolean wasCarriedOut() { return _wasCarriedOut; }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        return cardGame.getModifiersQuerying().getNormalCardPlaysAvailable(performingPlayer) >= 1;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        cardGame.getModifiersEnvironment().useNormalCardPlay(performingPlayer);
        cardGame.sendMessage("Normal card play used");
        _wasCarriedOut = true;
        return getNextAction();
    }
}