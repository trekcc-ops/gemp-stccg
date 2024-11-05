package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.game.DefaultGame;

public class SeedCardAction extends PlayCardAction {

    public SeedCardAction(PhysicalCard cardToSeed) {
        this(cardToSeed, null);
    }

    public SeedCardAction(PhysicalCard cardToSeed, Zone zone) {
        super(cardToSeed, cardToSeed, cardToSeed.getOwnerName(), zone, ActionType.SEED_CARD);
        setText("Seed " + cardToSeed.getFullName());
    }

    @Override
    protected Effect getFinalEffect() {
        return new SeedCardEffect(_performingPlayerId, (ST1EPhysicalCard) _cardEnteringPlay, _toZone, this);
    }

    @Override
    public DefaultGame getGame() { return _actionSource.getGame(); }
}