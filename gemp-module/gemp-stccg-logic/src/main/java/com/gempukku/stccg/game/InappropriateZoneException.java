package com.gempukku.stccg.game;

public class InappropriateZoneException extends InvalidGameOperationException {

    /* Exception to be thrown when a method has a zone argument that belongs to a different type of game or action
           (for example, if a method for a 1E game receives Zone.PLAY_PILE as an argument)
     */
    public InappropriateZoneException(String message) {
        super(message);
    }
}