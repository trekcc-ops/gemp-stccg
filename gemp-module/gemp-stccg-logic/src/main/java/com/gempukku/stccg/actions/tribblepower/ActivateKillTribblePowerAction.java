package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.choose.SelectPlayerAction;
import com.gempukku.stccg.actions.discard.TribblesMultiDiscardActionBroken;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.TopOfPlayPileFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Arrays;


public class ActivateKillTribblePowerAction extends ActivateTribblePowerAction {

    private enum Progress { playerSelected, wasCarriedOut }

    private String _targetPlayerName;
    private SelectPlayerAction _selectPlayerAction;

    public ActivateKillTribblePowerAction(TribblesGame cardGame, PhysicalCard performingCard,
                                          GameTextContext actionContext) {
        super(cardGame, actionContext, performingCard, Progress.values());
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
                _targetPlayerName = players[0];
                setProgress(Progress.playerSelected);
            }
            else {
                if (_selectPlayerAction == null) {
                    _selectPlayerAction =
                            new SelectPlayerAction(cardGame, _actionContext, "selectedPlayer", Arrays.asList(players));
                    return _selectPlayerAction;
                } else {
                    _targetPlayerName = _actionContext.getValueFromMemory("selectedPlayer");
                    setProgress(Progress.playerSelected);
                }
            }
        }

        if (!getProgress(Progress.wasCarriedOut)) {
            setProgress(Progress.wasCarriedOut);
            return new TribblesMultiDiscardActionBroken(cardGame, _performingCard, _targetPlayerName,
                    new TopOfPlayPileFilter(_targetPlayerName));
        }
        return getNextAction();
    }

}