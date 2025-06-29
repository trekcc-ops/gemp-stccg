package com.gempukku.stccg.actions.usage;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class UseNormalCardPlayAction extends ActionyAction {

    public UseNormalCardPlayAction(DefaultGame cardGame, Player performingPlayer) {
        super(cardGame, performingPlayer, ActionType.USAGE_LIMIT);
    }

    @Override
    public boolean wasCarriedOut() { return _wasCarriedOut; }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        try {
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            return cardGame.getGameState().getModifiersQuerying().getNormalCardPlaysAvailable(performingPlayer) >= 1;
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        cardGame.getModifiersEnvironment().useNormalCardPlay(performingPlayer);
        _wasCarriedOut = true;
        setAsSuccessful();
        return getNextAction();
    }
}