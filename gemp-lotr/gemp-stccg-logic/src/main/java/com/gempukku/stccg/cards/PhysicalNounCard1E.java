package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.google.common.collect.Iterables;

import java.util.Set;

public class PhysicalNounCard1E extends ST1EPhysicalCard {
    protected Set<Affiliation> _affiliationOptions;
    protected Affiliation _currentAffiliation; // TODO - NounCard class may include Equipment or other cards with no affiliation
    protected Quadrant _nativeQuadrant;
    public PhysicalNounCard1E(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
        _affiliationOptions = blueprint.getAffiliations();
        if (_affiliationOptions.size() == 1)
            _currentAffiliation = Iterables.getOnlyElement(_affiliationOptions);
        _nativeQuadrant = blueprint.getQuadrant();
    }

    public boolean isMultiAffiliation() { return _affiliationOptions.size() > 1; }
    public Affiliation getCurrentAffiliation() { return _currentAffiliation; }
    public void setCurrentAffiliation(Affiliation affiliation) { _currentAffiliation = affiliation; }

    public Set<Affiliation> getAffiliationOptions() { return _affiliationOptions; }
    public boolean isCompatibleWith(Affiliation affiliation) {
        if (getCurrentAffiliation() == affiliation)
            return true;
        if (getCurrentAffiliation() == Affiliation.BORG || affiliation == Affiliation.BORG)
            return false;
        return getCurrentAffiliation() == Affiliation.NON_ALIGNED || affiliation == Affiliation.NON_ALIGNED;
    }
    public Quadrant getCurrentQuadrant() {
        return _currentLocation.getQuadrant();
    }

    public void setCurrentLocation(ST1ELocation location) {
        _currentLocation = location;
        _locationZoneIndex = _game.getGameState().getSpacelineLocations().indexOf(location);
    }

    @Override
    public ST1ELocation getLocation() {
        return _currentLocation;
    }

    @Override
    public boolean hasTransporters() {
        if (_blueprint.getCardType() == CardType.SHIP || _blueprint.getCardType() == CardType.FACILITY) {
            return !_blueprint.hasNoTransporters();
        } else return false;
    }
}