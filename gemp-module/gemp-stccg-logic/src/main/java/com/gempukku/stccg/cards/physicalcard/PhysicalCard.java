package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.JsonViews;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.modifiers.ExtraPlayCost;

import java.util.Collection;
import java.util.List;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="cardId")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "title", "blueprintId", "cardId", "owner", "zone", "locationZoneIndex",
        "affiliation", "attachedToCardId", "stackedOnCardId", "isStopped", "dockedAtCardId", "rangeAvailable" })
@JsonPropertyOrder({ "cardId", "title", "blueprintId", "owner", "zone", "locationZoneIndex",
        "affiliation", "attachedToCardId", "stackedOnCardId", "isStopped", "dockedAtCardId", "rangeAvailable" })
public interface PhysicalCard<GenericGame extends DefaultGame> extends Filterable {
    GenericGame getGame();
    Zone getZone();
    void setZone(Zone zone);
    String getBlueprintId();
    String getImageUrl();
    int getCardId();
    Player getOwner();
    @JsonProperty("owner")
    String getOwnerName();

    void startAffectingGame(GenericGame game);

    void stopAffectingGame(GenericGame game);

    CardBlueprint getBlueprint();
    void attachTo(PhysicalCard<? extends GenericGame> physicalCard);
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

    int getLocationZoneIndex(GenericGame game);

    boolean canBeSeeded(GenericGame game);

    boolean canBePlayed(GenericGame game);

    boolean isControlledBy(String playerId);
    boolean isControlledBy(Player player);
    String getCardLink();
    MissionLocation getLocation() throws InvalidGameLogicException;
    void setLocation(MissionLocation location);
    String getFullName();
    TopLevelSelectableAction getPlayCardAction();
    Action getPlayCardAction(boolean forFree);

    boolean hasTextRemoved(GenericGame game);
    CardType getCardType();
    List<TopLevelSelectableAction> getRulesActionsWhileInPlay(Player player, GenericGame cardGame);
    List<TopLevelSelectableAction> getGameTextActionsWhileInPlay(Player player);

    List<PhysicalCard> getStackedCards(GenericGame game);

    Collection<PhysicalCard> getAttachedCards(GenericGame game);

    List<? extends ExtraPlayCost> getExtraCostToPlay(GenericGame game);

    List<TopLevelSelectableAction> getOptionalInPlayActions(ActionResult actionResult, TriggerTiming timing);
    TopLevelSelectableAction getDiscardedFromPlayTriggerAction(RequiredType requiredType);
    List<TopLevelSelectableAction> getOptionalAfterTriggerActions(Player player, ActionResult actionResult) throws PlayerNotFoundException;

    List<TopLevelSelectableAction> getRequiredResponseActions(ActionResult actionResult);

    boolean isUnique();

    Integer getNumberOfCopiesSeededByPlayer(Player player, GenericGame cardGame);

    boolean isCopyOf(PhysicalCard card);
    TopLevelSelectableAction createSeedCardAction();

    boolean hasIcon(GenericGame game, CardIcon icon);
    boolean isPresentWith(PhysicalCard card);
    boolean hasSkill(SkillName skillName);

    boolean checkTurnLimit(GenericGame game, int max);
    boolean isInPlay();
    boolean hasCharacteristic(Characteristic characteristic);

    boolean isMisSeed(GenericGame cardGame, MissionLocation mission) throws CardNotFoundException;

    List<Action> getEncounterActions(GenericGame game, AttemptingUnit attemptingUnit, EncounterSeedCardAction action, MissionLocation missionLocation) throws InvalidGameLogicException, PlayerNotFoundException;

    boolean isAtSpaceLocation();

    boolean isAtPlanetLocation();

    Player getController();

    int getCost();

    void setPlacedOnMission(boolean placedOnMission);

    boolean isPlacedOnMission();
}