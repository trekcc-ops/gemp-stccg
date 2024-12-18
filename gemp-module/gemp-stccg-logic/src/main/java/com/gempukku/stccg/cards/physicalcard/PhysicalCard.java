package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.Snapshotable;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.modifiers.ExtraPlayCost;

import java.util.Collection;
import java.util.List;

@JsonSerialize(using = PhysicalCardSerializer.class)
public interface PhysicalCard extends Filterable, Snapshotable<PhysicalCard> {
    DefaultGame getGame();
    Zone getZone();
    void setZone(Zone zone);
    String getBlueprintId();
    String getImageUrl();
    int getCardId();
    Player getOwner();
    String getOwnerName();

    void startAffectingGame(DefaultGame game);

    void stopAffectingGame(DefaultGame game);

    CardBlueprint getBlueprint();
    void attachTo(PhysicalCard physicalCard);
    void detach();
    PhysicalCard getAttachedTo();
    void stackOn(PhysicalCard physicalCard);
    PhysicalCard getStackedOn();

    String getTitle();
    boolean canInsertIntoSpaceline();
    int getLocationZoneIndex();

    boolean canBeSeeded(DefaultGame game);

    boolean canBePlayed(DefaultGame game);

    boolean isControlledBy(String playerId);
    boolean isControlledBy(Player player);
    String getCardLink();
    MissionLocation getLocation() throws InvalidGameLogicException;
    void setLocation(MissionLocation location);
    String getFullName();
    Action getPlayCardAction();
    Action getPlayCardAction(boolean forFree);

    boolean hasTextRemoved(DefaultGame game);
    CardType getCardType();
    List<? extends Action> getRulesActionsWhileInPlay(Player player);
    List<? extends Action> getGameTextActionsWhileInPlay(Player player);

    List<PhysicalCard> getStackedCards(DefaultGame game);

    Collection<PhysicalCard> getAttachedCards(DefaultGame game);

    List<? extends ExtraPlayCost> getExtraCostToPlay(DefaultGame _game);
    List<Action> getOptionalInPlayActions(Effect effect, TriggerTiming timing);
    List<Action> getOptionalInPlayActions(EffectResult effectResult, TriggerTiming timing);
    Action getDiscardedFromPlayTriggerAction(RequiredType requiredType);
    List<Action> getOptionalAfterTriggerActions(Player player, EffectResult effectResult);
    List<Action> getBeforeTriggerActions(Effect effect, RequiredType requiredType);
    List<Action> getBeforeTriggerActions(String playerId, Effect effect, RequiredType requiredType);
    List<Action> getRequiredResponseActions(EffectResult effectResult);

    boolean isUnique();
    Integer getNumberOfCopiesSeededByPlayer(Player player);
    boolean isCopyOf(PhysicalCard card);
    Action createSeedCardAction();

    boolean hasIcon(DefaultGame game, CardIcon icon);
    boolean isPresentWith(PhysicalCard card);
    boolean hasSkill(SkillName skillName);

    boolean checkTurnLimit(DefaultGame game, int max);
    boolean isInPlay();
    boolean hasCharacteristic(Characteristic characteristic);

    boolean isMisSeed(DefaultGame cardGame, MissionLocation mission);

    List<Action> getEncounterActions(DefaultGame game, AttemptingUnit attemptingUnit, EncounterSeedCardAction action, MissionLocation missionLocation) throws InvalidGameLogicException;

    boolean isAtSpaceLocation();

    boolean isAtPlanetLocation();
}