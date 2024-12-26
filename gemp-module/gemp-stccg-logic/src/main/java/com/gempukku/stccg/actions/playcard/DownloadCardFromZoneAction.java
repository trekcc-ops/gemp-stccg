package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.ModifierFlag;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DownloadCardFromZoneAction extends ActionyAction {
    private final String _playerId;
    private final Filter _filter;
    private Action _playCardAction;
    private final Zone _fromZone;
    private final DefaultGame _game;
    private final Filterable _destinationFilter;
    private final PhysicalCard _actionSource;

    public DownloadCardFromZoneAction(Zone fromZone, Player player, PhysicalCard actionSource,
                                      Filterable playableCardFilter) {
        this(fromZone, player, actionSource, playableCardFilter, Filters.any);
    }

    public DownloadCardFromZoneAction(Zone fromZone, Player player, PhysicalCard actionSource,
                                      Filterable playableCardFilter, Filterable destinationFilter) {
        super(player, "Download card from " + fromZone.getHumanReadable(),
                ActionType.DOWNLOAD_CARD);
        _destinationFilter = destinationFilter;
        _playerId = player.getPlayerId();
        _actionSource = actionSource;

        // FOR PLAYFROMHANDEFFECT
        // Card has to be in hand when you start playing the card (we need to copy the collection)
        _filter = Filters.and(playableCardFilter);

        _fromZone = fromZone;
        _game = player.getGame();
    }

    protected Collection<PhysicalCard> getPlayableCards() {
        List<PhysicalCard> sourceCards;
        if (_fromZone == Zone.HAND)
            sourceCards = _game.getGameState().getHand(_playerId);
        else if (_fromZone == Zone.DISCARD)
            sourceCards = _game.getGameState().getDiscard(_playerId);
        else if (_fromZone == Zone.DRAW_DECK)
            sourceCards = _game.getGameState().getDrawDeck(_playerId);
        else throw new RuntimeException(
                "Error in ChooseAndPlayCardFromZoneEffect processing for zone " + _fromZone.getHumanReadable());

        return Filters.filter(sourceCards, _game, _filter, Filters.playable);
    }

    protected void playCard(final PhysicalCard selectedCard) {
        _playCardAction = selectedCard.getPlayCardAction(true);
        _game.getActionsEnvironment().addActionToStack(_playCardAction);
    }

    @Override
    public boolean wasCarriedOut() {
        if (_playCardAction == null)
            return false;
        if (_playCardAction instanceof PlayCardAction)
            return _playCardAction.wasCarriedOut();
        return true;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        if (_fromZone == Zone.DISCARD || _fromZone == Zone.DRAW_DECK)
            return !_game.getModifiersQuerying().hasFlagActive(ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK) &&
                    !getPlayableCards().isEmpty();
        else
            return !getPlayableCards().isEmpty();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Player performingPlayer = cardGame.getPlayer(_playerId);
        Collection<PhysicalCard> playableCards = getPlayableCards();
        if (_fromZone == Zone.DISCARD || _fromZone == Zone.DRAW_DECK) {
            int minimum = _fromZone == Zone.DISCARD ? 1 : 0;
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new ArbitraryCardsSelectionDecision(performingPlayer, "Choose a card to play",
                            new LinkedList<>(playableCards), minimum, 1) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            List<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                            if (!selectedCards.isEmpty()) {
                                final PhysicalCard selectedCard = selectedCards.getFirst();
                                playCard(selectedCard);
                            }
                        }
                    }); // Additional implementations are for playing from hand
        } else if (playableCards.size() == 1) {
            playCard(playableCards.iterator().next());
        } else if (playableCards.size() > 1) {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new CardsSelectionDecision(performingPlayer, "Choose a card to play", playableCards,
                            1, 1) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            final PhysicalCard selectedCard = getSelectedCardsByResponse(result).iterator().next();
                            playCard(selectedCard);
                        }
                    });
        }
        return null;
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _actionSource;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _actionSource;
    }

    protected Action getPlayCardAction() { return _playCardAction; }
    protected void setPlayCardAction(Action action) { _playCardAction = action; }

    public String getPerformingPlayerId() { return _playerId; }

    public DefaultGame getGame() { return _game; }
}