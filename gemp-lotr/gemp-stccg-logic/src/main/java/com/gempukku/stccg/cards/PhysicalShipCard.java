package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.BeamCardsAction;
import com.gempukku.stccg.actions.WalkCardsAction;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

import java.util.LinkedList;
import java.util.List;

public class PhysicalShipCard extends PhysicalReportableCard1E {

    private boolean _docked = false;
    private PhysicalFacilityCard _dockedAtCard = null;

    public PhysicalShipCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
    }

    @Override
    public List<? extends Action> getPhaseActionsInPlay(Player player) {
        List<Action> actions = new LinkedList<>();
        if (_game.getGameState().getCurrentPhase() == Phase.EXECUTE_ORDERS) {
            if (isControlledBy(player.getPlayerId())) {
                if (hasTransporters())
                    actions.add(new BeamCardsAction(player, this));
                if (isDocked())
                    actions.add(new WalkCardsAction(player, this));
                            // Need to have staffing
/*                if (isDocked())
                    actions.add(new UndockAction(player, this));
                if (!isDocked())
                    actions.add(new DockAction(player, this)); */
            }
        }
        actions.removeIf(action -> !action.canBeInitiated());
        return actions;
    }

    public boolean isDocked() { return _docked; }

    @Override
    public void reportToFacility(PhysicalFacilityCard facility) {
        setCurrentLocation(facility.getLocation()); // TODO - What happens if the facility doesn't allow docking?
        _game.getGameState().attachCard(this, facility);
    }

    public void dockAtFacility(PhysicalFacilityCard facilityCard) {
        _game.getGameState().transferCard(this, facilityCard);
        _docked = true;
        _dockedAtCard = facilityCard;
    }

    public void undockFromFacility() {
        _docked = false;
        _dockedAtCard = null;
        _game.getGameState().detachCard(this, Zone.AT_LOCATION);
    }

    public PhysicalCard getDockedAtCard() {
        return _dockedAtCard;
    }

}