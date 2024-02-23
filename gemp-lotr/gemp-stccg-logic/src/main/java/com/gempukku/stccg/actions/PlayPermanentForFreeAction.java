package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.PlayCardEffect;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collections;

public class PlayPermanentForFreeAction extends AbstractCostToEffectAction {
    private final PhysicalCard _permanentPlayed;
    private boolean _cardRemoved;
    private PlayCardEffect _playCardEffect;
    private boolean _cardPlayed;
    private final Zone _fromZone;
    private final Zone _toZone;
    private final DefaultGame _game;

    public PlayPermanentForFreeAction(PhysicalCard card, Zone zone) {
        _game = card.getGame();
        _permanentPlayed = card;
        setText("Play " + _permanentPlayed.getFullName());
        setPerformingPlayer(card.getOwnerName());

        _fromZone = card.getZone();
        _toZone = zone;
    }

    @Override
    public DefaultGame getGame() { return _game; }

    @Override
    public ActionType getActionType() {
        return ActionType.PLAY_CARD;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _permanentPlayed;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return _permanentPlayed;
    }

    @Override
    public Effect nextEffect() {
        if (!_cardRemoved) {
            _cardRemoved = true;
            final Zone playedFromZone = _permanentPlayed.getZone();
            _game.getGameState().sendMessage(_permanentPlayed.getOwnerName() + " plays " +
                    _permanentPlayed.getCardLink() +  " from " + playedFromZone.getHumanReadable() +
                    " to " + _toZone.getHumanReadable());
            _game.getGameState().removeCardsFromZone(_permanentPlayed.getOwnerName(),
                    Collections.singleton(_permanentPlayed));
            if (playedFromZone == Zone.HAND)
                _game.getGameState().addCardToZone(_permanentPlayed, Zone.VOID_FROM_HAND);
            else
                _game.getGameState().addCardToZone(_permanentPlayed, Zone.VOID);
            if (playedFromZone == Zone.DRAW_DECK) {
                _game.getGameState().sendMessage(_permanentPlayed.getOwnerName() + " shuffles their deck");
                _game.getGameState().shuffleDeck(_permanentPlayed.getOwnerName());
            }
        }

        if (!_cardPlayed) {
            _cardPlayed = true;
            _playCardEffect = new PlayCardEffect(_game, _fromZone, _permanentPlayed, _toZone);
            return _playCardEffect;
        }

        return getNextEffect();
    }

    public boolean wasCarriedOut() {
        return _cardPlayed && _playCardEffect != null && _playCardEffect.wasCarriedOut();
    }
}
