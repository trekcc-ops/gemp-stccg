package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.AttachPermanentAction;
import com.gempukku.stccg.actions.playcard.STCCGPlayCardAction;
import com.gempukku.stccg.actions.sources.ActionSource;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.Blueprint155_021;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.common.filterable.Keyword;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.gempukku.stccg.modifiers.*;
import com.gempukku.stccg.requirement.Requirement;

import java.util.*;

public abstract class PhysicalCard implements Filterable {
    private final String _title; // Primarily stored here for easy access during debug
    protected final CardBlueprint _blueprint;
    protected final Player _owner;
    protected final int _cardId;
    protected String _imageUrl;
    protected Zone _zone;
    protected PhysicalCard _attachedTo;
    protected PhysicalCard _stackedOn;
    protected List<ModifierHook> _modifierHooks;
    protected final Map<Zone, List<ModifierHook>> _modifierHooksInZone = new HashMap<>(); // modifier hooks specific to stacked and discard
    private final Map<Zone, List<ModifierSource>> _modifiers = new HashMap<>(); // modifiers specific to stacked and discard
    protected Object _whileInZoneData;
    protected int _locationZoneIndex;
    protected ST1ELocation _currentLocation;

    public PhysicalCard(int cardId, Player owner, CardBlueprint blueprint) {
        _cardId = cardId;
        _owner = owner;
        _blueprint = blueprint;
        _imageUrl = blueprint.getImageUrl();
        _title = blueprint.getTitle();
    }

    public abstract DefaultGame getGame();
    
    public Zone getZone() { return _zone; }

    public void setZone(Zone zone) { _zone = zone; }
    
    public String getBlueprintId() { return _blueprint.getBlueprintId(); }
    public String getImageUrl() { return _imageUrl; }

    // Which player controls the card for purposes of the UI
    public String getCardControllerPlayerId() {
        if (isControlledBy(_owner))
            return _owner.getPlayerId();
        for (Player player : getGame().getPlayers())
            if (isControlledBy(player))
                return player.getPlayerId();
        return _owner.getPlayerId();
    }

    public int getCardId() { return _cardId; }
    public Player getOwner() { return _owner; }

    public String getOwnerName() {
        return _owner.getPlayerId();
    }

    public void startAffectingGame() {
        List<Modifier> modifiers = getInPlayModifiers();
        modifiers.addAll(_blueprint.getWhileInPlayModifiersNew(_owner, this));
        _modifierHooks = new LinkedList<>();
        for (Modifier modifier : modifiers)
            _modifierHooks.add(getGame().getModifiersEnvironment().addAlwaysOnModifier(modifier));
    }

    public void startAffectingGameInZone(Zone zone) {
        List<? extends Modifier> modifiers = null;
        if (zone == Zone.STACKED || zone == Zone.DISCARD)
            modifiers = getModifiers(_modifiers.get(zone));
        if (modifiers != null) {
            _modifierHooksInZone.put(zone, new LinkedList<>());
            for (Modifier modifier : modifiers)
                _modifierHooksInZone.get(zone).add(getGame().getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    public void stopAffectingGame() {
        if (_modifierHooks != null) {
            for (ModifierHook modifierHook : _modifierHooks)
                modifierHook.stop();
            _modifierHooks = null;
        }
    }

    public void appendModifierInZone(ModifierSource modifierSource, Zone zone) {
        _modifiers.computeIfAbsent(zone, k -> new LinkedList<>());
        _modifiers.get(zone).add(modifierSource);
    }

    public void stopAffectingGameInZone(Zone zone) {
        if (_modifierHooksInZone.get(zone) != null) {
            for (ModifierHook modifierHook : _modifierHooksInZone.get(zone))
                modifierHook.stop();
            _modifierHooksInZone.remove(zone);
        }
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

    
    public Object getWhileInZoneData() {
        return _whileInZoneData;
    }

    
    public void setWhileInZoneData(Object object) {
        _whileInZoneData = object;
    }


    public String getTitle() { return _title; }

    public boolean canInsertIntoSpaceline() { return _blueprint.canInsertIntoSpaceline(); }

    public void setLocationZoneIndex(int index) { _locationZoneIndex = index; }

    public int getLocationZoneIndex() {
        if (_currentLocation == null)
            return -1;
        else return _currentLocation.getLocationZoneIndex();
    }

    public Quadrant getQuadrant() { return _blueprint.getQuadrant(); }

    public boolean isAffectingGame() { return getGame().getCurrentPlayer() == _owner; }
    public boolean canEnterPlay(List<Requirement> requirements) {
        if (cannotEnterPlayPerUniqueness())
            return false;
        if (requirements != null && !createActionContext().acceptsAllRequirements(requirements))
            return false;
        return !getModifiers().canNotPlayCard(getOwnerName(), this);
    }

    protected boolean cannotEnterPlayPerUniqueness() {
        return isUnique() && _owner.hasACopyOfCardInPlay(this);
    }

    public boolean canBeSeeded() { return canEnterPlay(_blueprint.getSeedRequirements()); }
    public boolean canBePlayed() { return canEnterPlay(_blueprint.getPlayRequirements()); }

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

    public CostToEffectAction getPlayCardAction() {
        return getPlayCardAction(false);
    }
    public abstract CostToEffectAction getPlayCardAction(boolean forFree);

    public CostToEffectAction getPlayCardAction(Filterable additionalAttachmentFilter,
                                                boolean ignoreRoamingPenalty) {

            final Filterable validTargetFilter = _blueprint.getValidTargetFilter();
            if (validTargetFilter == null) {
                Zone playToZone = Zone.SUPPORT;
                CostToEffectAction action = new STCCGPlayCardAction((ST1EPhysicalCard) this, playToZone, this.getOwner());
                getModifiers().appendExtraCosts(action, this);
                getModifiers().appendPotentialDiscounts(action, this);

                return action;
            } else {
                Filter fullAttachValidTargetFilter = Filters.and(_blueprint.getValidTargetFilter(),
                        (Filter) (game1, targetCard) -> getModifiers().canHavePlayedOn(
                                this, targetCard),
                        (Filter) (game12, physicalCard) -> true);

                final AttachPermanentAction action = new AttachPermanentAction(this,
                        Filters.and(fullAttachValidTargetFilter, additionalAttachmentFilter));

                getModifiers().appendPotentialDiscounts(action, this);
                getModifiers().appendExtraCosts(action, this);

                return action;
            }
    }

    public List<Modifier> getModifiers(List<ModifierSource> sources) {
        List<Modifier> result = new LinkedList<>();
        if (sources != null)
            sources.forEach(inPlayModifier -> result.add(inPlayModifier.getModifier(createActionContext())));
        return result;
    }

    public boolean hasTextRemoved() {
        for (Modifier modifier : getModifiers().getModifiersAffectingCard(ModifierEffect.TEXT_MODIFIER, this)) {
            if (modifier.hasRemovedText(getGame(), this))
                return true;
        }
        return false;
    }

    public boolean hasTransporters() {
        return false;
    }

    public CardType getCardType() { return _blueprint.getCardType(); }

    public List<? extends Action> getPhaseActionsInPlay(Player player) { return getPhaseActionsInPlay(player.getPlayerId()); }

    public void attachToCardAtLocation(PhysicalCard destinationCard) {
        getGame().getGameState().transferCard(this, destinationCard);
        setLocation(destinationCard.getLocation());
    }

    public String getCardInfoHTML() {
        if (getZone().isInPlay() || getZone() == Zone.HAND) {
            StringBuilder sb = new StringBuilder();

/*            if (getZone() == Zone.HAND)
                sb.append("<b>Card is in hand - stats are only provisional</b><br><br>");
            else if (Filters.filterActive(getGame(), this).isEmpty())
                sb.append("<b>Card is inactive - current stats may be inaccurate</b><br><br>");*/

            Collection<Modifier> modifiers = getModifiers().getModifiersAffecting(this);
            if (!modifiers.isEmpty()) {
                sb.append("<b>Active modifiers:</b><br>");
                for (Modifier modifier : modifiers) {
                    sb.append(modifier.getCardInfoText(this));
                }
            }
/*
            List<PhysicalCard> stackedCards = getStackedCards();
            if (!stackedCards.isEmpty()) {
                sb.append("<br><b>Stacked cards:</b>");
                sb.append("<br>").append(TextUtils.getConcatenatedCardLinks(stackedCards));
            }
*/
            StringBuilder keywords = new StringBuilder();
            for (Keyword keyword : Keyword.values()) {
                if (keyword.isInfoDisplayable()) {
                    if (keyword.isMultiples()) {
                        int count = getModifiers().getKeywordCount(this, keyword);
                        if (count > 0)
                            keywords.append(keyword.getHumanReadable()).append(" +").append(count).append(", ");
                    } else {
                        if (getModifiers().hasKeyword(this, keyword))
                            keywords.append(keyword.getHumanReadable()).append(", ");
                    }
                }
            }
            if (!keywords.isEmpty())
                sb.append("<br><b>Keywords:</b> ").append(keywords.substring(0, keywords.length() - 2));

            return sb.toString();
        } else {
            return "";
        }
    }

    public List<PhysicalCard> getStackedCards() {
        List<PhysicalCard> result = new LinkedList<>();
        for (List<PhysicalCard> physicalCardList : getGame().getGameState().getStackedCards().values()) {
            for (PhysicalCard physicalCard : physicalCardList) {
                if (physicalCard.getStackedOn() == this)
                    result.add(physicalCard);
            }
        }
        return result;
    }
    public Collection<PhysicalCard> getAttachedCards() { return getGame().getGameState().getAttachedCards(this); }

    public List<? extends Action> getPhaseActionsInPlay(String playerId) {
            // TODO - Very jank just to see if I can get the Java blueprint to work
        if (_blueprint instanceof Blueprint155_021 testCard)
            return testCard.getInPlayActionsNew(getGame().getGameState().getCurrentPhase(), this);
        else {
            if (_blueprint.getInPlayPhaseActions() == null)
                return new LinkedList<>();
            else
                return getActivatedActions(playerId, _blueprint.getInPlayPhaseActions());
        }
    }

    public List<? extends Action> getPhaseActionsFromZone(String playerId, Zone zone) {
        if (zone == Zone.DISCARD) {
            return getActivatedActions(playerId, _blueprint.getInDiscardPhaseActions());
        }
        else if (zone == Zone.HAND) {
            if (_blueprint.getPlayInOtherPhaseConditions() == null)
                return null;
            ActionContext actionContext = createActionContext(playerId);
            List<Action> playCardActions = new LinkedList<>();

            if (canBePlayed()) {
                for (Requirement playInOtherPhaseCondition : _blueprint.getPlayInOtherPhaseConditions()) {
                    if (playInOtherPhaseCondition.accepts(actionContext))
                        playCardActions.add(getPlayCardAction(Filters.any, false));
                }
            }
            return playCardActions;
        }
        else return null;
    }

    public List<Modifier> getInPlayModifiers() {
        return getModifiers(_blueprint.getInPlayModifiers());
    }

    public List<? extends ExtraPlayCost> getExtraCostToPlay() {
        if (_blueprint.getExtraPlayCosts() == null)
            return null;

        List<ExtraPlayCost> result = new LinkedList<>();
        for (ExtraPlayCostSource extraPlayCost : _blueprint.getExtraPlayCosts()) {
            result.add(extraPlayCost.getExtraPlayCost(createActionContext()));
        }

        return result;
    }

    public List<Action> getOptionalInPlayActions(Effect effect, TriggerTiming timing) {
        List<Action> result = new LinkedList<>();
        List<ActionSource> triggers = _blueprint.getActivatedTriggers(timing);

        if (triggers != null) {
            for (ActionSource trigger : triggers) {
                Action action = getActionFromActionSource(trigger, getOwnerName(), effect);
                if (action != null)
                    result.add(action);
            }
        }
        return result;
    }

    public List<Action> getOptionalInPlayActions(EffectResult effectResult, TriggerTiming timing) {
        List<Action> result = new LinkedList<>();
        List<ActionSource> triggers = _blueprint.getActivatedTriggers(timing);

        if (triggers != null) {
            for (ActionSource trigger : triggers) {
                Action action = getActionFromActionSource(trigger, getOwnerName(), effectResult);
                if (action != null)
                    result.add(action);
            }
        }
        return result;
    }



    public Action getDiscardedFromPlayTriggerAction(RequiredType requiredType) {
        ActionSource actionSource = _blueprint.getDiscardedFromPlayTrigger(requiredType);
        if (actionSource == null)
            return null;
        else
            return getActionFromActionSource(actionSource);
    }

    private Action getActionFromActionSource(ActionSource actionSource) {
        return actionSource.createActionAndAppendToContext(this, createActionContext());
    }

    private Action getActionFromActionSource(ActionSource actionSource, String playerId, EffectResult effectResult) {
        return actionSource.createActionAndAppendToContext(this, createActionContext(playerId, effectResult));
    }

    private Action getActionFromActionSource(ActionSource actionSource, String playerId, Effect effect) {
        return actionSource.createActionAndAppendToContext(this, createActionContext(playerId, effect));
    }

    private Action getActionFromActionSource(ActionSource actionSource, String playerId, Effect effect, EffectResult effectResult) {
        return actionSource.createActionAndAppendToContext(this, createActionContext(playerId, effect, effectResult));
    }


    private List<Action> getActionsFromActionSources(String playerId, List<ActionSource> actionSources) {
        return getActionsFromActionSources(playerId, null, null, actionSources);
    }

    private List<Action> getActionsFromActionSources(String playerId, Effect effect, List<ActionSource> actionSources) {
        return getActionsFromActionSources(playerId, effect, null, actionSources);
    }

    private List<Action> getActionsFromActionSources(String playerId, EffectResult effectResult, List<ActionSource> actionSources) {
        return getActionsFromActionSources(playerId, null, effectResult, actionSources);
    }

    private List<Action> getActionsFromActionSources(String playerId, Effect effect, EffectResult effectResult,
                                                    List<ActionSource> actionSources) {
        List<Action> result = new LinkedList<>();
        actionSources.forEach(actionSource -> {
            if (actionSource != null) {
                Action action = getActionFromActionSource(actionSource, playerId, effect, effectResult);
                if (action != null)
                    result.add(action);
            }
        });
        return result;
    }

    private List<Action> getActivatedActions(String playerId, List<ActionSource> sources) {
        return getActionsFromActionSources(playerId, sources);
    }

    public List<Action> getOptionalAfterTriggerActions(String playerId, EffectResult effectResult) {
        return getActionsFromActionSources(
                playerId, effectResult, getBlueprint().getBeforeOrAfterTriggers(RequiredType.OPTIONAL, TriggerTiming.AFTER));
    }

    public List<Action> getBeforeTriggerActions(Effect effect, RequiredType requiredType) {
        return getActionsFromActionSources(getOwnerName(), effect, _blueprint.getBeforeOrAfterTriggers(requiredType, TriggerTiming.BEFORE));
    }

    public List<Action> getBeforeTriggerActions(String playerId, Effect effect, RequiredType requiredType) {
        return getActionsFromActionSources(
                playerId, effect, _blueprint.getBeforeOrAfterTriggers(requiredType, TriggerTiming.BEFORE));
    }

    public List<Action> getAfterTriggerActions(EffectResult effectResult, RequiredType requiredType) {
        return getActionsFromActionSources(getOwnerName(), effectResult, _blueprint.getBeforeOrAfterTriggers(requiredType, TriggerTiming.AFTER));
    }

    public List<Action> getAfterTriggerActions(Player player, EffectResult effectResult, RequiredType requiredType) {
        return getActionsFromActionSources(player.getPlayerId(), effectResult, _blueprint.getBeforeOrAfterTriggers(requiredType, TriggerTiming.AFTER));
    }


    public List<Action> getOptionalInHandAfterTriggerActions(String playerId, EffectResult effectResult) {
        return getActionsFromActionSources(playerId, effectResult, getBlueprint().getOptionalInHandTriggers(TriggerTiming.AFTER));
    }

    public ActionContext createActionContext() { return createActionContext(getOwnerName(), null, null); }
    public ActionContext createActionContext(String playerId) { return createActionContext(playerId, null, null); }

    public ActionContext createActionContext(String playerId, EffectResult effectResult) { return createActionContext(playerId, null, effectResult); }
    public ActionContext createActionContext(String playerId, Effect effect) { return createActionContext(playerId, effect, null); }
    public ActionContext createActionContext(String playerId, Effect effect, EffectResult effectResult) {
        return new DefaultActionContext(playerId, getGame(), this, effect, effectResult);
    }

    public int getPotentialDiscount() {
        if (_blueprint.getDiscountSources() == null)
            return 0;

        int result = 0;
        for (DiscountSource discountSource : _blueprint.getDiscountSources())
            result += discountSource.getPotentialDiscount(createActionContext());

        return result;
    }

    public void appendPotentialDiscountEffectsToAction(CostToEffectAction action) {
        if (_blueprint.getDiscountSources() != null) {
            ActionContext actionContext = createActionContext();
            for (DiscountSource discountSource : _blueprint.getDiscountSources()) {
                action.appendPotentialDiscount(discountSource.getDiscountEffect(action, actionContext));
            }
        }
    }

    public boolean isUnique() {
        return _blueprint.isUnique();
    }

    public Integer getNumberOfCopiesSeededByPlayer(Player player) {
        int total = 0;
        for (PhysicalCard seededCard : player.getCardsSeeded()) {
            if (seededCard.isCopyOf(this))
                total += 1;
        }
        return total;
    }

    public boolean isCopyOf(PhysicalCard card) {
        return card.getBlueprint() == _blueprint;
    }

    public Action createSeedCardAction() {
        ActionSource actionSource = _blueprint.getSeedCardActionSource();
        return actionSource.createActionAndAppendToContext(this, createActionContext());
    }

    public boolean hasIcon(CardIcon icon) {
        return getModifiers().hasIcon(this, icon);
    }

    public boolean isPresentWith(PhysicalCard card) {
        return card.getLocation() == this.getLocation() && card.getAttachedTo() == this.getAttachedTo();
        // TODO Elaborate on this definition
    }

    public boolean hasSkill(SkillName skillName) { return false; }
        // TODO May need to implement something here for weird non-personnel cards that have skills

    public boolean checkTurnLimit(int max) {
        return getModifiers().getUntilEndOfTurnLimitCounter(this).getUsedLimit() < max;
    }

    public boolean checkPhaseLimit(Phase phase, int max) {
        return getModifiers().getUntilEndOfPhaseLimitCounter(this, phase).getUsedLimit() < max;
    }

    public boolean checkPhaseLimit(int max) {
        return checkPhaseLimit(getGame().getGameState().getCurrentPhase(), max);
    }

    public boolean checkPhaseLimit(String prefix, int max) {
        return getModifiers().getUntilEndOfPhaseLimitCounter(this, prefix, getGame().getGameState().getCurrentPhase()).getUsedLimit() < max;
    }

    private ModifiersQuerying getModifiers() {
        return getGame().getModifiersQuerying();
    }

    public boolean isInPlay() {
        if (_zone == null)
            return false;
        else return _zone.isInPlay();
    }

    public boolean hasCharacteristic(Characteristic characteristic) {
        return _blueprint.hasCharacteristic(characteristic);
    }
}