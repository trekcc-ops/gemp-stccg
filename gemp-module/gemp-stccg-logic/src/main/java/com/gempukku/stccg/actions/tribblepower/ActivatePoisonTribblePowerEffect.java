package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.discard.DiscardCardsFromEndOfCardPileEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.TribblesGame;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ActivatePoisonTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivatePoisonTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        // Choose any opponent who still has card(s) in their draw deck.
        List<String> playersWithCards = new ArrayList<>();
        for (String player : _game.getAllPlayerIds()) {
            if ((!_game.getGameState().getDrawDeck(player).isEmpty()) && !Objects.equals(player, _activatingPlayer))
                playersWithCards.add(player);
        }
        String[] playersWithCardsArr = playersWithCards.toArray(new String[0]);
        if (playersWithCardsArr.length == 1)
            playerChosen(playersWithCardsArr[0], _game);
        else
            _game.getUserFeedback().sendAwaitingDecision(_activatingPlayer,
                    new MultipleChoiceAwaitingDecision("Choose a player", playersWithCardsArr) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result, _game);
                        }
                    });
        _game.getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }

    private void playerChosen(String chosenPlayer, TribblesGame game) {
        // That opponent must discard the top card
        SubAction subAction = _action.createSubAction();
        subAction.appendEffect(new DiscardCardsFromEndOfCardPileEffect(_game, _source, Zone.DRAW_DECK, EndOfPile.TOP,
                chosenPlayer, 1, true) {
            @Override
            protected void cardsDiscardedCallback(Collection<PhysicalCard> cards) {

                // and you immediately score points equal to the number of tribbles on that card
                PhysicalCard card = Iterables.getOnlyElement(cards);
                game.getGameState().addToPlayerScore(_activatingPlayer, card.getBlueprint().getTribbleValue());
            }
        });
        game.getActionsEnvironment().addActionToStack(subAction);
    }
}