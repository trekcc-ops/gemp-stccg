package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.google.common.collect.Iterables;

import java.util.Set;

public class PhysicalOutpostCard extends PhysicalCard {
    private Set<Affiliation> _affiliationOptions;
    private Affiliation _currentAffiliation;
    private Quadrant _nativeQuadrant;
    private final ST1EGame _game;
    public PhysicalOutpostCard(ST1EGame game, int cardId, String blueprintId, String owner, CardBlueprint blueprint) {
        super(cardId, blueprintId, owner, blueprint);
        _affiliationOptions = blueprint.getAffiliations();
        if (_affiliationOptions.size() == 1)
            _currentAffiliation = Iterables.getOnlyElement(_affiliationOptions);
        _nativeQuadrant = blueprint.getQuadrant();
        _game = game;
    }
    public boolean canSeedAtMission(PhysicalMissionCard mission) {
        for (Affiliation affiliation : _affiliationOptions)
            if (canSeedAtMissionAsAffiliation(mission, affiliation))
                return true;
        return false;
    }
    public boolean canSeedAtMissionAsAffiliation(PhysicalMissionCard mission, Affiliation affiliation) {
        if (mission.isHomeworld())
            return false;
        if (mission.getLocation().hasFacilityOwnedByPlayer(_owner))
            return false;
        return mission.getAffiliationIcons(_owner).contains(affiliation) && mission.getQuadrant() == _nativeQuadrant;
    }
    public boolean isMultiAffiliation() { return _affiliationOptions.size() > 1; }
    public Affiliation getCurrentAffiliation() { return _currentAffiliation; }
    public void setCurrentAffiliation(Affiliation affiliation) { _currentAffiliation = affiliation; }
    public void changeNativeQuadrant(Quadrant quadrant) { _nativeQuadrant = quadrant; }
    public Set<Affiliation> getAffiliationOptions() { return _affiliationOptions; }
    @Override
    public boolean canBeSeeded() {
        for (ST1ELocation location : _game.getGameState().getSpacelineLocations()) {
            for (PhysicalMissionCard mission : location.getMissions())
                if (this.canSeedAtMission(mission))
                    return true;
        }
        return false;
    }
}