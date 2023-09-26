package com.gempukku.lotro.effects;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.game.TribblesGame;
import com.gempukku.lotro.rules.GameUtils;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ActivatePoisonTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivatePoisonTribblePowerEffect(CostToEffectAction action, LotroPhysicalCard source) {
        super(action, source);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(TribblesGame game) {
        // Choose any opponent who still has card(s) in their draw deck.
        List<String> playersWithCards = new ArrayList<>();
        for (String player : GameUtils.getAllPlayers(game)) {
            if ((game.getGameState().getDeck(player).size() > 0) && !Objects.equals(player, _activatingPlayer))
                playersWithCards.add(player);
        }
        String[] playersWithCardsArr = playersWithCards.toArray(new String[0]);
        if (playersWithCardsArr.length == 1)
            playerChosen(playersWithCardsArr[0], game);
        else
            game.getUserFeedback().sendAwaitingDecision(_activatingPlayer,
                    new MultipleChoiceAwaitingDecision(1, "Choose a player", playersWithCardsArr) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result, game);
                        }
                    });
        game.getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }

    private void playerChosen(String chosenPlayer, TribblesGame game) {
        // That opponent must discard the top card
        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(new DiscardTopCardFromDeckEffect(_source, chosenPlayer, 1, true) {
            @Override
            protected void cardsDiscardedCallback(Collection<LotroPhysicalCard> cards) {

                // and you immediately score points equal to the number of tribbles on that card
                LotroPhysicalCard card = Iterables.getOnlyElement(cards);
                game.getGameState().addToPlayerScore(_activatingPlayer, card.getBlueprint().getTribbleValue());
            }
        });
        game.getActionsEnvironment().addActionToStack(subAction);
    }
}