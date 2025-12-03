package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.actions.missionattempt.RevealSeedCardAction;
import com.gempukku.stccg.actions.playcard.STCCGPlayCardAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.List;
import java.util.Objects;

public class ST1EPhysicalCard extends AbstractPhysicalCard {
    protected final ST1EGame _game;
    protected boolean _isStopped;
    public ST1EPhysicalCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(cardId, owner, blueprint);
        _game = game;
    }

    public List<CardIcon> getIcons() { return _blueprint.getIcons(); }

    public TopLevelSelectableAction getPlayCardAction(boolean forFree) {
        // TODO - Assuming default is play to table. Long-term this should pull from the blueprint.
        STCCGPlayCardAction action = new STCCGPlayCardAction(_game, this, Zone.CORE, _ownerName, forFree);
        _game.getGameState().getModifiersQuerying().appendExtraCosts(action, this);
        return action;
    }

    @Override
    public boolean isMisSeed(DefaultGame cardGame, MissionLocation mission) throws CardNotFoundException {
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
                    if (this.isCopyOf(olderCard) && this != olderCard && Objects.equals(_ownerName, olderCard.getOwnerName()))
                        return true;
                }
            }
        }
        return false;
    }

    public void stop() {
        _isStopped = true;
    }

    public void unstop() {
        _isStopped = false;
    }

    public boolean isStopped() {
        return _isStopped;
    }

    @Override
    public List<Action> getEncounterActions(DefaultGame cardGame, AttemptMissionAction attemptAction,
                                            AttemptingUnit attemptingUnit, MissionLocation missionLocation)
            throws InvalidGameLogicException, PlayerNotFoundException {
        return _blueprint.getEncounterSeedCardActions(this, attemptAction, cardGame, attemptingUnit,
                missionLocation);
    }

    @Override
    public boolean isActive() {
        // TODO - account for other inactive states
        return !_isStopped;
    }

    @Override
    public boolean isPresentWith(PhysicalCard card) {
        return card.getGameLocation() == this.getGameLocation() &&
                card.getGameLocation() instanceof MissionLocation missionLocation &&
                _attachedToCardId != null &&
                _attachedToCardId == card.getAttachedToCardId() &&
                _game.getGameState().getSpacelineLocations().contains(missionLocation);
    }


}