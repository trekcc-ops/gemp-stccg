package com.gempukku.lotro.effects.choose;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.PlayUtils;
import com.gempukku.lotro.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.actions.lotronly.PlayPermanentAction;
import com.gempukku.lotro.modifiers.ModifierFlag;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.UnrespondableEffect;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ChooseAndPlayCardFromDeckEffect implements Effect {
    private final String _playerId;
    private final Filterable[] _filter;
    private final int _twilightModifier;
    private CostToEffectAction _playCardAction;

    public ChooseAndPlayCardFromDeckEffect(String playerId, Filterable... filter) {
        this(playerId, 0, filter);
    }

    public ChooseAndPlayCardFromDeckEffect(String playerId, int twilightModifier, Filterable... filter) {
        _playerId = playerId;
        _filter = filter;
        _twilightModifier = twilightModifier;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK);
    }

    @Override
    public String getText(DefaultGame game) {
        return "Play card from deck";
    }

    @Override
    public void playEffect(final DefaultGame game) {
        if (isPlayableInFull(game)) {
            Collection<LotroPhysicalCard> deck = Filters.filter(game.getGameState().getDeck(_playerId), game, Filters.and(_filter, Filters.playable(game, _twilightModifier)));
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new ArbitraryCardsSelectionDecision(1, "Choose a card to play", new LinkedList<>(deck), 0, 1) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            List<LotroPhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                            if (selectedCards.size() > 0) {
                                final LotroPhysicalCard selectedCard = selectedCards.get(0);
                                cardSelectedToPlay(game, selectedCard);
                            }
                        }
                    });
        }
    }

    protected void cardSelectedToPlay(DefaultGame game, final LotroPhysicalCard selectedCard) {
        _playCardAction = PlayUtils.getPlayCardAction(game, selectedCard, _twilightModifier, Filters.any, false);
        _playCardAction.appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(DefaultGame game) {
                        afterCardPlayed(selectedCard);
                    }
                });
        game.getActionsEnvironment().addActionToStack(_playCardAction);
    }

    protected void afterCardPlayed(LotroPhysicalCard cardPlayed) {
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