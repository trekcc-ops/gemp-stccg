package com.gempukku.stccg.actions.playcard;

public class PlayCardState {
    private int _id;
    private PlayCardAction _playCardAction;

    /**
     * Creates state information for a play card action.
     * @param id the unique id
     * @param playCardAction the play card action
     */
    public PlayCardState(int id, PlayCardAction playCardAction) {
        _id = id;
        _playCardAction = playCardAction;
    }

    /**
     * Gets the unique id.
     * @return the unique id
     */
    public Integer getId() {
        return _id;
    }

    /**
     * Gets the play card action.
     * @return the play card action
     */
    public PlayCardAction getPlayCardAction() {
        return _playCardAction;
    }
}
