package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.choose.SelectPlayerAction;
import com.gempukku.stccg.actions.discard.TribblesMultiDiscardActionBroken;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.filters.TopOfPlayPileFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Arrays;


public class ActivateKillTribblePowerAction extends ActivateTribblePowerAction {

    private enum Progress { playerSelected, wasCarriedOut }
    private Player _targetPlayer;
    private SelectPlayerAction _selectPlayerAction;
    private final TribblesActionContext _actionContext;

    public ActivateKillTribblePowerAction(TribblesGame cardGame, TribblesActionContext actionContext)
            throws InvalidGameLogicException, PlayerNotFoundException {
        super(cardGame, actionContext, TribblePower.KILL, Progress.values());
        _actionContext = actionContext;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!getProgress(Progress.playerSelected)) {
            // Choose a player...
            String[] players = cardGame.getAllPlayerIds();
            if (players.length == 1) {
                _targetPlayer = cardGame.getPlayer(players[0]);
                setProgress(Progress.playerSelected);
            }
            else {
                if (_selectPlayerAction == null) {
                    _selectPlayerAction =
                            new SelectPlayerAction(cardGame, _actionContext, "selectedPlayer", Arrays.asList(players));
                    return _selectPlayerAction;
                } else {
                    String targetPlayerId = _actionContext.getValueFromMemory("selectedPlayer");
                    _targetPlayer = cardGame.getPlayer(targetPlayerId);
                    setProgress(Progress.playerSelected);
                }
            }
        }

        if (!getProgress(Progress.wasCarriedOut)) {
            setProgress(Progress.wasCarriedOut);
            return new TribblesMultiDiscardActionBroken(_performingCard, _targetPlayer, new TopOfPlayPileFilter(_targetPlayer));
        }
        return getNextAction();
    }

}