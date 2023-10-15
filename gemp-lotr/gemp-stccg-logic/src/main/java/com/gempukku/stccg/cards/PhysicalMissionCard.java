package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.gamestate.ST1ELocation;

import java.util.Objects;
import java.util.Set;

public class PhysicalMissionCard extends PhysicalCard {
    private Quadrant _quadrant;
    private ST1ELocation _location = null;
    public PhysicalMissionCard(int cardId, String blueprintId, String owner, CardBlueprint blueprint) {
        super(cardId, blueprintId, owner, blueprint);
        _quadrant = blueprint.getQuadrant();
    }
    public Set<Affiliation> getAffiliationIcons(String playerId) {
        if (Objects.equals(playerId, _owner)) {
            return _blueprint.getOwnerAffiliationIcons();
        } else if (_blueprint.getOpponentAffiliationIcons() == null) {
            return _blueprint.getOwnerAffiliationIcons();
        } else {
            // TODO: Assumes all missions are symmetric
            return _blueprint.getOwnerAffiliationIcons();
        }
    }
    public Quadrant getQuadrant() { return _quadrant; }
    public boolean isHomeworld() { return _blueprint.isHomeworld(); }
    @Override
    public boolean canBeSeeded() { return true; }
    public void setLocation(ST1ELocation location) { _location = location; }
    public ST1ELocation getLocation() { return _location; }
}