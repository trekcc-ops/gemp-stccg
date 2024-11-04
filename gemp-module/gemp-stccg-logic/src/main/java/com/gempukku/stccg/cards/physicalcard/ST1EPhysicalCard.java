package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.playcard.STCCGPlayCardAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.game.SnapshotData;

import java.util.List;
import java.util.Map;

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
        return action;
    }

    @Override
    public ST1EPhysicalCard generateSnapshot(SnapshotData snapshotData) {

        // TODO - A lot of repetition here between the various PhysicalCard classes

        ST1EPhysicalCard newCard = new ST1EPhysicalCard(_game, _cardId, _owner, _blueprint);
        newCard._imageUrl = _imageUrl;
        newCard.setZone(_zone);
        newCard.attachTo(snapshotData.getDataForSnapshot(_attachedTo));
        newCard.stackOn(snapshotData.getDataForSnapshot(_stackedOn));
        newCard._currentLocation = snapshotData.getDataForSnapshot(_currentLocation);
        newCard._whileInZoneData = _whileInZoneData;
        newCard._modifiers.putAll(_modifiers);
        newCard._modifierHooks = _modifierHooks;
        newCard._modifierHooksInZone.putAll(_modifierHooksInZone);

        for (PhysicalCard card : _cardsSeededUnderneath)
            newCard.addCardToSeededUnder(snapshotData.getDataForSnapshot(card));

        for (Map.Entry<Player, List<PhysicalCard>> entry : _cardsPreSeededUnderneath.entrySet())
            for (PhysicalCard card : entry.getValue())
                newCard.addCardToPreSeeds(snapshotData.getDataForSnapshot(card), entry.getKey());

        return newCard;
    }
}