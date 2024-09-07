package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.actions.AbstractCostToEffectAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

public class UndockAction extends AbstractCostToEffectAction {
    private final PhysicalShipCard _cardToUndock;
    private boolean _cardUndocked = false;

    public UndockAction(Player player, PhysicalShipCard cardUndocking) {
        super(player, ActionType.MOVE_CARDS);
        _cardToUndock = cardUndocking;
        this.text = "Undock";
    }

    @Override
    public PhysicalCard getActionAttachedToCard() { return _cardToUndock; }
    @Override
    public PhysicalCard getActionSource() { return _cardToUndock; }

    @Override
    public Effect nextEffect() {
//        if (!isAnyCostFailed()) {

        Effect cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_cardUndocked) {
            _cardUndocked = true;
            _cardToUndock.undockFromFacility();
        }

        return getNextEffect();
    }

    public boolean wasActionCarriedOut() {
        return _cardUndocked;
    }

    @Override
    public boolean canBeInitiated() { return _cardToUndock.isDocked(); }

    @Override
    public ST1EGame getGame() { return _cardToUndock.getGame(); }

}
