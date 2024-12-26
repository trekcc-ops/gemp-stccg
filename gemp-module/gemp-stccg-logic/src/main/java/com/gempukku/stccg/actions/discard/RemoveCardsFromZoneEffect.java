package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;

import java.util.Collection;

public class RemoveCardsFromZoneEffect extends DefaultEffect {
    private final String _playerPerforming;
    private final PhysicalCard _source;
    private final Collection<PhysicalCard> _cardsToRemove;
    private final Zone _fromZone;

    public RemoveCardsFromZoneEffect(ActionContext actionContext, Collection<PhysicalCard> cardsToRemove, Zone fromZone) {
        super(actionContext);
        _playerPerforming = actionContext.getPerformingPlayerId();
        _source = actionContext.getSource();
        _cardsToRemove = cardsToRemove;
        _fromZone = fromZone;
    }

    @Override
    public boolean isPlayableInFull() {
        return _cardsToRemove.stream().noneMatch(physicalCard -> physicalCard.getZone() != _fromZone);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            _game.getGameState().removeCardsFromZone(_playerPerforming, _cardsToRemove);
            for (PhysicalCard removedCard : _cardsToRemove)
                _game.getGameState().addCardToZone(removedCard, Zone.REMOVED);

            _game.sendMessage(_playerPerforming + " removed " +
                    TextUtils.getConcatenatedCardLinks(_cardsToRemove) + " from discard using " + _source.getCardLink());

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}