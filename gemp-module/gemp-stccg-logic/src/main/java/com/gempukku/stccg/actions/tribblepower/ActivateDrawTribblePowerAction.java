package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.draw.DrawCardAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;


public class ActivateDrawTribblePowerAction extends ActivateTribblePowerAction {

    private enum Progress { playerSelected }
    public ActivateDrawTribblePowerAction(TribblesActionContext actionContext, TribblePower power)
            throws InvalidGameLogicException {
        super(actionContext, power, Progress.values());
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!getProgress(Progress.playerSelected)) {
            String[] players = cardGame.getAllPlayerIds();
            if (players.length == 1)
                playerChosen(players[0], cardGame);
            else
                cardGame.getUserFeedback().sendAwaitingDecision(
                        new MultipleChoiceAwaitingDecision(cardGame.getPlayer(_performingPlayerId),
                                "Choose a player", players) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                playerChosen(result, cardGame);
                            }
                        });
            setProgress(Progress.playerSelected);
        }
        return getNextAction();
    }

    private void playerChosen(String playerId, DefaultGame game) {
        appendAction(new DrawCardAction(_performingCard, game.getPlayer(playerId)));
    }

}