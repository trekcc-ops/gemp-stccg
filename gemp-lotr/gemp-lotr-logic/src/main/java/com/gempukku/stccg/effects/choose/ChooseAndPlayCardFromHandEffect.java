package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.PlayUtils;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.PlayPermanentAction;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.UnrespondableEffect;

import java.util.Collection;
import java.util.LinkedList;

public class ChooseAndPlayCardFromHandEffect implements Effect {
    private final String _playerId;
    private final boolean _ignoreRoamingPenalty;
    private final boolean _ignoreCheckingDeadPile;
    private final Filter _filter;
    private final int _twilightModifier;
    private CostToEffectAction _playCardAction;

    public ChooseAndPlayCardFromHandEffect(String playerId, DefaultGame game, Filterable... filters) {
        this(playerId, game, 0, false, filters);
    }

    public ChooseAndPlayCardFromHandEffect(String playerId, DefaultGame game, int twilightModifier, Filterable... filters) {
        this(playerId, game, twilightModifier, false, filters);
    }

    public ChooseAndPlayCardFromHandEffect(String playerId, DefaultGame game, int twilightModifier, boolean ignoreRoamingPenalty, Filterable... filters) {
        this(playerId, game, twilightModifier, ignoreRoamingPenalty, false, filters);
    }

    public ChooseAndPlayCardFromHandEffect(String playerId, DefaultGame game, int twilightModifier, boolean ignoreRoamingPenalty, boolean ignoreCheckingDeadPile, Filterable... filters) {
        _playerId = playerId;
        _ignoreRoamingPenalty = ignoreRoamingPenalty;
        _ignoreCheckingDeadPile = ignoreCheckingDeadPile;
        // Card has to be in hand when you start playing the card (we need to copy the collection)
        _filter = Filters.and(filters, Filters.in(new LinkedList<PhysicalCard>(game.getGameState().getHand(playerId))));
        _twilightModifier = twilightModifier;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Play card from hand";
    }

    private Collection<PhysicalCard> getPlayableInHandCards(DefaultGame game) {
        return Filters.filter(game.getGameState().getHand(_playerId), game, _filter, Filters.playable(game, _twilightModifier, _ignoreRoamingPenalty, _ignoreCheckingDeadPile));
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return getPlayableInHandCards(game).size() > 0;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public void playEffect(final DefaultGame game) {
        Collection<PhysicalCard> playableInHand = getPlayableInHandCards(game);
        if (playableInHand.size() == 1)
            playCard(game, playableInHand.iterator().next());
        else if (playableInHand.size() > 1) {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardsSelectionDecision(1, "Choose a card to play", playableInHand, 1, 1) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            final PhysicalCard selectedCard = getSelectedCardsByResponse(result).iterator().next();
                            playCard(game, selectedCard);
                        }
                    });
        }
    }

    private void playCard(DefaultGame game, final PhysicalCard selectedCard) {
        _playCardAction = PlayUtils.getPlayCardAction(game, selectedCard, _twilightModifier, Filters.any, _ignoreRoamingPenalty);
        _playCardAction.appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(DefaultGame game) {
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
