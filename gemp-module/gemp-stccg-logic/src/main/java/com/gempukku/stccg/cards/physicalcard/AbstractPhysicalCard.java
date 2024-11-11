package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.playcard.STCCGPlayCardAction;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.blueprints.Blueprint155_021;
import com.gempukku.stccg.cards.blueprints.Blueprint212_019;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.actionsource.ActionSource;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.gempukku.stccg.modifiers.ExtraPlayCost;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ModifierEffect;

import java.util.*;

public abstract class AbstractPhysicalCard implements PhysicalCard {

    protected final CardBlueprint _blueprint;
    protected final Player _owner;
    protected final int _cardId;
    protected Zone _zone;
    protected PhysicalCard _attachedTo;
    protected PhysicalCard _stackedOn;
    protected ST1ELocation _currentLocation;
    protected Map<Player, List<PhysicalCard>> _cardsPreSeededUnderneath = new HashMap<>();
    protected List<PhysicalCard> _cardsSeededUnderneath = new LinkedList<>();

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
        return _owner.getPlayerId();
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
    public ST1ELocation getLocation() { return _currentLocation; }

    public void setLocation(ST1ELocation location) {
        _currentLocation = location;
    }

    public String getFullName() { return _blueprint.getFullName(); }

    public Action getPlayCardAction() {
        return getPlayCardAction(false);
    }
    public abstract Action getPlayCardAction(boolean forFree);

    public Action getPlayCardAction(DefaultGame game, Filterable additionalAttachmentFilter) {

        final Filterable validTargetFilter = _blueprint.getValidTargetFilter();
        if (validTargetFilter == null) {
            Action action =
                    new STCCGPlayCardAction((ST1EPhysicalCard) this, Zone.SUPPORT, this.getOwner());
            game.getModifiersQuerying().appendExtraCosts(action, this);
            return action;
        } else {
            Filter fullAttachValidTargetFilter = Filters.and(
                    validTargetFilter,
                    (Filter) (game1, targetCard) -> game1.getModifiersQuerying().canHavePlayedOn(this,
                            targetCard),
                    (Filter) (game12, physicalCard) -> true
            );
            final AttachPermanentAction action = new AttachPermanentAction(this,
                    Filters.and(fullAttachValidTargetFilter, additionalAttachmentFilter));
            game.getModifiersQuerying().appendExtraCosts(action, this);
            return action;
        }
    }


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

    public List<? extends Action> getPhaseActionsInPlay(Player player) {
        // TODO - Very jank just to see if I can get the Java blueprint to work
        if (_blueprint instanceof Blueprint155_021 testCard)
            return testCard.getInPlayActionsNew(player, this);
        else {
            if (_blueprint.getInPlayPhaseActions() == null)
                return new LinkedList<>();
            else
                return getActivatedActions(player.getPlayerId(), _blueprint.getInPlayPhaseActions());
        }
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

    public List<? extends Action> getPhaseActionsFromZone(Player player, Zone zone) {
        DefaultGame game = player.getGame();
        if (zone == Zone.DISCARD) {
            return getActivatedActions(player.getPlayerId(), _blueprint.getInDiscardPhaseActions());
        }
        else if (zone == Zone.HAND) {
            if (_blueprint.getPlayInOtherPhaseConditions() == null)
                return null;
            List<Action> playCardActions = new LinkedList<>();

            if (canBePlayed(game)) {
                for (Requirement playInOtherPhaseCondition : _blueprint.getPlayInOtherPhaseConditions()) {
                    if (playInOtherPhaseCondition.accepts(createActionContext(player, null, null)))
                        playCardActions.add(getPlayCardAction(game, Filters.any));
                }
            }
            return playCardActions;
        }
        else return null;
    }

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


    private List<Action> getActionsFromActionSources(String playerId, Effect effect, EffectResult effectResult,
                                                     List<ActionSource> actionSources) {
        List<Action> result = new LinkedList<>();
        actionSources.forEach(actionSource -> {
            if (actionSource != null) {
                Action action = actionSource.createActionWithNewContext(this, playerId, effect, effectResult);
                if (action != null) result.add(action);
            }
        });
        return result;
    }

    private List<Action> getActivatedActions(String playerId, List<ActionSource> sources) {
        return getActionsFromActionSources(playerId, null, null, sources);
    }

    public List<Action> getOptionalAfterTriggerActions(Player player, EffectResult effectResult) {
        if (_blueprint instanceof Blueprint212_019 riskBlueprint) {
            return riskBlueprint.getValidResponses(this, player, effectResult);
        } else {
            return getActionsFromActionSources(player.getPlayerId(), null, effectResult,
                    _blueprint.getBeforeOrAfterTriggers(RequiredType.OPTIONAL, TriggerTiming.AFTER));
        }
    }

    public List<Action> getBeforeTriggerActions(Effect effect, RequiredType requiredType) {
        return getActionsFromActionSources(getOwnerName(), effect, null,
                _blueprint.getBeforeOrAfterTriggers(requiredType, TriggerTiming.BEFORE));
    }

    public List<Action> getBeforeTriggerActions(String playerId, Effect effect, RequiredType requiredType) {
        return getActionsFromActionSources(playerId, effect, null,
                _blueprint.getBeforeOrAfterTriggers(requiredType, TriggerTiming.BEFORE));
    }

    public List<Action> getRequiredResponseActions(EffectResult effectResult) {
        return _blueprint.getRequiredAfterTriggerActions(effectResult, this);
    }

    ActionContext createActionContext(DefaultGame game) {
        return new DefaultActionContext(getOwnerName(), game, this, null, null);
    }


    public ActionContext createActionContext(Player player, Effect effect, EffectResult effectResult) {
        return new DefaultActionContext(player.getPlayerId(), player.getGame(), this, effect, effectResult);
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
        return card.getLocation() == this.getLocation() && card.getAttachedTo() == this.getAttachedTo();
        // TODO Elaborate on this definition
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

    public void addCardToSeededUnder(PhysicalCard card) {
        _cardsSeededUnderneath.add(card);
    }

    public List<PhysicalCard> getCardsSeededUnderneath() { return _cardsSeededUnderneath; }
    public Collection<PhysicalCard> getCardsPreSeeded(Player player) {
        if (_cardsPreSeededUnderneath.get(player) == null)
            return new LinkedList<>();
        else return _cardsPreSeededUnderneath.get(player);
    }

    public void removeSeedCard(PhysicalCard card) {
        _cardsSeededUnderneath.remove(card);
    }

    public void removePreSeedCard(PhysicalCard card, Player player) {
        _cardsPreSeededUnderneath.get(player).remove(card);
    }

    public void seedPreSeeds() {
        // TODO - This won't work quite right for shared missions
        Set<Player> playersWithSeeds = _cardsPreSeededUnderneath.keySet();
        for (Player player : playersWithSeeds) {
            for (PhysicalCard card : _cardsPreSeededUnderneath.get(player)) {
                _cardsSeededUnderneath.add(card);
            }
            _cardsPreSeededUnderneath.remove(player);
        }
    }


    public void addCardToPreSeeds(PhysicalCard card, Player player) {
        _cardsPreSeededUnderneath.computeIfAbsent(player, k -> new LinkedList<>());
        _cardsPreSeededUnderneath.get(player).add(card);
    }

}