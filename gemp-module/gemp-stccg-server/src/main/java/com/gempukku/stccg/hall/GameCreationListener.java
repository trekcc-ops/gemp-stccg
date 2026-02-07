package com.gempukku.stccg.hall;

import com.gempukku.stccg.game.CardGameMediator;

public interface GameCreationListener {
    void process(CardGameMediator mediator);
}