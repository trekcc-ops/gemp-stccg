package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.missionattempt.RevealSeedCardAction;
import com.gempukku.stccg.actions.playcard.STCCGPlayCardAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.List;

public class ST1EPhysicalCard extends AbstractPhysicalCard<ST1EGame> {
    protected final ST1EGame _game;
    protected boolean _isStopped;
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

    public TopLevelSelectableAction getPlayCardAction(boolean forFree) {
        // TODO - Assuming default is play to table. Long-term this should pull from the blueprint.
        STCCGPlayCardAction action = new STCCGPlayCardAction(this, Zone.TABLE, getOwner(), forFree);
        _game.getModifiersQuerying().appendExtraCosts(action, this);
        return action;
    }

    @Override
    public boolean isMisSeed(ST1EGame cardGame, MissionLocation mission) throws CardNotFoundException {
        if (_blueprint.getCardType() != CardType.DILEMMA && _blueprint.getCardType() != CardType.ARTIFACT)
            return true; // TODO - Sometimes gametext allows them to be seeded
        if (hasIcon(cardGame, CardIcon.AU_ICON))
            return true; // TODO - Need to consider cards that allow them
        if ((_blueprint.getMissionType() == MissionType.PLANET || _blueprint.getMissionType() == MissionType.SPACE) &&
                mission.getMissionType() != MissionType.DUAL && mission.getMissionType() != _blueprint.getMissionType())
            return true;
        List<Action> performedActions = cardGame.getActionsEnvironment().getPerformedActions();
        for (Action action : performedActions) {
            if (action instanceof RevealSeedCardAction revealAction) {
                if (_blueprint.getCardType() == CardType.ARTIFACT) {
                    // TODO - Artifact misseeding is a pain
                } else {
                    int olderCardId = revealAction.getRevealedCardId();
                    PhysicalCard olderCard = cardGame.getCardFromCardId(olderCardId);
                    if (this.isCopyOf(olderCard) && this != olderCard && _owner == olderCard.getOwner())
                        return true;
                }
            }
        }
        return false;
    }

    public void stop() {
        _isStopped = true;
        _game.sendSerializedGameStateToClient();
    }

    public void unstop() {
        _isStopped = false;
        _game.sendSerializedGameStateToClient();
    }

    public boolean isStopped() {
        return _isStopped;
    }

    @Override
    public List<Action> getEncounterActions(ST1EGame cardGame, AttemptingUnit attemptingUnit,
                                            EncounterSeedCardAction action, MissionLocation missionLocation)
            throws PlayerNotFoundException {
        return _blueprint.getEncounterActions(this, cardGame, attemptingUnit, action, missionLocation);
    }

}