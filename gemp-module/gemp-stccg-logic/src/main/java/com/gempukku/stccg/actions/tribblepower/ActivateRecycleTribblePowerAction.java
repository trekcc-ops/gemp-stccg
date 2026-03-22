package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.placecard.ShuffleCardsIntoDrawDeckAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.filters.InYourDiscardFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.List;


public class ActivateRecycleTribblePowerAction extends ActivateTribblePowerAction {

    public ActivateRecycleTribblePowerAction(TribblesGame cardGame, PhysicalCard performingCard,
                                             GameTextContext actionContext) throws PlayerNotFoundException {
        super(cardGame, actionContext, performingCard);
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws PlayerNotFoundException {
        // Choose a player to shuffle his or her discard pile into his or her draw deck
        List<String> playersWithCards = new ArrayList<>();
        for (Player player : cardGame.getPlayers()) {
            if (player.getCardsInGroup(Zone.DISCARD).isEmpty())
                playersWithCards.add(player.getPlayerId());
        }
        if (playersWithCards.size() == 1)
            playerChosen(cardGame, playersWithCards.getFirst());
        else
            cardGame.sendAwaitingDecision(
                    new MultipleChoiceAwaitingDecision(cardGame.getPlayer(_performingPlayerId), "Choose a player",
                            playersWithCards, cardGame) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(cardGame, result);
                        }
                    });
        return getNextAction();
    }

    private void playerChosen(DefaultGame cardGame, String chosenPlayer) {
        Action shuffleAction = new ShuffleCardsIntoDrawDeckAction(cardGame,
                _performingCard, chosenPlayer, new InYourDiscardFilter(chosenPlayer));
        appendEffect(shuffleAction);
    }



}