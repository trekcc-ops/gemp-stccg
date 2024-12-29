package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AllPlayersDiscardFromHandAction extends ActionyAction {
    private final PhysicalCard _performingCard;
    private final boolean _allPlayersMustBeAble;
    private final boolean _forced;

    public AllPlayersDiscardFromHandAction(DefaultGame game, Action action, boolean allPlayersMustBeAble,
                                           boolean forced) {
        super(game.getPlayer(action.getPerformingPlayerId()), ActionType.DISCARD);
        _performingCard = action.getPerformingCard();
        _allPlayersMustBeAble = allPlayersMustBeAble;
        _forced = forced;
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return _allPlayersMustBeAble ?
                cardGame.getPlayerIds().stream().noneMatch(player -> cardGame.getGameState().getHand(player).isEmpty()) :
                cardGame.getPlayerIds().stream().anyMatch(player -> !cardGame.getGameState().getHand(player).isEmpty());
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {

        for (String player : cardGame.getAllPlayerIds()) {
            Collection<PhysicalCard> hand = Filters.filter(cardGame.getGameState().getHand(player), cardGame, Filters.any);
            if (hand.size() == 1) {
                discardCards(cardGame, player, cardGame.getGameState().getHand(player));
            } else {
                cardGame.getUserFeedback().sendAwaitingDecision(
                        new CardsSelectionDecision(cardGame.getPlayer(player), "Choose a card to discard", hand,
                                1, 1) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Set<PhysicalCard> cards = getSelectedCardsByResponse(result);
                                discardCards(cardGame, player, cards);
                            }
                        });
            }

        }
        return getNextAction();
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    private boolean canDiscard(DefaultGame cardGame, String playerId) {
        return !_forced || cardGame.getModifiersQuerying().canDiscardCardsFromHand(playerId, _performingCard);
    }

    private void discardCards(DefaultGame game, String playerId, Collection<PhysicalCard> cards) {
        if (canDiscard(game, playerId)) {
            GameState gameState = game.getGameState();
            Set<PhysicalCard> discardedCards = new HashSet<>(cards);

            gameState.removeCardsFromZone(playerId, discardedCards);
            for (PhysicalCard card : discardedCards) {
                gameState.addCardToZone(card, Zone.DISCARD);
                game.getActionsEnvironment().emitEffectResult(new DiscardCardFromHandResult(_performingCard, card));
            }

            if (!discardedCards.isEmpty())
                gameState.sendMessage(playerId + " discarded " + TextUtils.getConcatenatedCardLinks(discardedCards) +
                        " from " + Zone.HAND.getHumanReadable());
        }
    }
}