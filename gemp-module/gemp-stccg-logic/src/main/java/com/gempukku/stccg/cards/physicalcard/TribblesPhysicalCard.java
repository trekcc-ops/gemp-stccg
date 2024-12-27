package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.playcard.TribblesPlayCardAction;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.SnapshotData;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.MissionLocation;

public class TribblesPhysicalCard extends AbstractPhysicalCard {
    private final TribblesGame _game;
    public TribblesPhysicalCard(TribblesGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(cardId, owner, blueprint);
        _game = game;
    }
    @Override
    public TribblesGame getGame() { return _game; }

    public boolean isMisSeed(DefaultGame game, MissionLocation mission) {
        return false;
    }

    @Override
    public TopLevelSelectableAction getPlayCardAction(boolean forFree) { return new TribblesPlayCardAction(this); }

    public boolean canPlayOutOfSequence() {
        if (_blueprint.getPlayOutOfSequenceConditions() == null) return false;
        return _blueprint.getPlayOutOfSequenceConditions().stream().anyMatch(
                requirement -> requirement.accepts(createActionContext(getGame())));
    }

    public boolean isNextInSequence() {
        final int cardValue = _blueprint.getTribbleValue();
        if (_game.getGameState().isChainBroken() && (cardValue == 1)) {
            return true;
        }
        return (cardValue == _game.getGameState().getNextTribbleInSequence());
    }

    @Override
    public TribblesPhysicalCard generateSnapshot(SnapshotData snapshotData) {

        // TODO - A lot of repetition here between the various PhysicalCard classes

        TribblesPhysicalCard newCard = new TribblesPhysicalCard(_game, _cardId, snapshotData.getDataForSnapshot(_owner), _blueprint);
        newCard.setZone(_zone);
        newCard.attachTo(snapshotData.getDataForSnapshot(_attachedTo));
        newCard.stackOn(snapshotData.getDataForSnapshot(_stackedOn));
        newCard._currentLocation = snapshotData.getDataForSnapshot(_currentLocation);

        return newCard;
    }

}