package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

public interface GameLocation {

    boolean mayBeAttemptedByPlayer(Player player, ST1EGame cardGame) throws InvalidGameLogicException;

    boolean isPlanet();

    boolean isSpace();

    boolean isInQuadrant(Quadrant quadrant);
    
    int getDistanceToLocation(ST1EGame cardGame, GameLocation location, Player calculatingPlayer) 
            throws InvalidGameLogicException;
    
    String getLocationName() throws InvalidGameLogicException;

    boolean isHomeworld();
}