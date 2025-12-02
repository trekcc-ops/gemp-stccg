package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AllPlayersDiscardFromHandAction extends ActionyAction {
    private final PhysicalCard _performingCard;
    private final boolean _allPlayersMustBeAble;
    private final boolean _forced;

    public AllPlayersDiscardFromHandAction(DefaultGame game, Action action, PhysicalCard performingCard,
                                           boolean allPlayersMustBeAble, boolean forced)
            throws PlayerNotFoundException {
        super(game, game.getPlayer(action.getPerformingPlayerId()), ActionType.ALL_PLAYERS_DISCARD);
        _performingCard = performingCard;
        _allPlayersMustBeAble = allPlayersMustBeAble;
        _forced = forced;
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return _allPlayersMustBeAble ?
                cardGame.getPlayers().stream().noneMatch(player -> player.getCardsInHand().isEmpty()) :
                cardGame.getPlayers().stream().anyMatch(player -> !player.getCardsInHand().isEmpty());
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {

        for (Player player : cardGame.getPlayers()) {
            Collection<PhysicalCard> hand = Filters.filter(player.getCardsInHand(), cardGame, Filters.any);
            if (hand.size() == 1) {
                discardCards(cardGame, player, player.getCardsInHand());
            } else {
                cardGame.getUserFeedback().sendAwaitingDecision(
                        new CardsSelectionDecision(player, "Choose a card to discard", hand,
                                1, 1, cardGame) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Set<PhysicalCard> cards = getSelectedCardsByResponse(result);
                                discardCards(cardGame, player, cards);
                            }
                        });
            }

        }
        setAsSuccessful();
        return getNextAction();
    }

    private boolean canDiscard(DefaultGame cardGame, String playerId) {
        return !_forced || cardGame.getGameState().getModifiersQuerying().canDiscardCardsFromHand(playerId, _performingCard);
    }

    private void discardCards(DefaultGame game, Player discardingPlayer, Collection<PhysicalCard> cards) {
        if (canDiscard(game, discardingPlayer.getPlayerId())) {
            GameState gameState = game.getGameState();
            Set<PhysicalCard> discardedCards = new HashSet<>(cards);

            gameState.removeCardsFromZoneWithoutSendingToClient(game, discardedCards);
            for (PhysicalCard card : discardedCards) {
                gameState.addCardToTopOfDiscardPile(card);
                saveResult(new DiscardCardFromHandResult(card, this));
            }
        }
    }


}