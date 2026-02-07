package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.game.ST1EGame;

public class NullLocation implements GameLocation {

    // GameLocation class specifically designed for cards not at a location, to avoid null pointer exceptions

    @Override
    public boolean mayBeAttemptedByPlayer(Player player, ST1EGame cardGame) {
        return false;
    }

    @Override
    public boolean isPlanet() { return false; }

    @Override
    public boolean isSpace() { return false; }

    @Override
    public boolean isInQuadrant(Quadrant quadrant) { return false; }

    @Override
    public int getDistanceToLocation(ST1EGame cardGame, GameLocation location, Player calculatingPlayer)
            throws InvalidGameLogicException{
        throw new InvalidGameLogicException("Cannot travel to or from a null location");
    }

    @Override
    public String getLocationName() throws InvalidGameLogicException {
        throw new InvalidGameLogicException("Null location has no name");
    }

    public boolean isHomeworld() { return false; }

    @Override
    public boolean hasCardSeededUnderneath(PhysicalCard card) {
        return false;
    }

    @Override
    public int getLocationId() {
        return -999;
    }
}