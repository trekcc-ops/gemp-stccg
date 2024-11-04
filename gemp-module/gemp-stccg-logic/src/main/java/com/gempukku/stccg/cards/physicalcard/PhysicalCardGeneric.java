package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.SnapshotData;

import java.util.List;
import java.util.Map;

public class PhysicalCardGeneric extends PhysicalCard {
    private final DefaultGame _game;

    public PhysicalCardGeneric(DefaultGame game, int cardId, String owner, CardBlueprint blueprint) {
        super(cardId, game.getGameState().getPlayer(owner), blueprint);
        _game = game;
    }

    public PhysicalCardGeneric(DefaultGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(cardId, owner, blueprint);
        _game = game;
    }

    @Override
    public DefaultGame getGame() { return _game; }

    @Override
    public CostToEffectAction getPlayCardAction(boolean forFree) {
        return new PlayCardAction(this) {
          @Override
          public DefaultGame getGame() { return _game; }
        };
    }

    @Override
    public PhysicalCard generateSnapshot(SnapshotData snapshotData) {

        // TODO - A lot of repetition here between the various PhysicalCard classes

        PhysicalCardGeneric newCard = new PhysicalCardGeneric(_game, _cardId, _owner, _blueprint);
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