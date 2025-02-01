package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.PlayerNotFoundException;

import javax.xml.datatype.DatatypeConfigurationException;


public class ActivateDrawTribblePowerAction extends ActivateTribblePowerAction {

    private enum Progress { playerSelected }
    public ActivateDrawTribblePowerAction(TribblesActionContext actionContext, TribblePower power)
            throws InvalidGameLogicException, PlayerNotFoundException {
        super(actionContext, power, Progress.values());
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