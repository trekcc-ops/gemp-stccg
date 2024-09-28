package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Preventable;
import com.gempukku.stccg.TextUtils;

import java.util.Collections;

public class PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect extends DefaultEffect implements Preventable {
    private final String _playerId;
    private final int _count;
    private boolean _prevented;
    private final DefaultGame _game;

    public PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect(ActionContext actionContext, String playerId, int count) {
        super(playerId);
        _game = actionContext.getGame();
        _playerId = playerId;
        _count = count;
    }
    public PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect(DefaultGame game, String playerId, int count) {
        super(playerId);
        _game = game;
        _playerId = playerId;
        _count = count;
    }

    @Override
    public String getText() {
        return "Place " + TextUtils.plural(_count, "card") + " from top of draw deck on top of play pile";
    }
    @Override
    public boolean isPlayableInFull() {
        return _game.getGameState().getDrawDeck(_playerId).size() >= _count;
    }

    @Override
    public String getPerformingPlayerId() {
        return _playerId;
    }

    @Override
    public FullEffectResult playEffectReturningResult() {
        int drawn = 0;

        while ((drawn < _count) && (!_prevented) && (!_game.getGameState().getDrawDeck(_playerId).isEmpty())) {
            PhysicalCard card = _game.getGameState().getDrawDeck(_playerId).getFirst();
            _game.getGameState().removeCardsFromZone(null, Collections.singleton(card));
            _game.getGameState().addCardToZone(card, Zone.PLAY_PILE);
            _game.sendMessage(card.getOwnerName() + " puts " + card.getCardLink() + " from the top of their draw deck on top of their play pile");
            drawn++;
        }

        if (drawn == _count) {
            return new FullEffectResult(true);
        } else
            return new FullEffectResult(false);
    }

    @Override
    public void prevent() {
        _prevented = true;
    }

    @Override
    public boolean isPrevented() {
        return _prevented;
    }
}
