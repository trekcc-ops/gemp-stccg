package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.effect.ModifierSource;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.Snapshotable;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.gempukku.stccg.modifiers.ExtraPlayCost;
import com.gempukku.stccg.modifiers.Modifier;

import java.util.Collection;
import java.util.List;

@JsonSerialize(using = PhysicalCardSerializer.class)
public interface PhysicalCard extends Filterable, Snapshotable<PhysicalCard> {
    DefaultGame getGame();
    Zone getZone();
    void setZone(Zone zone);
    String getBlueprintId();
    String getImageUrl();
    String getCardControllerPlayerIdForClient();
    int getCardId();
    Player getOwner();
    String getOwnerName();
    void startAffectingGame();
    void stopAffectingGame();
    CardBlueprint getBlueprint();
    void attachTo(PhysicalCard physicalCard);
    void detach();
    PhysicalCard getAttachedTo();
    void stackOn(PhysicalCard physicalCard);
    PhysicalCard getStackedOn();
    Object getWhileInZoneData();
    void setWhileInZoneData(Object object);
    String getTitle();
    boolean canInsertIntoSpaceline();
    int getLocationZoneIndex();
    boolean canEnterPlay(List<Requirement> requirements);
    boolean canBeSeeded();
    boolean canBePlayed();
    boolean isControlledBy(String playerId);
    boolean isControlledBy(Player player);
    String getCardLink();
    ST1ELocation getLocation();
    void setLocation(ST1ELocation location);
    String getFullName();
    CostToEffectAction getPlayCardAction();
    CostToEffectAction getPlayCardAction(boolean forFree);
    CostToEffectAction getPlayCardAction(Filterable additionalAttachmentFilter);
    List<Modifier> getModifiers(List<ModifierSource> sources);
    boolean hasTextRemoved();
    CardType getCardType();
    List<? extends Action> getPhaseActionsInPlay(Player player);
    void attachToCardAtLocation(PhysicalCard destinationCard);
    List<PhysicalCard> getStackedCards();
    Collection<PhysicalCard> getAttachedCards();
    List<? extends Action> getPhaseActionsInPlay(String playerId);
    List<? extends Action> getPhaseActionsFromZone(String playerId, Zone zone);
    List<? extends ExtraPlayCost> getExtraCostToPlay();
    List<Action> getOptionalInPlayActions(Effect effect, TriggerTiming timing);
    List<Action> getOptionalInPlayActions(EffectResult effectResult, TriggerTiming timing);
    Action getDiscardedFromPlayTriggerAction(RequiredType requiredType);
    List<Action> getOptionalAfterTriggerActions(String playerId, EffectResult effectResult);
    List<Action> getBeforeTriggerActions(Effect effect, RequiredType requiredType);
    List<Action> getBeforeTriggerActions(String playerId, Effect effect, RequiredType requiredType);
    List<Action> getRequiredResponseActions(EffectResult effectResult);
    ActionContext createActionContext();
    ActionContext createActionContext(String playerId, Effect effect, EffectResult effectResult);
    boolean isUnique();
    Integer getNumberOfCopiesSeededByPlayer(Player player);
    boolean isCopyOf(PhysicalCard card);
    Action createSeedCardAction();
    boolean hasIcon(CardIcon icon);
    boolean isPresentWith(PhysicalCard card);
    boolean hasSkill(SkillName skillName);
    boolean checkTurnLimit(int max);
    boolean isInPlay();
    boolean hasCharacteristic(Characteristic characteristic);
    void addCardToSeededUnder(PhysicalCard card);
    Collection<PhysicalCard> getCardsSeededUnderneath();
    Collection<PhysicalCard> getCardsPreSeeded(Player player);
    void removePreSeedCard(PhysicalCard card, Player player);
    void seedPreSeeds();
    void addCardToPreSeeds(PhysicalCard card, Player player);
    void setImageUrl(String imageUrl);
}