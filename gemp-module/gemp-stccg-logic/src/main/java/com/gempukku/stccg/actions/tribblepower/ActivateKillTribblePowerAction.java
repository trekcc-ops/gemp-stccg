package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.actions.discard.DiscardCardsFromEndOfCardPileEffect;
import com.gempukku.stccg.actions.draw.DrawCardAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ActivateKillTribblePowerAction extends ActivateTribblePowerAction {

    public ActivateKillTribblePowerAction(TribblesActionContext actionContext, TribblePower power) {
        super(actionContext, power);
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Action cost = getNextCost();
        if (cost != null)
            return cost;

        // Choose a player...
        String[] players = cardGame.getAllPlayerIds();
        if (players.length == 1)
            new DiscardCardsFromEndOfCardPileEffect(
                    Zone.PLAY_PILE, EndOfPile.TOP, cardGame.getPlayer(players[0]), _performingCard).playEffect();
        else
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new MultipleChoiceAwaitingDecision(cardGame.getPlayer(_performingPlayerId),
                            "Choose a player to shuffle his or her discard pile into his or her draw deck",
                            players) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            Player targetPlayer = cardGame.getPlayer(result);
                            new DiscardCardsFromEndOfCardPileEffect(
                                    Zone.PLAY_PILE, EndOfPile.TOP, targetPlayer, _performingCard).playEffect();
                        }
                    });

        return getNextAction();
    }

}