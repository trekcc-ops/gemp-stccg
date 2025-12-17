package com.gempukku.stccg.game;

public class InappropriateGameTypeException extends InvalidGameOperationException {

    /* Exception to be thrown when the wrong type of game is passed as an argument to a method.
            For example, if a TribblesGame is passed to an action exclusive to 1E games like "change affiliation".
     */
    public InappropriateGameTypeException(String message) {
        super(message);
    }
}