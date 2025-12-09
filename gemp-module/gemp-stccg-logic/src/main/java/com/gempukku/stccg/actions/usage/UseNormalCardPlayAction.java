package com.gempukku.stccg.actions.usage;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;

public class UseNormalCardPlayAction extends ActionyAction {

    public UseNormalCardPlayAction(DefaultGame cardGame, Player performingPlayer) {
        super(cardGame, performingPlayer, ActionType.USAGE_LIMIT);
    }

    public UseNormalCardPlayAction(DefaultGame cardGame, String performingPlayerName) {
        super(cardGame, performingPlayerName, ActionType.USAGE_LIMIT);
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return cardGame.getGameState().getNormalCardPlaysAvailable(_performingPlayerId) >= 1;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        cardGame.getGameState().useNormalCardPlay(_performingPlayerId);
        setAsSuccessful();
        return getNextAction();
    }
}