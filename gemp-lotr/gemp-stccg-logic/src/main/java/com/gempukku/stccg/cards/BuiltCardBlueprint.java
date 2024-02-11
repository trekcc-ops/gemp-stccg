package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.results.EffectResult;
import com.gempukku.stccg.effects.DiscountEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.modifiers.ExtraPlayCost;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementUtils;

import java.util.*;

public class BuiltCardBlueprint implements CardBlueprint {
    private String title;
    private String location;
    private String subtitle;
    private String _lore;
    private String imageUrl;
    private Uniqueness uniqueness = null;
    private Side side;
    private CardType cardType;
    private Set<Affiliation> _affiliations = new HashSet<>();
    private Region region;
    private Quadrant quadrant;
    private boolean _canInsertIntoSpaceline;
    private PropertyLogo _propertyLogo;
    private Set<Affiliation> _ownerAffiliationIcons = new HashSet<>();
    private Set<Affiliation> _opponentAffiliationIcons = new HashSet<>();
    private boolean _anyCrewOrAwayTeamCanAttempt;
    private MissionType _missionType;
    private FacilityType _facilityType;
    private Culture culture;
    private Race race;
    private Map<Keyword, Integer> keywords;
    private int cost = -1;
    private int strength;
    private int vitality;
    private int resistance;
    private int tribbleValue;
    private TribblePower tribblePower;
    private int siteNumber;
    private Set<PossessionClass> possessionClasses;

    private List<Requirement> requirements;
    private List<FilterableSource> targetFilters;
    private List<Phase> seedPhases;

    private List<ActionSource> requiredBeforeTriggers;
    private List<ActionSource> requiredAfterTriggers;
    private List<ActionSource> optionalBeforeTriggers;
    private List<ActionSource> optionalAfterTriggers;

    private List<ActionSource> beforeActivatedTriggers;
    private List<ActionSource> afterActivatedTriggers;

    private List<ActionSource> optionalInHandBeforeActions;
    private List<ActionSource> optionalInHandAfterActions;

    private List<ActionSource> optionalInHandAfterTriggers;

    private List<ActionSource> inPlayPhaseActions;
    private List<ActionSource> inDiscardPhaseActions;
    private List<ActionSource> fromStackedPhaseActions;

    private List<ModifierSource> inPlayModifiers;
    private List<ModifierSource> stackedOnModifiers;
    private List<ModifierSource> inDiscardModifiers;

    private List<TwilightCostModifierSource> twilightCostModifiers;

    private List<ExtraPlayCostSource> extraPlayCosts;
    private List<DiscountSource> discountSources;

    private List<Requirement<TribblesActionContext>> playInOtherPhaseConditions;
    private List<Requirement<TribblesActionContext>> playOutOfSequenceConditions;
    private List<FilterableSource> copiedFilters;

    private ActionSource playEventAction;
    private ActionSource killedRequiredTriggerAction;
    private ActionSource killedOptionalTriggerAction;
    private ActionSource discardedFromPlayRequiredTriggerAction;
    private ActionSource discardedFromPlayOptionalTriggerAction;

    public BuiltCardBlueprint() {
    }

    // Building methods

    public void appendCopiedFilter(FilterableSource filterableSource) {
        if (copiedFilters == null)
            copiedFilters = new LinkedList<>();
        copiedFilters.add(filterableSource);
    }

    public void appendPlayInOtherPhaseCondition(Requirement requirement) {
        if (playInOtherPhaseConditions == null)
            playInOtherPhaseConditions = new LinkedList<>();
        playInOtherPhaseConditions.add(requirement);
    }

    public void appendPlayOutOfSequenceCondition(Requirement<TribblesActionContext> requirement) {
        if (playOutOfSequenceConditions == null)
            playOutOfSequenceConditions = new LinkedList<>();
        playOutOfSequenceConditions.add(requirement);
    }

    public void appendDiscountSource(DiscountSource discountSource) {
        if (discountSources == null)
            discountSources = new LinkedList<>();
        discountSources.add(discountSource);
    }

    public void appendOptionalInHandAfterTrigger(ActionSource actionSource) {
        if (optionalInHandAfterTriggers == null)
            optionalInHandAfterTriggers = new LinkedList<>();
        optionalInHandAfterTriggers.add(actionSource);
    }

    public void appendOptionalInHandBeforeAction(ActionSource actionSource) {
        if (optionalInHandBeforeActions == null)
            optionalInHandBeforeActions = new LinkedList<>();
        optionalInHandBeforeActions.add(actionSource);
    }

    public void appendOptionalInHandAfterAction(ActionSource actionSource) {
        if (optionalInHandAfterActions == null)
            optionalInHandAfterActions = new LinkedList<>();
        optionalInHandAfterActions.add(actionSource);
    }

    public void appendExtraPlayCost(ExtraPlayCostSource extraPlayCostSource) {
        if (extraPlayCosts == null)
            extraPlayCosts = new LinkedList<>();
        extraPlayCosts.add(extraPlayCostSource);
    }

    public void appendBeforeActivatedTrigger(ActionSource actionSource) {
        if (beforeActivatedTriggers == null)
            beforeActivatedTriggers = new LinkedList<>();
        beforeActivatedTriggers.add(actionSource);
    }

    public void appendAfterActivatedTrigger(ActionSource actionSource) {
        if (afterActivatedTriggers == null)
            afterActivatedTriggers = new LinkedList<>();
        afterActivatedTriggers.add(actionSource);
    }

    public void appendRequiredBeforeTrigger(ActionSource actionSource) {
        if (requiredBeforeTriggers == null)
            requiredBeforeTriggers = new LinkedList<>();
        requiredBeforeTriggers.add(actionSource);
    }

    public void appendRequiredAfterTrigger(ActionSource actionSource) {
        if (requiredAfterTriggers == null)
            requiredAfterTriggers = new LinkedList<>();
        requiredAfterTriggers.add(actionSource);
    }

    public void appendOptionalBeforeTrigger(ActionSource actionSource) {
        if (optionalBeforeTriggers == null)
            optionalBeforeTriggers = new LinkedList<>();
        optionalBeforeTriggers.add(actionSource);
    }

    public void appendOptionalAfterTrigger(ActionSource actionSource) {
        if (optionalAfterTriggers == null)
            optionalAfterTriggers = new LinkedList<>();
        optionalAfterTriggers.add(actionSource);
    }

    public void appendPlayRequirement(Requirement requirement) {
        if (requirements == null)
            requirements = new LinkedList<>();
        requirements.add(requirement);
    }

    public void appendInPlayModifier(ModifierSource modifierSource) {
        if (inPlayModifiers == null)
            inPlayModifiers = new LinkedList<>();
        inPlayModifiers.add(modifierSource);
    }

    public void appendStackedOnModifier(ModifierSource modifierSource) {
        if (stackedOnModifiers == null)
            stackedOnModifiers = new LinkedList<>();
        stackedOnModifiers.add(modifierSource);
    }

    public void appendInDiscardModifier(ModifierSource modifierSource) {
        if (inDiscardModifiers == null)
            inDiscardModifiers = new LinkedList<>();
        inDiscardModifiers.add(modifierSource);
    }

    public void appendTargetFilter(FilterableSource targetFilter) {
        if (targetFilters == null)
            targetFilters = new LinkedList<>();
        targetFilters.add(targetFilter);
    }

    public void appendInPlayPhaseAction(ActionSource actionSource) {
        if (inPlayPhaseActions == null)
            inPlayPhaseActions = new LinkedList<>();
        inPlayPhaseActions.add(actionSource);
    }

    public void appendInDiscardPhaseAction(ActionSource actionSource) {
        if (inDiscardPhaseActions == null)
            inDiscardPhaseActions = new LinkedList<>();
        inDiscardPhaseActions.add(actionSource);
    }

    public void appendFromStackedPhaseAction(ActionSource actionSource) {
        if (fromStackedPhaseActions == null)
            fromStackedPhaseActions = new LinkedList<>();
        fromStackedPhaseActions.add(actionSource);
    }

    public void appendTwilightCostModifier(TwilightCostModifierSource twilightCostModifierSource) {
        if (twilightCostModifiers == null)
            twilightCostModifiers = new LinkedList<>();
        twilightCostModifiers.add(twilightCostModifierSource);
    }

    public void setPlayEventAction(ActionSource playEventAction) {
        this.playEventAction = playEventAction;
    }

    public void setKilledRequiredTriggerAction(ActionSource killedRequiredTriggerAction) {
        this.killedRequiredTriggerAction = killedRequiredTriggerAction;
    }

    public void setKilledOptionalTriggerAction(ActionSource killedOptionalTriggerAction) {
        this.killedOptionalTriggerAction = killedOptionalTriggerAction;
    }

    public void setDiscardedFromPlayRequiredTriggerAction(ActionSource discardedFromPlayRequiredTriggerAction) {
        this.discardedFromPlayRequiredTriggerAction = discardedFromPlayRequiredTriggerAction;
    }

    public void setDiscardedFromPlayOptionalTriggerAction(ActionSource discardedFromPlayOptionalTriggerAction) {
        this.discardedFromPlayOptionalTriggerAction = discardedFromPlayOptionalTriggerAction;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public void setUniqueness(Uniqueness uniqueness) { this.uniqueness = uniqueness;}
    public Uniqueness getUniqueness() { return this.uniqueness;}

    public void setSide(Side side) {
        this.side = side;
    }
    public void addAffiliation(Affiliation affiliation) { _affiliations.add(affiliation); }
    public Set<Affiliation> getAffiliations() { return _affiliations; }
    public void setQuadrant(Quadrant quadrant) {
        this.quadrant = quadrant;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public void setRegion(Region region) { this.region = region; }
    public Region getRegion() { return this.region; }

    public Quadrant getQuadrant() { return this.quadrant; }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }
    public void setFacilityType(FacilityType facilityType) { _facilityType = facilityType; }
    public FacilityType getFacilityType() { return _facilityType; }

    public void setCulture(Culture culture) {
        this.culture = culture;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public void setKeywords(Map<Keyword, Integer> keywords) {
        this.keywords = keywords;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public void setVitality(int vitality) {
        this.vitality = vitality;
    }

    public void setResistance(int resistance) {
        this.resistance = resistance;
    }

    public void setTribbleValue(int tribbleValue) {
        this.tribbleValue = tribbleValue;
    }

    public void setTribblePower(TribblePower tribblePower) {
        this.tribblePower = tribblePower;
    }

    public void setSiteNumber(int siteNumber) {
        this.siteNumber = siteNumber;
    }

    public void setPossessionClasses(Set<PossessionClass> possessionClasses) {
        this.possessionClasses = possessionClasses;
    }

    public void setDirection(Direction direction) {
    }

    // Implemented methods

    @Override
    public Side getSide() {
        return side;
    }

    @Override
    public CardType getCardType() {
        return cardType;
    }

    @Override
    public Culture getCulture() {
        return culture;
    }

    @Override
    public Race getRace() {
        return race;
    }

    @Override
    public boolean isUnique() {
        return this.uniqueness == Uniqueness.UNIQUE;
    }

    @Override
    public boolean isUniversal() { return this.uniqueness == Uniqueness.UNIVERSAL; }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSubtitle() {
        return subtitle;
    }
    @Override
    public String getImageUrl() { return imageUrl; }

    @Override
    public int getTwilightCost() {
        return cost;
    }

    @Override
    public int getStrength() {
        return strength;
    }

    @Override
    public int getVitality() {
        return vitality;
    }

    @Override
    public int getResistance() {
        return resistance;
    }

    public String getLocation() { return location; }

    @Override
    public TribblePower getTribblePower() {
        return tribblePower;
    }

    @Override
    public int getTribbleValue() {
        return tribbleValue;
    }

    @Override
    public Set<PossessionClass> getPossessionClasses() {
        return possessionClasses;
    }

    @Override
    public boolean hasKeyword(Keyword keyword) {
        return keywords != null && keywords.containsKey(keyword);
    }

    @Override
    public int getKeywordCount(Keyword keyword) {
        if (keywords == null)
            return 0;
        Integer count = keywords.get(keyword);
        return Objects.requireNonNullElse(count, 0);
    }

    public Filterable getValidTargetFilter() {
        if (targetFilters == null)
            return null;

        Filterable[] result = new Filterable[targetFilters.size()];
        for (int i = 0; i < result.length; i++) {
            final FilterableSource filterableSource = targetFilters.get(i);
            result[i] = filterableSource.getFilterable(null);
        }

        return Filters.and(result);
    }

    @Override
    public List<? extends Action> getPhaseActionsFromDiscard(String playerId, DefaultGame game, PhysicalCard self) {
        return getActivatedActions(playerId, game, self, inDiscardPhaseActions);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, DefaultGame game, PhysicalCard self) {
        List<ActivateCardAction> activatedActions = getActivatedActions(playerId, game, self, inPlayPhaseActions);
        if (copiedFilters != null) {
            if (activatedActions == null)
                activatedActions = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
                final PhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(activatedActions, firstActive.getBlueprint().getPhaseActionsInPlay(playerId, game, self));
            }
        }
        return activatedActions;
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsFromStacked(String playerId, DefaultGame game, PhysicalCard self) {
        return getActivatedActions(playerId, game, self, fromStackedPhaseActions);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(DefaultGame game, PhysicalCard self) {
        List<Modifier> modifiers = self.getModifiers(inPlayModifiers);
        if (copiedFilters != null) {
            if (modifiers == null)
                modifiers = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(self.getOwnerName(), game, self, null, null);
                final PhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null) {
                    addAllNotNull(modifiers, firstActive.getBlueprint().getInPlayModifiers(game, self));
                }
            }
        }
        return modifiers;
    }

    private <T> void addAllNotNull(List<T> list, List<? extends T> possiblyNullList) {
        if (possiblyNullList != null)
            list.addAll(possiblyNullList);
    }

    @Override
    public List<? extends Modifier> getStackedOnModifiers(PhysicalCard self) {
        return self.getModifiers(stackedOnModifiers);
    }

    @Override
    public List<? extends Modifier> getInDiscardModifiers(PhysicalCard self) {
        return self.getModifiers(inDiscardModifiers);
    }

    @Override
    public boolean playRequirementsNotMet(PhysicalCard self) {
        DefaultActionContext dummy = new DefaultActionContext(self.getOwnerName(), self.getGame(), self, null, null);

        if (requirements != null) {
            if (!RequirementUtils.acceptsAllRequirements(requirements, dummy)) return true;
        }

        return !(playEventAction == null || playEventAction.isValid(dummy));
    }

    @Override
    public int getTwilightCostModifier(DefaultGame game, PhysicalCard self, PhysicalCard target) {
        if (twilightCostModifiers == null)
            return 0;

        DefaultActionContext actionContext = new DefaultActionContext(self.getOwnerName(), game, self, null, null);

        int result = 0;
        for (TwilightCostModifierSource twilightCostModifier : twilightCostModifiers)
            result += twilightCostModifier.getTwilightCostModifier(actionContext, target);

        return result;
    }

    @Override
    public PlayEventAction getPlayEventCardAction(PhysicalCard self) {
        DefaultActionContext actionContext = new DefaultActionContext(self.getOwnerName(), self.getGame(), self,
                null, null);
        PlayEventAction action = new PlayEventAction(self, playEventAction.requiresRanger());
        playEventAction.createAction(action, actionContext);
        return action;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredBeforeTriggers(DefaultGame game, Effect effect, PhysicalCard self) {
        List<RequiredTriggerAction> result = null;

        if (requiredBeforeTriggers != null) {
            result = new LinkedList<>();
            for (ActionSource requiredBeforeTrigger : requiredBeforeTriggers) {
                DefaultActionContext actionContext = new DefaultActionContext(self.getOwnerName(), game, self, null, effect);
                if (requiredBeforeTrigger.isValid(actionContext)) {
                    RequiredTriggerAction action = new RequiredTriggerAction(self);
                    requiredBeforeTrigger.createAction(action, actionContext);
                    result.add(action);
                }
            }
        }

        if (copiedFilters != null) {
            if (result == null)
                result = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(self.getOwnerName(), game, self, null, effect);
                final PhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(result, firstActive.getBlueprint().getRequiredBeforeTriggers(game, effect, self));
            }
        }

        return result;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(DefaultGame game, EffectResult effectResult, PhysicalCard self) {
        List<RequiredTriggerAction> result = null;

        if (requiredAfterTriggers != null) {
            result = new LinkedList<>();
            for (ActionSource requiredAfterTrigger : requiredAfterTriggers) {
                DefaultActionContext actionContext = new DefaultActionContext(self.getOwnerName(), game, self, effectResult, null);
                if (requiredAfterTrigger.isValid(actionContext)) {
                    RequiredTriggerAction action = new RequiredTriggerAction(self);
                    requiredAfterTrigger.createAction(action, actionContext);
                    result.add(action);
                }
            }
        }

        if (copiedFilters != null) {
            if (result == null)
                result = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(self.getOwnerName(), game, self, effectResult, null);
                final PhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(result, firstActive.getBlueprint().getRequiredAfterTriggers(game, effectResult, self));
            }
        }

        return result;
    }

    @Override
    public List<OptionalTriggerAction> getOptionalBeforeTriggers(String playerId, DefaultGame game, Effect effect, PhysicalCard self) {
        List<OptionalTriggerAction> result = null;

        if (optionalBeforeTriggers != null) {
            result = new LinkedList<>();
            for (ActionSource optionalBeforeTrigger : optionalBeforeTriggers) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, effect);
                if (optionalBeforeTrigger.isValid(actionContext)) {
                    OptionalTriggerAction action = new OptionalTriggerAction(self);
                    optionalBeforeTrigger.createAction(action, actionContext);
                    result.add(action);
                }
            }
        }

        if (copiedFilters != null) {
            if (result == null)
                result = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, effect);
                final PhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(result, firstActive.getBlueprint().getOptionalBeforeTriggers(playerId, game, effect, self));
            }
        }

        return result;
    }

    public List<ActionSource> getOptionalAfterTriggers() {
        return optionalAfterTriggers;
    }



    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayBeforeActions(String playerId, DefaultGame game, Effect effect, PhysicalCard self) {
        List<ActivateCardAction> result = null;

        if (beforeActivatedTriggers != null) {
            result = new LinkedList<>();
            for (ActionSource beforeActivatedTrigger : beforeActivatedTriggers) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, effect);
                if (beforeActivatedTrigger.isValid(actionContext)) {
                    ActivateCardAction action = new ActivateCardAction(game, self);
                    beforeActivatedTrigger.createAction(action, actionContext);
                    result.add(action);
                }
            }
        }

        if (copiedFilters != null) {
            if (result == null)
                result = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, effect);
                final PhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(result, firstActive.getBlueprint().getOptionalInPlayBeforeActions(playerId, game, effect, self));
            }
        }

        return result;
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayAfterActions(String playerId, DefaultGame game, EffectResult effectResult, PhysicalCard self) {
        List<ActivateCardAction> result = null;

        if (afterActivatedTriggers != null) {
            result = new LinkedList<>();
            for (ActionSource afterActivatedTrigger : afterActivatedTriggers) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, effectResult, null);
                if (afterActivatedTrigger.isValid(actionContext)) {
                    ActivateCardAction action = new ActivateCardAction(game, self);
                    afterActivatedTrigger.createAction(action, actionContext);
                    result.add(action);
                }
            }
        }

        if (copiedFilters != null) {
            if (result == null)
                result = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, effectResult, null);
                final PhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(result, firstActive.getBlueprint().getOptionalInPlayAfterActions(playerId, game, effectResult, self));
            }
        }

        return result;
    }

    @Override
    public List<PlayEventAction> getPlayResponseEventBeforeActions(String playerId, DefaultGame game, Effect effect, PhysicalCard self) {
        if (optionalInHandBeforeActions == null)
            return null;

        List<PlayEventAction> result = new LinkedList<>();
        for (ActionSource optionalInHandBeforeAction : optionalInHandBeforeActions) {
            DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, effect);
            if (optionalInHandBeforeAction.isValid(actionContext)) {
                PlayEventAction action = new PlayEventAction(self);
                optionalInHandBeforeAction.createAction(action, actionContext);
                result.add(action);
            }
        }

        return result;
    }

    @Override
    public List<PlayEventAction> getPlayResponseEventAfterActions(String playerId, DefaultGame game, EffectResult effectResult, PhysicalCard self) {
        if (optionalInHandAfterActions == null)
            return null;

        List<PlayEventAction> result = new LinkedList<>();
        for (ActionSource optionalInHandAfterAction : optionalInHandAfterActions) {
            DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, effectResult, null);
            if (optionalInHandAfterAction.isValid(actionContext)) {
                PlayEventAction action = new PlayEventAction(self);
                optionalInHandAfterAction.createAction(action, actionContext);
                result.add(action);
            }
        }

        return result;
    }

    @Override
    public List<OptionalTriggerAction> getOptionalInHandAfterTriggers(String playerId, DefaultGame game, EffectResult effectResult, PhysicalCard self) {
        if (optionalInHandAfterTriggers == null)
            return null;

        List<OptionalTriggerAction> result = new LinkedList<>();
        for (ActionSource optionalInHandAfterTrigger : optionalInHandAfterTriggers) {
            DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, effectResult, null);
            if (optionalInHandAfterTrigger.isValid(actionContext)) {
                OptionalTriggerAction action = new OptionalTriggerAction(self);
                optionalInHandAfterTrigger.createAction(action, actionContext);
                result.add(action);
            }
        }

        return result;
    }

    @Override
    public List<? extends ExtraPlayCost> getExtraCostToPlay(DefaultGame game, PhysicalCard self) {
        if (extraPlayCosts == null)
            return null;

        DefaultActionContext actionContext = new DefaultActionContext(self.getOwnerName(), game, self, null, null);

        List<ExtraPlayCost> result = new LinkedList<>();
        for (ExtraPlayCostSource extraPlayCost : extraPlayCosts) {
            result.add(extraPlayCost.getExtraPlayCost(actionContext));
        }

        return result;
    }

    @Override
    public List<? extends Action> getPhaseActionsInHand(String playerId, PhysicalCard self) {
        if (playInOtherPhaseConditions == null)
            return null;
        DefaultActionContext actionContext = new DefaultActionContext(playerId, self.getGame(), self, null, null);
        List<Action> playCardActions = new LinkedList<>();

        if (self.getGame().checkPlayRequirements(self)) {
            for (Requirement playInOtherPhaseCondition : playInOtherPhaseConditions) {
                if (playInOtherPhaseCondition.accepts(actionContext))
                    playCardActions.add(self.getPlayCardAction(0, Filters.any, false));
            }
        }

        return playCardActions;
    }

    // Default implementations - not needed (for now)

    @Override
    public int getPotentialDiscount(PhysicalCard self) {
        if (discountSources == null)
            return 0;

        int result = 0;
        DefaultActionContext actionContext = new DefaultActionContext(self.getOwnerName(), self.getGame(), self,
                null, null);
        for (DiscountSource discountSource : discountSources)
            result += discountSource.getPotentialDiscount(actionContext);

        return result;
    }

    @Override
    public void appendPotentialDiscountEffects(DefaultGame game, CostToEffectAction action, String playerId, PhysicalCard self) {
        if (discountSources != null) {
            DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
            for (DiscountSource discountSource : discountSources) {
                final DiscountEffect discountEffect = discountSource.getDiscountEffect(action, actionContext);
                action.appendPotentialDiscount(discountEffect);
            }
        }
    }

    @Override
    public RequiredTriggerAction getDiscardedFromPlayRequiredTrigger(DefaultGame game, PhysicalCard self) {
        if (discardedFromPlayRequiredTriggerAction == null)
            return null;

        DefaultActionContext actionContext = new DefaultActionContext(self.getOwnerName(), game, self, null, null);
        if (discardedFromPlayRequiredTriggerAction.isValid(actionContext)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            discardedFromPlayRequiredTriggerAction.createAction(action, actionContext);
            return action;
        }
        return null;
    }

    @Override
    public OptionalTriggerAction getDiscardedFromPlayOptionalTrigger(String playerId, DefaultGame game, PhysicalCard self) {
        if (discardedFromPlayOptionalTriggerAction == null)
            return null;

        DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
        if (discardedFromPlayOptionalTriggerAction.isValid(actionContext)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            discardedFromPlayOptionalTriggerAction.createAction(action, actionContext);
            return action;
        }
        return null;
    }

    @Override
    public RequiredTriggerAction getKilledRequiredTrigger(DefaultGame game, PhysicalCard self) {
        if (killedRequiredTriggerAction == null)
            return null;

        DefaultActionContext actionContext = new DefaultActionContext(self.getOwnerName(), game, self, null, null);
        if (killedRequiredTriggerAction.isValid(actionContext)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            killedRequiredTriggerAction.createAction(action, actionContext);
            return action;
        }
        return null;
    }

    @Override
    public OptionalTriggerAction getKilledOptionalTrigger(String playerId, DefaultGame game, PhysicalCard self) {
        if (killedOptionalTriggerAction == null)
            return null;

        DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
        if (killedOptionalTriggerAction.isValid(actionContext)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            killedOptionalTriggerAction.createAction(action, actionContext);
            return action;
        }
        return null;
    }

    @Override
    public String getDisplayableInformation(PhysicalCard self) {
        return null;
    }

    // Helper methods

    private List<ActivateCardAction> getActivatedActions(String playerId, DefaultGame game, PhysicalCard self, List<ActionSource> sources) {
        if (sources == null)
            return null;

        List<ActivateCardAction> result = new LinkedList<>();
        for (ActionSource inPlayPhaseAction : sources) {
            DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
            if (inPlayPhaseAction.isValid(actionContext)) {
                ActivateCardAction action = new ActivateCardAction(game, self);
                inPlayPhaseAction.createAction(action, actionContext);
                result.add(action);
            }
        }
        return result;
    }

    public void throwException(String message) throws InvalidCardDefinitionException {
        throw new InvalidCardDefinitionException(message);
    }

    public void validateConsistency() throws InvalidCardDefinitionException {
        if (title == null) throwException("Card has to have a title");
        if (cardType == null) throwException("Card has to have a type");
        if (cardType == CardType.MISSION) {
            if (_propertyLogo != null) throwException("Mission card should not have a property logo");
            if (location == null && !title.equals("Space")) throwException("Mission card should have a location");
        } else if (cardType == CardType.TRIBBLE) {
            if (tribblePower == null) throwException("Tribble card has to have a Tribble power");
            if (!Arrays.asList(1, 10, 100, 1000, 10000, 100000).contains(tribbleValue))
                throwException("Tribble card does not have a valid Tribble value");
        } else if (_propertyLogo == null)
            // TODO - Technically tribbles should have property logos too, they're just never relevant
            throwException("Non-mission card has to have a property logo");

            // Checks below are LotR-specific
        if (cardType != CardType.EVENT && playEventAction != null)
            throwException("Only events should have an event type effect");
        if (targetFilters != null && keywords != null) {
            if (keywords.size() > 1 && keywords.containsKey(Keyword.TALE))
                throwException("Attachment should not have keywords");
        }
    }

    public List<FilterableSource> getCopiedFilters() {
        return copiedFilters;
    }

    public boolean canPlayOutOfSequence(TribblesGame game, PhysicalCard self) {
        if (playOutOfSequenceConditions == null) return false;
        TribblesActionContext actionContext =
                new TribblesActionContext(self.getOwnerName(), game, self, null, null);
        return playOutOfSequenceConditions.stream().anyMatch(requirement -> requirement.accepts(actionContext));
    }

    public void setSeedPhase(List<Phase> seedPhases) { this.seedPhases = seedPhases; }
    public List<Phase> getSeedPhases() { return this.seedPhases; }
    public void setCanInsertIntoSpaceline(boolean canInsert) { _canInsertIntoSpaceline = canInsert; }
    public boolean canInsertIntoSpaceline() { return _canInsertIntoSpaceline; }
    public void setAnyCrewOrAwayTeamCanAttempt(boolean canAttempt) { _anyCrewOrAwayTeamCanAttempt = canAttempt; }
    public void addOwnerAffiliationIcon(Affiliation affiliation) { _ownerAffiliationIcons.add(affiliation); }
    public Set<Affiliation> getOwnerAffiliationIcons() { return _ownerAffiliationIcons; }
    public Set<Affiliation> getOpponentAffiliationIcons() { return _opponentAffiliationIcons; }
    public void setMissionType(MissionType type) { _missionType = type; }
    public MissionType getMissionType() { return _missionType; }
    public Affiliation homeworldAffiliation() {
        if (this.cardType != CardType.MISSION)
            return null;
        for (Affiliation affiliation : Affiliation.values()) {
            String homeworldString = affiliation.name().toLowerCase() + " homeworld";
            if (_lore != null)
                if (_lore.toLowerCase().contains(homeworldString))
                    return affiliation;
        }
        return null;
    }

    public boolean isHomeworld() { return homeworldAffiliation() != null; }
    public void setLore(String lore) { _lore = lore; }
    public void setPropertyLogo(PropertyLogo propertyLogo) { _propertyLogo = propertyLogo; }

    public String getFullName() {
        if (getSubtitle() != null)
            return getTitle() + ", " + getSubtitle();
        else
            return getTitle();
    }

    public String getCardLink(String blueprintId) {
        return "<div class='cardHint' value='" + blueprintId + "'>" +
                (isUnique() ? "·" : "") + getFullName() + "</div>";
    }
}
