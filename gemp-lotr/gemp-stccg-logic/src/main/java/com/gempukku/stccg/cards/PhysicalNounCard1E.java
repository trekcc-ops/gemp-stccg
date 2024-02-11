package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.google.common.collect.Iterables;

import java.util.Set;

public class PhysicalNounCard1E extends PhysicalCard {
    protected Set<Affiliation> _affiliationOptions;
    protected Affiliation _currentAffiliation;
    protected Quadrant _nativeQuadrant;
    protected ST1ELocation _currentLocation;
    protected final ST1EGame _game;
    public PhysicalNounCard1E(ST1EGame game, int cardId, String blueprintId, String owner, CardBlueprint blueprint) {
        super(cardId, blueprintId, owner, blueprint);
        _affiliationOptions = blueprint.getAffiliations();
        if (_affiliationOptions.size() == 1)
            _currentAffiliation = Iterables.getOnlyElement(_affiliationOptions);
        _nativeQuadrant = blueprint.getQuadrant();
        _game = game;
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
    public ST1ELocation getCurrentLocation() {
        return _currentLocation;
    }
}