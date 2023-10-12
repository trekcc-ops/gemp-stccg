package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.PlayPermanentAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.ModifierFlag;
import com.gempukku.stccg.rules.PlayUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ChooseAndPlayCardFromZoneEffect implements Effect {
    private final String _playerId;
    private final boolean _ignoreRoamingPenalty;
    private final boolean _ignoreCheckingDeadPile;
    private final Filter _filter;
    private final int _twilightModifier;
    private CostToEffectAction _playCardAction;
    private final Zone _fromZone;
    private final DefaultGame _game;

    public ChooseAndPlayCardFromZoneEffect(Zone fromZone, String playerId, DefaultGame game, Filterable... filters) {
        this(fromZone, playerId, game, 0, false, filters);
    }

    public ChooseAndPlayCardFromZoneEffect(Zone fromZone, String playerId, DefaultGame game, int twilightModifier, Filterable... filters) {
        this(fromZone, playerId, game, twilightModifier, false, filters);
    }

    public ChooseAndPlayCardFromZoneEffect(Zone fromZone, String playerId, DefaultGame game, int twilightModifier, boolean ignoreRoamingPenalty, Filterable... filters) {
        this(fromZone, playerId, game, twilightModifier, ignoreRoamingPenalty, false, filters);
    }

    public ChooseAndPlayCardFromZoneEffect(Zone fromZone, String playerId, DefaultGame game, int twilightModifier, boolean ignoreRoamingPenalty, boolean ignoreCheckingDeadPile, Filterable... filters) {
        _playerId = playerId;
        _ignoreRoamingPenalty = ignoreRoamingPenalty;
        _ignoreCheckingDeadPile = ignoreCheckingDeadPile;

                // FOR PLAYFROMHANDEFFECT
        // Card has to be in hand when you start playing the card (we need to copy the collection)
        _filter = Filters.and(filters, Filters.in(new LinkedList<>(game.getGameState().getHand(playerId))));

        _twilightModifier = twilightModifier;
        _fromZone = fromZone;
        _game = game;
    }

    @Override
    public String getText() {
        return "Play card from " + _fromZone.getHumanReadable();
    }

    private Collection<PhysicalCard> getPlayableCards() {
        if (_fromZone == Zone.HAND) {
            return Filters.filter(_game.getGameState().getHand(_playerId), _game, _filter, Filters.playable(_twilightModifier, _ignoreRoamingPenalty, _ignoreCheckingDeadPile));
        } else if (_fromZone == Zone.DISCARD) {
            return Filters.filter(_game.getGameState().getDiscard(_playerId), _game, _filter, Filters.playable(_game, _twilightModifier));
        } else if (_fromZone == Zone.DRAW_DECK) {
            return Filters.filter(_game.getGameState().getDrawDeck(_playerId), _game,
                    Filters.and(_filter, Filters.playable(_game, _twilightModifier)));
        }
        return null;
    }

    @Override
    public boolean isPlayableInFull() {
        if (_fromZone == Zone.DISCARD || _fromZone == Zone.DRAW_DECK)
            return !_game.getModifiersQuerying().hasFlagActive(_game, ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK) && getPlayableCards().size() > 0;
        else
            return getPlayableCards().size() > 0;
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
                int minimum;
                if (_fromZone == Zone.DISCARD) minimum = 1;
                else minimum = 0;
                _game.getUserFeedback().sendAwaitingDecision(_playerId,
                        new ArbitraryCardsSelectionDecision(1, "Choose a card to play", new LinkedList<>(playableCards), minimum, 1) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                List<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                                if (selectedCards.size() > 0) {
                                    final PhysicalCard selectedCard = selectedCards.get(0);
                                    playCard(_game, selectedCard);
                                }
                            }
                        }); // Additional implementations are for playing from hand
            } else if (playableCards.size() == 1) {
                playCard(_game, playableCards.iterator().next());
            } else if (playableCards.size() > 1) {
                _game.getUserFeedback().sendAwaitingDecision(_playerId,
                        new CardsSelectionDecision(1, "Choose a card to play", playableCards, 1, 1) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                final PhysicalCard selectedCard = getSelectedCardsByResponse(result).iterator().next();
                                playCard(_game, selectedCard);
                            }
                        });
            }
        }
    }

    private void playCard(DefaultGame game, final PhysicalCard selectedCard) {
        _playCardAction = PlayUtils.getPlayCardAction(game, selectedCard, _twilightModifier, Filters.any, _ignoreRoamingPenalty);
        _playCardAction.appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect() {
                        afterCardPlayed(selectedCard);
                    }
                });
        game.getActionsEnvironment().addActionToStack(_playCardAction);
    }

    protected void afterCardPlayed(PhysicalCard cardPlayed) {
    }

    @Override
    public boolean wasCarriedOut() {
        if (_playCardAction == null)
            return false;
        if (_playCardAction instanceof PlayPermanentAction)
            return _playCardAction.wasCarriedOut();
        return true;
    }
}
