package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.draw.DrawSingleCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.PlayerNotFoundException;


public class ActivateDrawTribblePowerAction extends ActivateTribblePowerAction {

    private enum Progress { playerSelected }
    public ActivateDrawTribblePowerAction(TribblesGame cardGame, PhysicalCard performingCard,
                                          ActionContext actionContext) {
        super(cardGame, actionContext, performingCard, Progress.values());
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (!getProgress(Progress.playerSelected)) {
            String[] players = cardGame.getAllPlayerIds();
            if (players.length == 1)
                playerChosen(players[0], cardGame);
            else
                cardGame.sendAwaitingDecision(
                        new MultipleChoiceAwaitingDecision(cardGame.getPlayer(_performingPlayerId),
                                "Choose a player", players, cardGame) {
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
        appendEffect(new DrawSingleCardAction(game, playerId));
    }

}