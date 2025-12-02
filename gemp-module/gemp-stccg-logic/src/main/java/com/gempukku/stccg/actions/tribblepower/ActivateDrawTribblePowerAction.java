package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.PlayerNotFoundException;


public class ActivateDrawTribblePowerAction extends ActivateTribblePowerAction {

    private enum Progress { playerSelected }
    public ActivateDrawTribblePowerAction(TribblesGame cardGame, PhysicalCard performingCard, ActionContext actionContext)
            throws InvalidGameLogicException, PlayerNotFoundException {
        super(cardGame, actionContext, performingCard, Progress.values());
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (!getProgress(Progress.playerSelected)) {
            String[] players = cardGame.getAllPlayerIds();
            if (players.length == 1)
                playerChosen(players[0], cardGame);
            else
                cardGame.getUserFeedback().sendAwaitingDecision(
                        new MultipleChoiceAwaitingDecision(cardGame.getPlayer(_performingPlayerId),
                                "Choose a player", players, cardGame) {
                            @Override
                            protected void validDecisionMade(int index, String result)
                                    throws DecisionResultInvalidException {
                                try {
                                    playerChosen(result, cardGame);
                                } catch(PlayerNotFoundException exp) {
                                    throw new DecisionResultInvalidException(exp.getMessage());
                                }
                            }
                        });
            setProgress(Progress.playerSelected);
        }
        return getNextAction();
    }

    private void playerChosen(String playerId, DefaultGame game) throws PlayerNotFoundException {
        appendEffect(new DrawCardsAction(_performingCard, game.getPlayer(playerId)));
    }

}