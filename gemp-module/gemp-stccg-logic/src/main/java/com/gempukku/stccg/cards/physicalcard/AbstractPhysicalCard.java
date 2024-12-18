package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.AttemptingUnit;
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
    protected PhysicalCard _attachedTo;
    protected PhysicalCard _stackedOn;
    protected MissionLocation _currentLocation;

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
        _attachedTo = physicalCard;
    }

    public void detach() {
        _attachedTo = null;
    }

    public PhysicalCard getAttachedTo() {
        return _attachedTo;
    }

    public void stackOn(PhysicalCard physicalCard) {
        _stackedOn = physicalCard;
    }


    public PhysicalCard getStackedOn() {
        return _stackedOn;
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
    }

    public String getFullName() { return _blueprint.getFullName(); }

    public Action getPlayCardAction() {
        return getPlayCardAction(false);
    }
    public abstract Action getPlayCardAction(boolean forFree);


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

    public List<? extends Action> getRulesActionsWhileInPlay(Player player) {
        return new LinkedList<>();
    }

    public List<? extends Action> getGameTextActionsWhileInPlay(Player player) {
        return _blueprint.getGameTextActionsWhileInPlay(player, this);
    }

    public List<PhysicalCard> getStackedCards(DefaultGame game) {
        List<PhysicalCard> result = new LinkedList<>();
        for (List<PhysicalCard> physicalCardList : game.getGameState().getStackedCards().values()) {
            for (PhysicalCard physicalCard : physicalCardList) {
                if (physicalCard.getStackedOn() == this)
                    result.add(physicalCard);
            }
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

    public List<Action> getOptionalInPlayActions(Effect effect, TriggerTiming timing) {
        List<Action> result = new LinkedList<>();
        List<ActionSource> triggers = _blueprint.getActivatedTriggers(timing);

        if (triggers != null) {
            for (ActionSource trigger : triggers) {
                Action action = trigger.createActionWithNewContext(this, effect, null);
                if (action != null) result.add(action);
            }
        }
        return result;
    }

    public List<Action> getOptionalInPlayActions(EffectResult effectResult, TriggerTiming timing) {
        List<Action> result = new LinkedList<>();
        List<ActionSource> triggers = _blueprint.getActivatedTriggers(timing);

        if (triggers != null) {
            for (ActionSource trigger : triggers) {
                Action action = trigger.createActionWithNewContext(this, null, effectResult);
                if (action != null)
                    result.add(action);
            }
        }
        return result;
    }



    public Action getDiscardedFromPlayTriggerAction(RequiredType requiredType) {
        ActionSource actionSource = _blueprint.getDiscardedFromPlayTrigger(requiredType);
        return (actionSource == null) ?
                null : actionSource.createActionWithNewContext(this, null, null);
    }


    private List<Action> getActionsFromActionSources(String playerId, PhysicalCard card, Effect effect,
                                                     EffectResult effectResult, List<ActionSource> actionSources) {
        List<Action> result = new LinkedList<>();
        actionSources.forEach(actionSource -> {
            if (actionSource != null) {
                Action action = actionSource.createActionWithNewContext(card, playerId, effect, effectResult);
                if (action != null) result.add(action);
            }
        });
        return result;
    }

    public List<Action> getOptionalAfterTriggerActions(Player player, EffectResult effectResult) {
        if (_blueprint instanceof Blueprint212_019 riskBlueprint) {
            return riskBlueprint.getValidResponses(this, player, effectResult);
        }
        else if (_blueprint instanceof Blueprint156_010 surpriseBlueprint) {
            return surpriseBlueprint.getValidResponses(this, player, effectResult);
        }
        else if (_blueprint instanceof Blueprint109_063 missionSpecBlueprint) {
            return missionSpecBlueprint.getValidResponses(this, player, effectResult);
        }
        else {
            return getActionsFromActionSources(player.getPlayerId(), this, null, effectResult,
                    _blueprint.getBeforeOrAfterTriggers(RequiredType.OPTIONAL, TriggerTiming.AFTER));
        }
    }

    public List<Action> getBeforeTriggerActions(Effect effect, RequiredType requiredType) {
        return getActionsFromActionSources(getOwnerName(), this, effect, null,
                _blueprint.getBeforeOrAfterTriggers(requiredType, TriggerTiming.BEFORE));
    }

    public List<Action> getBeforeTriggerActions(String playerId, Effect effect, RequiredType requiredType) {
        return getActionsFromActionSources(playerId, this, effect, null,
                _blueprint.getBeforeOrAfterTriggers(requiredType, TriggerTiming.BEFORE));
    }

    public List<Action> getRequiredResponseActions(EffectResult effectResult) {
        return _blueprint.getRequiredAfterTriggerActions(effectResult, this);
    }

    ActionContext createActionContext(DefaultGame game) {
        return new DefaultActionContext(getOwnerName(), game, this, null, null);
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

    public Action createSeedCardAction() {
        if (_blueprint.getSeedCardActionSource() == null)
            return null;
        else
            return _blueprint.getSeedCardActionSource().createActionWithNewContext(this);
    }

    public boolean hasIcon(DefaultGame game, CardIcon icon) {
        return game.getModifiersQuerying().hasIcon(this, icon);
    }


    public boolean isPresentWith(PhysicalCard card) {
        // TODO Elaborate on this definition
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

}