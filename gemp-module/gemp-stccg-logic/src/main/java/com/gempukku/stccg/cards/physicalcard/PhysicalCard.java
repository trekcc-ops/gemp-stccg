package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.cardgroup.PhysicalCardGroup;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ChildCardRelationshipType;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="cardId")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "title", "blueprintId", "cardId", "owner", "locationId",
        "affiliation", "attachedToCardId", "stackedOnCardId", "isStopped", "dockedAtCardId", "rangeAvailable",
        "imageUrl", "cardType", "uniqueness", "hasUniversalIcon", "isInPlay", "isPlacedOnMission",
        "childrenCards", "parentCard", "relationToParent"
})
@JsonPropertyOrder({ "cardId", "title", "blueprintId", "owner", "locationId",
        "affiliation", "attachedToCardId", "stackedOnCardId", "isStopped", "dockedAtCardId", "rangeAvailable",
        "imageUrl", "cardType", "uniqueness", "hasUniversalIcon", "isInPlay", "isPlacedOnMission",
        "childrenCards", "parentCard", "relationToParent"
})
@JsonIgnoreProperties(value = { "title" }, allowGetters = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public interface PhysicalCard {

    @JsonIgnore
    Zone getZone();
    boolean isInHand(DefaultGame cardGame);
    void setZone(Zone zone);
    String getBlueprintId();
    @JsonProperty("imageUrl")
    String getImageUrl();

    @JsonProperty("cardId")
    int getCardId();

    @JsonProperty("owner")
    String getOwnerName();

    CardBlueprint getBlueprint();

    @JsonIgnore
    PhysicalCard getAttachedTo(DefaultGame cardGame);
    @JsonProperty("attachedToCardId")
    Integer getAttachedToCardId();

    void stackOn(PhysicalCard physicalCard);
    @JsonIdentityReference(alwaysAsId=true)
    @JsonProperty("stackedOnCardId")
    PhysicalCard getStackedOn(DefaultGame cardGame);

    String getTitle();
    boolean canInsertIntoSpaceline();

    boolean isControlledBy(String playerId);
    boolean isControlledBy(Player player);
    String getCardLink();
    GameLocation getGameLocation(ST1EGameState gameSate);

    GameLocation getGameLocation(ST1EGame cardGame);
    void setLocationId(int locationId);
    void setLocationId(DefaultGame cardGame, int locationId);
    void setLocation(DefaultGame cardGame, GameLocation location);

    String getFullName();

    TopLevelSelectableAction getPlayCardAction(DefaultGame cardGame);
    Action getPlayCardAction(DefaultGame cardGame, boolean forFree);

    boolean hasTextRemoved(DefaultGame game);

    @JsonProperty("cardType")
    CardType getCardType();
    List<TopLevelSelectableAction> getRulesActionsWhileInPlay(Player player, DefaultGame cardGame);

    List<PhysicalCard> getStackedCards(DefaultGame game);

    Collection<PhysicalCard> getAttachedCards(DefaultGame game);

    List<TopLevelSelectableAction> getRequiredResponseActions(DefaultGame cardGame, ActionResult actionResult);

    boolean isUnique();

    boolean isCopyOf(PhysicalCard card);
    List<TopLevelSelectableAction> createSeedCardActions(DefaultGame cardGame);


    boolean hasIcon(DefaultGame game, CardIcon icon);

    boolean hasSkill(SkillName skillName, DefaultGame cardGame);

    @JsonProperty("isInPlay")
    boolean isInPlay();
    boolean hasCharacteristic(Characteristic characteristic);

    boolean isMisSeed(DefaultGame cardGame, MissionLocation mission) throws CardNotFoundException;

    List<Action> getEncounterActions(DefaultGame game, AttemptMissionAction attemptAction,
                                     AttemptingUnit attemptingUnit,
                                     MissionLocation missionLocation)
            throws InvalidGameLogicException, PlayerNotFoundException;

    boolean isAtSpaceLocation(ST1EGame cardGame);

    boolean isAtPlanetLocation(ST1EGame cardGame);

    String getControllerName();

    int getCost();

    @JsonProperty("isPlacedOnMission")
    boolean isPlacedOnMission();

    default MissionLocation getLocationDeprecatedOnlyUseForTests(ST1EGame stGame) throws InvalidGameLogicException {
        if (getGameLocation(stGame) instanceof MissionLocation mission)
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

    default void removeFromCardGroup(DefaultGame cardGame) {
        PhysicalCardGroup<? extends PhysicalCard> group =
                cardGame.getGameState().getCardGroup(getOwnerName(), getZone());
        if (group != null)
            group.remove(this);
    }


    @JsonProperty("hasUniversalIcon")
    default boolean hasUniversalIcon() {
        return getBlueprint().hasUniversalIcon();
    }

    void reveal();

    default List<TopLevelSelectableAction> getOptionalResponseActionsWhileInHand(DefaultGame cardGame, Player player,
                                                                                 ActionResult actionResult) {
        return getBlueprint().getOptionalResponseActionsWhileInHand(cardGame, this, player, actionResult);
    }


    default List<TopLevelSelectableAction> getPlayActionsFromGameText(Player player, DefaultGame cardGame) {
        return getBlueprint().getPlayActionsFromGameText(this, player, cardGame);
    }

    boolean isOwnedBy(String playerName);

    boolean isActive();

    @JsonProperty("locationId")
    int getLocationId();

    boolean isAtSameLocationAsCard(PhysicalCard card);

    @JsonProperty("cardId")
    void setCardId(int cardId);

    Collection<TopLevelSelectableAction> getOptionalResponseActionsWhileInPlay(DefaultGame game, Player player);

    boolean isBeingEncounteredBy(String playerName, DefaultGame cardGame);
    boolean isBeingEncountered(DefaultGame cardGame);

    @JsonIgnore
    boolean isOnPlanet(DefaultGame cardGame);

    @JsonIgnore
    default int getPointBoxValue() {
        return getBlueprint().getPointsShown();
    }

    @JsonIgnore
    default boolean hasPropertyLogo(PropertyLogo propertyLogo) {
        return getBlueprint().getPropertyLogo() == propertyLogo;
    }

    default boolean isPersonaVersionOf(PhysicalCard otherCard) {
        String thisCardPersona = getBlueprint().getPersona();
        return (thisCardPersona != null && Objects.equals(thisCardPersona, otherCard.getBlueprint().getPersona()));
    }

    boolean isBeingEncounteredBy(DefaultGame cardGame, PhysicalCard encounteringCard);

    default MissionRequirement getNullifyRequirement() {
        return getBlueprint().getNullifyRequirement();
    }

    default List<Modifier> getAlwaysOnModifiers(DefaultGame cardGame) {
        return getBlueprint().getAlwaysOnModifiers(cardGame,this);
    }

    void clearParentCardRelationship();
    void clearChildRelationship(PhysicalCard childCard);

    void addChildCardRelationship(PhysicalCard childCard, ChildCardRelationshipType childCardRelationshipType);

    PhysicalCard getParentCard();
    boolean isAboard(PhysicalCard card);
    void setParentCardRelationship(PhysicalCard parentCard, ChildCardRelationshipType relationshipType);

    PhysicalCard getAtopCard();

    void setAsAtop(PhysicalCard destination);

    boolean isInDiscard(DefaultGame game);

    default List<PhysicalCard> getDestinationOptionsFromGameText(GameTextContext context, DefaultGame cardGame) {
        return new ArrayList<>(getBlueprint().getPlayCardDestinationOptionsFromGameText(context, cardGame));
    }

    boolean isInDrawDeck(DefaultGame game);
}