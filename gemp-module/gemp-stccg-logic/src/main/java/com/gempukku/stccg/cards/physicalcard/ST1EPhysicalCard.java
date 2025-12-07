package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.actions.missionattempt.RevealSeedCardAction;
import com.gempukku.stccg.actions.playcard.ReportCardAction;
import com.gempukku.stccg.actions.playcard.STCCGPlayCardAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(value = { "cardType", "hasUniversalIcon", "imageUrl", "isInPlay", "title", "uniqueness" },
        allowGetters = true)
public class ST1EPhysicalCard extends AbstractPhysicalCard {

    @JsonProperty("isStopped")
    protected boolean _isStopped;

    @JsonCreator
    public ST1EPhysicalCard(
            @JsonProperty("cardId")
            int cardId,
            @JsonProperty("owner")
            String ownerName,
            @JsonProperty("blueprintId")
            String blueprintId,
            @JacksonInject
            CardBlueprintLibrary blueprintLibrary) throws CardNotFoundException {
        super(cardId, ownerName, blueprintLibrary.getCardBlueprint(blueprintId));
    }

    public ST1EPhysicalCard(int cardId, String ownerName, CardBlueprint blueprint) {
        super(cardId, ownerName, blueprint);
    }



    public TopLevelSelectableAction getPlayCardAction(DefaultGame cardGame, boolean forFree) {
        if (this instanceof ReportableCard reportable) {
            return new ReportCardAction(cardGame, reportable, forFree);
        } else {
            // TODO - Assuming default is play to table. Long-term this should pull from the blueprint.
            STCCGPlayCardAction action = new STCCGPlayCardAction(cardGame, this, Zone.CORE, _ownerName, forFree);
            cardGame.getGameState().getModifiersQuerying().appendExtraCosts(action, this);
            return action;
        }
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
                if (getCardType() == CardType.ARTIFACT) {
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

    @JsonIgnore
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

}