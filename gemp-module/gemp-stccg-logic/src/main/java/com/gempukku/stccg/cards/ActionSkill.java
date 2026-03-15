package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public abstract class ActionSkill extends Skill {
    public ActionSkill(String text) {
        super();
    }

    public abstract Action getAction(DefaultGame cardGame, PhysicalCard thisCard);
}