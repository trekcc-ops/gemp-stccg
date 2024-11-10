package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.actions.AbstractCostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.choose.ChooseCardsOnTableEffect;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.google.common.collect.Iterables;

import java.util.Collection;

public class DockAction extends AbstractCostToEffectAction {
    private final PhysicalShipCard _cardToDock;
    private boolean _targetChosen;
    private boolean _cardDocked;
    private FacilityCard _dockingTarget;
    private final Collection<FacilityCard> _dockingTargetOptions;

    public DockAction(Player player, PhysicalShipCard cardToDock) {
        super(player, ActionType.MOVE_CARDS);
        _cardToDock = cardToDock;
        this.text = "Dock";

        _dockingTargetOptions = Filters.yourActiveFacilities(player).stream()
                .filter(card -> card.isCompatibleWith(_cardToDock) && card.getLocation() == _cardToDock.getLocation())
                .toList();
    }

    private Effect chooseDockingTargetEffect() {
        DefaultGame game = _cardToDock.getGame();
        Player performingPlayer = game.getPlayer(_performingPlayerId);
        return new ChooseCardsOnTableEffect(this, performingPlayer,
                "Choose facility to dock at", _dockingTargetOptions) {
            @Override
            protected void cardsSelected(Collection<PhysicalCard> cards) {
                _targetChosen = true;
                _dockingTarget = (FacilityCard) Iterables.getOnlyElement(cards);
            }
        };

    }
    @Override
    public PhysicalCard getCardForActionSelection() { return _cardToDock; }
    @Override
    public PhysicalCard getActionSource() { return _cardToDock; }

    @Override
    public Effect nextEffect(DefaultGame cardGame) {
//        if (!isAnyCostFailed()) {

        Effect cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_targetChosen) {
            appendTargeting(chooseDockingTargetEffect());
            return getNextCost();
        }

        if (!_cardDocked) {
            _cardDocked = true;
            _cardToDock.dockAtFacility(_dockingTarget);
            _cardToDock.getGame().getGameState().transferCard(_cardToDock, _dockingTarget);
        }

        return getNextEffect();
    }

    @Override
    public boolean canBeInitiated(DefaultGame cardGame) { return !_cardToDock.isDocked() && !_dockingTargetOptions.isEmpty(); }

}