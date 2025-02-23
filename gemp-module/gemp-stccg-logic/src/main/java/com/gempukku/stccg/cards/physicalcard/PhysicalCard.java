package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.modifiers.ExtraPlayCost;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="cardId")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "title", "blueprintId", "cardId", "owner", "locationId",
        "affiliation", "attachedToCardId", "stackedOnCardId", "isStopped", "dockedAtCardId", "rangeAvailable",
        "imageUrl", "cardType", "uniqueness", "hasUniversalIcon", "isInPlay" })
@JsonPropertyOrder({ "cardId", "title", "blueprintId", "owner", "locationId",
        "affiliation", "attachedToCardId", "stackedOnCardId", "isStopped", "dockedAtCardId", "rangeAvailable",
        "imageUrl", "cardType", "uniqueness", "hasUniversalIcon", "isInPlay" })
public interface PhysicalCard extends Filterable {

    @JsonIgnore
    DefaultGame getGame();
    @JsonIgnore
    Zone getZone();
    boolean isInHand(DefaultGame cardGame);
    void setZone(Zone zone);
    String getBlueprintId();
    @JsonProperty("imageUrl")
    String getImageUrl();
    int getCardId();
    Player getOwner();
    @JsonProperty("owner")
    String getOwnerName();

    void startAffectingGame(DefaultGame game);

    void stopAffectingGame(DefaultGame game);

    CardBlueprint getBlueprint();
    void attachTo(PhysicalCard physicalCard);
    void detach();
    @JsonIdentityReference(alwaysAsId=true)
    @JsonProperty("attachedToCardId")
    PhysicalCard getAttachedTo();
    void stackOn(PhysicalCard physicalCard);
    @JsonIdentityReference(alwaysAsId=true)
    @JsonProperty("stackedOnCardId")
    PhysicalCard getStackedOn();

    String getTitle();
    boolean canInsertIntoSpaceline();

    boolean canBeSeeded(DefaultGame game);

    boolean canBePlayed(DefaultGame game);

    boolean isControlledBy(String playerId);
    boolean isControlledBy(Player player);
    String getCardLink();
    GameLocation getGameLocation();
    void setLocation(GameLocation location);
    String getFullName();
    TopLevelSelectableAction getPlayCardAction();
    Action getPlayCardAction(boolean forFree);

    boolean hasTextRemoved(DefaultGame game);

    @JsonProperty("cardType")
    CardType getCardType();
    List<TopLevelSelectableAction> getRulesActionsWhileInPlay(Player player, DefaultGame cardGame);
    List<TopLevelSelectableAction> getGameTextActionsWhileInPlay(Player player);

    List<PhysicalCard> getStackedCards(DefaultGame game);

    Collection<PhysicalCard> getAttachedCards(DefaultGame game);

    List<? extends ExtraPlayCost> getExtraCostToPlay(DefaultGame game);

    List<TopLevelSelectableAction> getOptionalResponseWhileInPlayActions(ActionResult actionResult);
    TopLevelSelectableAction getDiscardedFromPlayTriggerAction(RequiredType requiredType);
    List<TopLevelSelectableAction> getOptionalAfterTriggerActions(Player player, ActionResult actionResult) throws PlayerNotFoundException;

    List<TopLevelSelectableAction> getRequiredResponseActions(ActionResult actionResult);

    boolean isUnique();

    Integer getNumberOfCopiesSeededByPlayer(Player player, DefaultGame cardGame);

    boolean isCopyOf(PhysicalCard card);

    List<TopLevelSelectableAction> createSeedCardActions();


    boolean hasIcon(DefaultGame game, CardIcon icon);
    boolean isPresentWith(PhysicalCard card);
    boolean hasSkill(SkillName skillName);

    boolean checkTurnLimit(DefaultGame game, int max);

    @JsonProperty("isInPlay")
    boolean isInPlay();
    boolean hasCharacteristic(Characteristic characteristic);

    boolean isMisSeed(DefaultGame cardGame, MissionLocation mission) throws CardNotFoundException;

    List<Action> getEncounterActions(DefaultGame game, AttemptMissionAction attemptAction,
                                     AttemptingUnit attemptingUnit,
                                     MissionLocation missionLocation)
            throws InvalidGameLogicException, PlayerNotFoundException;

    boolean isAtSpaceLocation();

    boolean isAtPlanetLocation();

    Player getController();

    int getCost();

    void setPlacedOnMission(boolean placedOnMission);

    boolean isPlacedOnMission();

    @JsonProperty("locationId")
    default Integer getLocationIdForSerialization() {
        GameLocation location = getGameLocation();
        if (location instanceof MissionLocation mission)
            return mission.getLocationId();
        else return null;
    }

    default MissionLocation getLocationDeprecatedOnlyUseForTests() throws InvalidGameLogicException {
        if (getGameLocation() instanceof MissionLocation mission)
            return mission;
        throw new InvalidGameLogicException("Tried to process card's location for a card not at any location");
    }

    default Uniqueness getUniqueness() {
        return getBlueprint().getUniqueness();
    }

    default boolean isUniversal() {
        return getBlueprint().isUniversal();
    }

    boolean isKnownToPlayer(String requestingPlayerId);

    boolean isVisibleToPlayer(String requestingPlayerId);

    void removeFromCardGroup();

    @JsonProperty("hasUniversalIcon")
    default boolean hasUniversalIcon() {
        return getBlueprint().hasUniversalIcon();
    }

    @JsonIgnore
    default Integer getStrength(DefaultGame cardGame) {
        return cardGame.getGameState().getModifiersQuerying().getStrength(this);
    }
}