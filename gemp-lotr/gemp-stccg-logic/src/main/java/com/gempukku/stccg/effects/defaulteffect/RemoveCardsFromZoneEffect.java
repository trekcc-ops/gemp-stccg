package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collection;

public class RemoveCardsFromZoneEffect extends DefaultEffect {
    private final String _playerPerforming;
    private final PhysicalCard _source;
    private final Collection<PhysicalCard> _cardsToRemove;
    private final Zone _fromZone;
    private final DefaultGame _game;

    public RemoveCardsFromZoneEffect(DefaultGame game, String playerPerforming, PhysicalCard source,
                                     Collection<PhysicalCard> cardsToRemove, Zone fromZone) {
        _playerPerforming = playerPerforming;
        _source = source;
        _cardsToRemove = cardsToRemove;
        _fromZone = fromZone;
        _game = game;
    }

    public RemoveCardsFromZoneEffect(ActionContext actionContext, Collection<PhysicalCard> cardsToRemove, Zone fromZone) {
        _playerPerforming = actionContext.getPerformingPlayer();
        _source = actionContext.getSource();
        _game = actionContext.getGame();
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
                _game.getGameState().addCardToZone(_game, removedCard, Zone.REMOVED);

            _game.getGameState().sendMessage(_playerPerforming + " removed " +
                    GameUtils.getAppendedNames(_cardsToRemove) + " from discard using " + GameUtils.getCardLink(_source));

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
