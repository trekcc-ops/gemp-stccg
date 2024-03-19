package com.gempukku.stccg.actions;

import java.util.LinkedList;

public class LegalActionList extends LinkedList<Action> {

    public void addIfLegal(Action action) {
        if (action.canBeInitiated())
            add(action);
    }

}
