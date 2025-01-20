package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.blueprints.Blueprint109_063;
import com.gempukku.stccg.cards.blueprints.Blueprint156_010;
import com.gempukku.stccg.cards.blueprints.Blueprint212_019;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.actionsource.ActionSource;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.modifiers.ExtraPlayCost;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ModifierEffect;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractPhysicalCard implements PhysicalCard {

    protected final CardBlueprint _blueprint;
    protected final Player _owner;
    protected final int _cardId;
    protected Zone _zone;
    private Integer _attachedToCardId;
    private Integer _stackedOnCardId;
    protected MissionLocation _currentLocation;
    private boolean _placedOnMission = false;

    public AbstractPhysicalCard(int cardId, Player owner, CardBlueprint blueprint) {
        _cardId = cardId;
        _owner = owner;
        _blueprint = blueprint;
    }

    public Zone getZone() { return _zone; }

    public void setZone(Zone zone) { _zone = zone; }

    public String getBlueprintId() { return _blueprint.getBlueprintId(); }
    public String getImageUrl() {
        // TODO - Replace with a client communication method that pulls image options from the library
        String result;
        if (this instanceof AffiliatedCard affiliatedCard) {
            String affiliatedImage = _blueprint.getAffiliationImageUrl(affiliatedCard.getAffiliation());
            if (affiliatedImage != null)
                result = affiliatedImage;
            else
                result = _blueprint.getImageUrl();
        } else {
            result = _blueprint.getImageUrl();
        }
        return result;
    }

    public int getCardId() { return _cardId; }
    public Player getOwner() { return _owner; }

    public String getOwnerName() {
        if (_owner != null) {
            return _owner.getPlayerId();
        } else {
            return null;
        }
    }

    public void startAffectingGame(DefaultGame game) {
        game.getModifiersEnvironment().addModifierHooks(this);
    }

    public void stopAffectingGame(DefaultGame game) {
        game.getModifiersEnvironment().removeModifierHooks(this);
    }


    public CardBlueprint getBlueprint() {
        return _blueprint;
    }

    public void attachTo(PhysicalCard physicalCard) {
        _attachedToCardId = physicalCard.getCardId();
    }

    public void detach() {
        _attachedToCardId = null;
    }

    public PhysicalCard getAttachedTo() {
        if (_attachedToCardId == null) {
            return null;
        } else {
            try {
                return getGame().getCardFromCardId(_attachedToCardId);
            } catch(CardNotFoundException exp) {
                getGame().sendErrorMessage(exp);
                return null;
            }
        }
    }

    public void stackOn(PhysicalCard physicalCard) {
        _stackedOnCardId = physicalCard.getCardId();
    }


    public PhysicalCard getStackedOn() {
        if (_stackedOnCardId == null) {
            return null;
        } else {
            try {
                return getGame().getCardFromCardId(_stackedOnCardId);
            } catch(CardNotFoundException exp) {
                getGame().sendErrorMessage(exp);
                return null;
            }
        }
    }


    public String getTitle() { return _blueprint.getTitle(); }

    public boolean canInsertIntoSpaceline() { return _blueprint.canInsertIntoSpaceline(); }

    public int getLocationZoneIndex() {
        if (_currentLocation == null)
            return -1;
        else return _currentLocation.getLocationZoneIndex();
    }

    private boolean canEnterPlay(DefaultGame game, List<Requirement> requirements) {
        if (cannotEnterPlayPerUniqueness())
            return false;
        if (requirements != null && !createActionContext(game).acceptsAllRequirements(requirements))
            return false;
        return !game.getModifiersQuerying().canNotPlayCard(getOwnerName(), this);
    }


    protected boolean cannotEnterPlayPerUniqueness() {
        return isUnique() && (_owner.hasACopyOfCardInPlay(this));
    }

    public boolean canBeSeeded(DefaultGame game) { return canEnterPlay(game, _blueprint.getSeedRequirements()); }

    public boolean canBePlayed(DefaultGame game) { return canEnterPlay(game, _blueprint.getPlayRequirements()); }


    public boolean isControlledBy(String playerId) {
        // TODO - Need to set modifiers for when cards get temporary control
        // PhysicalFacilityCard has an override for headquarters. Updates to this method should be made there as well.
        if (!_zone.isInPlay())
            return false;
        return playerId.equals(_owner.getPlayerId());
    }
    public boolean isControlledBy(Player player) { return isControlledBy(player.getPlayerId()); }

    public String getCardLink() { return _blueprint.getCardLink(); }
    public MissionLocation getLocation() throws InvalidGameLogicException {
        if (_currentLocation == null)
            throw new InvalidGameLogicException("Tried to process card's location for a card not at any location");
        return _currentLocation;
    }

    public void setLocation(MissionLocation location) {
        _currentLocation = location;
        for (PhysicalCard attachedCard : getAttachedCards(getGame())) {
            attachedCard.setLocation(location);
        }
        for (PhysicalCard stackedCard : getStackedCards(getGame())) {
            stackedCard.setLocation(location);
        }
    }

    public String getFullName() { return _blueprint.getFullName(); }

    public TopLevelSelectableAction getPlayCardAction() {
        return getPlayCardAction(false);
    }
    public abstract TopLevelSelectableAction getPlayCardAction(boolean forFree);


    public boolean hasTextRemoved(DefaultGame game) {
        for (Modifier modifier :
                game.getModifiersQuerying().getModifiersAffectingCard(ModifierEffect.TEXT_MODIFIER, this)) {
            if (modifier.hasRemovedText(game, this))
                return true;
        }
        return false;
    }


    public boolean hasTransporters() {
        return false;
    }

    public CardType getCardType() { return _blueprint.getCardType(); }

    public List<TopLevelSelectableAction> getRulesActionsWhileInPlay(Player player) {
        return new LinkedList<>();
    }

    public List<TopLevelSelectableAction> getGameTextActionsWhileInPlay(Player player) {
        return _blueprint.getGameTextActionsWhileInPlay(player, this);
    }

    public List<PhysicalCard> getStackedCards(DefaultGame game) {
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCard card : game.getGameState().getAllCardsInGame()) {
            if (card.getStackedOn() == this)
                result.add(card);
        }
        return result;
    }

    public Collection<PhysicalCard> getAttachedCards(DefaultGame game) { return game.getGameState().getAttachedCards(this); }

    public List<? extends ExtraPlayCost> getExtraCostToPlay(DefaultGame game) {
        if (_blueprint.getExtraPlayCosts() == null)
            return null;

        List<ExtraPlayCost> result = new LinkedList<>();
        _blueprint.getExtraPlayCosts().forEach(
                extraPlayCost -> result.add(extraPlayCost.getExtraPlayCost(createActionContext(game))));
        return result;
    }

    public List<TopLevelSelectableAction> getOptionalInPlayActions(ActionResult actionResult, TriggerTiming timing) {
        List<TopLevelSelectableAction> result = new LinkedList<>();
        List<ActionSource> triggers = _blueprint.getActivatedTriggers(timing);

        if (triggers != null) {
            for (ActionSource trigger : triggers) {
                TopLevelSelectableAction action = trigger.createActionWithNewContext(this, actionResult);
                if (action != null)
                    result.add(action);
            }
        }
        return result;
    }



    public TopLevelSelectableAction getDiscardedFromPlayTriggerAction(RequiredType requiredType) {
        ActionSource actionSource = _blueprint.getDiscardedFromPlayTrigger(requiredType);
        return (actionSource == null) ?
                null : actionSource.createActionWithNewContext(this, null);
    }


    private List<TopLevelSelectableAction> getActionsFromActionSources(String playerId, PhysicalCard card,
                                                     ActionResult actionResult, List<ActionSource> actionSources) {
        List<TopLevelSelectableAction> result = new LinkedList<>();
        actionSources.forEach(actionSource -> {
            if (actionSource != null) {
                TopLevelSelectableAction action =
                        actionSource.createActionWithNewContext(card, playerId, actionResult);
                if (action != null) result.add(action);
            }
        });
        return result;
    }

    public List<TopLevelSelectableAction> getOptionalAfterTriggerActions(Player player, ActionResult actionResult) {
        return switch (_blueprint) {
            case Blueprint212_019 riskBlueprint -> riskBlueprint.getValidResponses(this, player, actionResult);
            case Blueprint156_010 surpriseBlueprint -> surpriseBlueprint.getValidResponses(this, player, actionResult);
            case Blueprint109_063 missionSpecBlueprint ->
                    missionSpecBlueprint.getValidResponses(this, player, actionResult);
            case null, default -> {
                assert _blueprint != null;
                yield getActionsFromActionSources(player.getPlayerId(), this, actionResult,
                        _blueprint.getBeforeOrAfterTriggers(RequiredType.OPTIONAL, TriggerTiming.AFTER));
            }
        };
    }

    public List<TopLevelSelectableAction> getRequiredResponseActions(ActionResult actionResult) {
        return _blueprint.getRequiredAfterTriggerActions(actionResult, this);
    }

    ActionContext createActionContext(DefaultGame game) {
        return new DefaultActionContext(getOwnerName(), game, this, null);
    }


    public boolean isUnique() {
        return _blueprint.isUnique();
    }

    public Integer getNumberOfCopiesSeededByPlayer(Player player) {
        int total = 0;
        Collection<Action> performedActions = player.getGame().getActionsEnvironment().getPerformedActions();
        for (Action action : performedActions) {
            if (action instanceof SeedCardAction seedCardAction) {
                if (Objects.equals(seedCardAction.getPerformingPlayerId(), player.getPlayerId()) &&
                        seedCardAction.getCardEnteringPlay().isCopyOf(this))
                    total += 1;
            }
        }
        return total;
    }

    public boolean isCopyOf(PhysicalCard card) {
        return card.getBlueprint() == _blueprint;
    }

    public TopLevelSelectableAction createSeedCardAction() {
        if (_blueprint.getSeedCardActionSource() == null)
            return null;
        else
            return _blueprint.getSeedCardActionSource().createActionWithNewContext(this);
    }

    public boolean hasIcon(DefaultGame game, CardIcon icon) {
        return game.getModifiersQuerying().hasIcon(this, icon);
    }


    public boolean isPresentWith(PhysicalCard card) {
        try {
            return card.getLocation() == this.getLocation() && card.getAttachedTo() == this.getAttachedTo();
        } catch(InvalidGameLogicException exp) {
            card.getGame().sendErrorMessage(exp);
            return false;
        }
    }

    public boolean hasSkill(SkillName skillName) { return false; }
    // TODO May need to implement something here for weird non-personnel cards that have skills

    public boolean checkTurnLimit(DefaultGame game, int max) {
        // TODO This isn't right since it checks against the card instead of the action
        return game.getModifiersQuerying().getUntilEndOfTurnLimitCounter(this).getUsedLimit() < max;
    }


    public boolean isInPlay() {
        if (_zone == null)
            return false;
        else return _zone.isInPlay();
    }

    public boolean hasCharacteristic(Characteristic characteristic) {
        return _blueprint.hasCharacteristic(characteristic);
    }


    public List<Action> getEncounterActions(DefaultGame game, AttemptingUnit attemptingUnit,
                                            EncounterSeedCardAction action, MissionLocation missionLocation)
            throws InvalidGameLogicException {
        throw new InvalidGameLogicException(
                "Tried to call getEncounterActions for a card that does not have an encounter action");
    }

    public boolean isAtPlanetLocation() {
        if (_currentLocation == null)
            return false;
        else return _currentLocation.isPlanet();
    }

    public boolean isAtSpaceLocation() {
        if (_currentLocation == null)
            return false;
        else return _currentLocation.isSpace();
    }

    public Player getController() {
        return _owner;
    }

    public int getCost() { return _blueprint.getCost(); }

    public void setPlacedOnMission(boolean placedOnMission) {
        _placedOnMission = placedOnMission;
    }

    public boolean isPlacedOnMission() { return _placedOnMission; }

}