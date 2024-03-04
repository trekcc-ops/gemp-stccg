package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectType;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.ModifierFlag;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ChooseAndPlayCardFromZoneEffect implements Effect {
    private final String _playerId;
    private final Filter _filter;
    private CostToEffectAction _playCardAction;
    private final Zone _fromZone;
    private final DefaultGame _game;
    private Filterable _destinationFilter;

    public ChooseAndPlayCardFromZoneEffect(Zone fromZone, Player player, Filterable playableCardFilter) {
        this(fromZone, player.getPlayerId(), player.getGame(), playableCardFilter, Filters.any);
    }


    public ChooseAndPlayCardFromZoneEffect(Zone fromZone, String playerId, DefaultGame game,
                                           Filterable playableCardFilter, Filterable destinationFilter) {
        _destinationFilter = destinationFilter;
        _playerId = playerId;

        // FOR PLAYFROMHANDEFFECT
        // Card has to be in hand when you start playing the card (we need to copy the collection)
        _filter = Filters.and(playableCardFilter, Filters.in(new LinkedList<>(game.getGameState().getHand(playerId))));

        _fromZone = fromZone;
        _game = game;
    }

    @Override
    public String getText() {
        return "Play card from " + _fromZone.getHumanReadable();
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

    @Override
    public boolean isPlayableInFull() {
        if (_fromZone == Zone.DISCARD || _fromZone == Zone.DRAW_DECK)
            return !_game.getModifiersQuerying().hasFlagActive(ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK) &&
                    !getPlayableCards().isEmpty();
        else
            return !getPlayableCards().isEmpty();
    }

    @Override
    public EffectType getType() {
        return null;
    }

    @Override
    public void playEffect() {
        if (isPlayableInFull()) {
            Collection<PhysicalCard> playableCards = getPlayableCards();
            if (_fromZone == Zone.DISCARD || _fromZone == Zone.DRAW_DECK) {
                int minimum = _fromZone == Zone.DISCARD ? 1 : 0;
                _game.getUserFeedback().sendAwaitingDecision(_playerId,
                        new ArbitraryCardsSelectionDecision(1, "Choose a card to play", 
                                new LinkedList<>(playableCards), minimum, 1) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                List<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                                if (!selectedCards.isEmpty()) {
                                    final PhysicalCard selectedCard = selectedCards.get(0);
                                    playCard(selectedCard);
                                }
                            }
                        }); // Additional implementations are for playing from hand
            } else if (playableCards.size() == 1) {
                playCard(playableCards.iterator().next());
            } else if (playableCards.size() > 1) {
                _game.getUserFeedback().sendAwaitingDecision(_playerId,
                        new CardsSelectionDecision(1, "Choose a card to play", playableCards, 
                                1, 1) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                final PhysicalCard selectedCard = getSelectedCardsByResponse(result).iterator().next();
                                playCard(selectedCard);
                            }
                        });
            }
        }
    }

    protected void playCard(final PhysicalCard selectedCard) {
        _playCardAction = selectedCard.getPlayCardAction(_destinationFilter);
        _playCardAction.appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect() {
                        afterCardPlayed(selectedCard);
                    }
                });
        _game.getActionsEnvironment().addActionToStack(_playCardAction);
    }

    protected void afterCardPlayed(PhysicalCard cardPlayed) {
    }

    @Override
    public boolean wasCarriedOut() {
        if (_playCardAction == null)
            return false;
        if (_playCardAction instanceof PlayCardAction)
            return _playCardAction.wasCarriedOut();
        return true;
    }

    public void addDestinationFilter(Filterable filter) {
        _destinationFilter = Filters.and(_destinationFilter, filter);
    }

    public String getPerformingPlayerId() { return _playerId; }

    protected CostToEffectAction getPlayCardAction() { return _playCardAction; }
    protected void setPlayCardAction(CostToEffectAction action) { _playCardAction = action; }
}
