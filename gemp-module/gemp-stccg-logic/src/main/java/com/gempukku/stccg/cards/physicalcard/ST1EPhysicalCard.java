package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.playcard.STCCGPlayCardAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

import java.util.List;

public class ST1EPhysicalCard extends PhysicalCard {
    protected final ST1EGame _game;
    public ST1EPhysicalCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(cardId, owner, blueprint);
        _game = game;
    }

    // For testing
    public ST1EPhysicalCard(ST1EGame game, Player owner, String title, String set) throws CardNotFoundException {
        super(game.getGameState().getAndIncrementNextCardId(), owner,
                game.getBlueprintLibrary().getBlueprintByName(title, set));
        _game = game;
    }
    @Override
    public ST1EGame getGame() { return _game; }

    public List<CardIcon> getIcons() { return _blueprint.getIcons(); }

    public CostToEffectAction getPlayCardAction(boolean forFree) {
        // TODO - Assuming default is play to table. Long-term this should pull from the blueprint.
        STCCGPlayCardAction action = new STCCGPlayCardAction(this, Zone.TABLE, getOwner(), forFree);
        getGame().getModifiersQuerying().appendExtraCosts(action, this);
        getGame().getModifiersQuerying().appendPotentialDiscounts(action, this);
        return action;
    }
}