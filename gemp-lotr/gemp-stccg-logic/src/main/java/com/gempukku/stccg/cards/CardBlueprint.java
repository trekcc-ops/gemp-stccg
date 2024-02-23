package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.sources.ActionSource;
import com.gempukku.stccg.actions.sources.TriggerActionSource;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.requirement.Requirement;

import java.util.*;

public class CardBlueprint {
    private String _blueprintId;
    private String title;
    private String subtitle;
    private CardType _cardType;
    private String imageUrl;
    private PropertyLogo _propertyLogo;
    private String _lore;
    private Uniqueness uniqueness = null;
    private Quadrant quadrant;
    private String location;
    private int _pointsShown;
    private boolean _hasPointBox;
    private Side side;
    private final Set<Affiliation> _affiliations = new HashSet<>();
    private Region region;
    private boolean _canInsertIntoSpaceline;
    private final Set<Affiliation> _ownerAffiliationIcons = new HashSet<>();
    private final Set<Affiliation> _opponentAffiliationIcons = new HashSet<>();
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
    private String _missionRequirements;
    private TribblePower tribblePower;
    private Set<PossessionClass> possessionClasses;
    private List<Phase> seedPhases;

    private List<Requirement> _playRequirements;
    private List<FilterableSource> targetFilters;
    private final Map<RequiredType, List<ActionSource>> _beforeTriggers = new HashMap<>();
    private final Map<RequiredType, List<ActionSource>> _afterTriggers = new HashMap<>();
    private final Map<RequiredType, ActionSource> _discardedFromPlayTriggers = new HashMap<>();
    private final Map<TriggerTiming, List<ActionSource>> _optionalInHandTriggers = new HashMap<>();
    private final Map<TriggerTiming, List<ActionSource>> _activatedTriggers = new HashMap<>();
    private final Map<RequiredType, ActionSource> _killedTriggers = new HashMap<>();

    private List<ActionSource> inPlayPhaseActions;
    private List<ActionSource> inDiscardPhaseActions;

    private List<ModifierSource> inPlayModifiers;

    private List<TwilightCostModifierSource> _twilightCostModifierSources;

    private List<ExtraPlayCostSource> extraPlayCosts;
    private List<DiscountSource> _discountSources;

    private List<Requirement> playInOtherPhaseConditions;
    private List<Requirement> playOutOfSequenceConditions;

    private ActionSource _playEventAction;

    public CardBlueprint(String blueprintId) {
        _blueprintId = blueprintId;
        for (RequiredType requiredType : RequiredType.values()) {
            _beforeTriggers.put(requiredType, new LinkedList<>());
            _afterTriggers.put(requiredType, new LinkedList<>());
        }
    }

    // Setter/getter methods for card features
    public String getBlueprintId() { return _blueprintId; }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() { return title; }
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    public String getSubtitle() { return subtitle; }
    public void setCardType(CardType cardType) {
        this._cardType = cardType;
    }
    public CardType getCardType() { return _cardType; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getImageUrl() { return imageUrl; }
    public void setPropertyLogo(PropertyLogo propertyLogo) { _propertyLogo = propertyLogo; }
    public void setLore(String lore) { _lore = lore; }
    public String getLore() { return _lore; }
    public void setUniqueness(Uniqueness uniqueness) { this.uniqueness = uniqueness;}
    public Uniqueness getUniqueness() { return this.uniqueness;}
    public boolean isUnique() { return this.uniqueness == Uniqueness.UNIQUE; }
    public boolean isUniversal() { return this.uniqueness == Uniqueness.UNIVERSAL; }
    public void setQuadrant(Quadrant quadrant) {
        this.quadrant = quadrant;
    }
    public Quadrant getQuadrant() { return this.quadrant; }

    // Missions
    public void setMissionType(MissionType type) { _missionType = type; }
    public MissionType getMissionType() { return _missionType; }
    public void setRegion(Region region) { this.region = region; }
    public Region getRegion() { return this.region; }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getLocation() { return location; }
    public void setMissionRequirements(String requirements) { _missionRequirements = requirements;}
    public String getMissionRequirements() { return _missionRequirements; }
    public void setPointsShown(int pointsShown) { _pointsShown = pointsShown; }
    public int getPointsShown() { return _pointsShown; }
    public void setHasPointBox(boolean hasPointBox) { _hasPointBox = hasPointBox; }
    public boolean hasNoPointBox() { return !_hasPointBox; }
    public void addOwnerAffiliationIcon(Affiliation affiliation) { _ownerAffiliationIcons.add(affiliation); }
    public Set<Affiliation> getOwnerAffiliationIcons() { return _ownerAffiliationIcons; }
    public Set<Affiliation> getOpponentAffiliationIcons() { return _opponentAffiliationIcons; }

    // Noun cards
    public void setFacilityType(FacilityType facilityType) { _facilityType = facilityType; }
    public FacilityType getFacilityType() { return _facilityType; }
    public void addAffiliation(Affiliation affiliation) { _affiliations.add(affiliation); }
    public Set<Affiliation> getAffiliations() { return _affiliations; }
    public void setStrength(int strength) {
        this.strength = strength;
    }
    public int getStrength() { return strength; }

    // Tribbles
    public void setTribbleValue(int tribbleValue) {
        this.tribbleValue = tribbleValue;
    }
    public int getTribbleValue() { return tribbleValue; }
    public void setTribblePower(TribblePower tribblePower) {
        this.tribblePower = tribblePower;
    }
    public TribblePower getTribblePower() { return tribblePower; }

    // LotR
    public void setCost(int cost) { this.cost = cost; }
    public int getTwilightCost() { return cost; }
    public void setSide(Side side) {
        this.side = side;
    }
    public Side getSide() {
        return side;
    }
    public void setCulture(Culture culture) {
        this.culture = culture;
    }
    public Culture getCulture() { return culture; }
    public void setRace(Race race) {
        this.race = race;
    }
    public Race getRace() { return race; }
    public void setVitality(int vitality) {
        this.vitality = vitality;
    }
    public int getVitality() { return vitality; }
    public void setResistance(int resistance) {
        this.resistance = resistance;
    }
    public int getResistance() { return resistance; }
    public void setKeywords(Map<Keyword, Integer> keywords) {
        this.keywords = keywords;
    }
    public boolean hasKeyword(Keyword keyword) { return keywords != null && keywords.containsKey(keyword); }
    public int getKeywordCount(Keyword keyword) {
        if (keywords == null)
            return 0;
        Integer count = keywords.get(keyword);
        return Objects.requireNonNullElse(count, 0);
    }
    public void setPossessionClasses(Set<PossessionClass> possessionClasses) { this.possessionClasses = possessionClasses; }
    public Set<PossessionClass> getPossessionClasses() { return possessionClasses; }


    // Gametext features
    public void setSeedPhase(List<Phase> seedPhases) { this.seedPhases = seedPhases; }
    public List<Phase> getSeedPhases() { return this.seedPhases; }
    public void setCanInsertIntoSpaceline(boolean canInsert) { _canInsertIntoSpaceline = canInsert; }
    public boolean canInsertIntoSpaceline() { return _canInsertIntoSpaceline; }
    public void setAnyCrewOrAwayTeamCanAttempt(boolean canAttempt) { }
    public Affiliation homeworldAffiliation() {
        if (this._cardType != CardType.MISSION)
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


    // Building methods

    public void appendPlayInOtherPhaseCondition(Requirement requirement) {
        if (playInOtherPhaseConditions == null)
            playInOtherPhaseConditions = new LinkedList<>();
        playInOtherPhaseConditions.add(requirement);
    }

    public void appendPlayOutOfSequenceCondition(Requirement requirement) {
        if (playOutOfSequenceConditions == null)
            playOutOfSequenceConditions = new LinkedList<>();
        playOutOfSequenceConditions.add(requirement);
    }

    public void appendDiscountSource(DiscountSource discountSource) {
        if (_discountSources == null)
            _discountSources = new LinkedList<>();
        _discountSources.add(discountSource);
    }

    public void appendOptionalInHandTrigger(ActionSource actionSource, TriggerTiming timing) {
        _optionalInHandTriggers.computeIfAbsent(timing, k -> new LinkedList<>());
        _optionalInHandTriggers.get(timing).add(actionSource);
    }

    public void appendExtraPlayCost(ExtraPlayCostSource extraPlayCostSource) {
        if (extraPlayCosts == null)
            extraPlayCosts = new LinkedList<>();
        extraPlayCosts.add(extraPlayCostSource);
    }

    public void appendActivatedTrigger(ActionSource actionSource, TriggerTiming timing) {
        _activatedTriggers.computeIfAbsent(timing, k -> new LinkedList<>());
        _activatedTriggers.get(timing).add(actionSource);
    }

    public void appendBeforeOrAfterTrigger(TriggerActionSource actionSource) {
        RequiredType requiredType = actionSource.getRequiredType();
        TriggerTiming triggerTiming = actionSource.getTiming();
        Map<RequiredType, List<ActionSource>> triggersMap = null;
        if (triggerTiming == TriggerTiming.BEFORE)
            triggersMap = _beforeTriggers;
        else if (triggerTiming == TriggerTiming.AFTER)
            triggersMap = _afterTriggers;
        if (triggersMap != null){
            triggersMap.computeIfAbsent(requiredType, k -> new LinkedList<>());
            triggersMap.get(requiredType).add(actionSource);
        }
    }

    public void appendPlayRequirement(Requirement requirement) {
        if (_playRequirements == null)
            _playRequirements = new LinkedList<>();
        _playRequirements.add(requirement);
    }

    public void appendInPlayModifier(ModifierSource modifierSource) {
        if (inPlayModifiers == null)
            inPlayModifiers = new LinkedList<>();
        inPlayModifiers.add(modifierSource);
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

    public void appendTwilightCostModifier(TwilightCostModifierSource twilightCostModifierSource) {
        if (_twilightCostModifierSources == null)
            _twilightCostModifierSources = new LinkedList<>();
        _twilightCostModifierSources.add(twilightCostModifierSource);
    }

    public void setPlayEventAction(ActionSource playEventAction) { _playEventAction = playEventAction; }
    public ActionSource getPlayEventAction() { return _playEventAction; }
    public void setKilledTrigger(RequiredType requiredType, ActionSource actionSource) { _killedTriggers.put(requiredType, actionSource); }
    public void setDiscardedFromPlayTrigger(RequiredType requiredType, ActionSource actionSource) { _discardedFromPlayTriggers.put(requiredType, actionSource); }
    public ActionSource getDiscardedFromPlayTrigger(RequiredType requiredType) { return _discardedFromPlayTriggers.get(requiredType); }
    public List<Requirement> getPlayRequirements() { return _playRequirements; }
    public List<ActionSource> getOptionalInHandTriggers(TriggerTiming timing) { return _optionalInHandTriggers.get(timing); }
    public List<ExtraPlayCostSource> getExtraPlayCosts() { return extraPlayCosts; }
    public List<Requirement> getPlayInOtherPhaseConditions() { return playInOtherPhaseConditions; }
    public List<DiscountSource> getDiscountSources() { return _discountSources; }
    public List<ActionSource> getInDiscardPhaseActions() { return inDiscardPhaseActions; }
    public List<ActionSource> getActivatedTriggers(TriggerTiming timing) { return _activatedTriggers.get(timing); }
    public List<Requirement> getPlayOutOfSequenceConditions() { return playOutOfSequenceConditions; }

    
    public int getTwilightCostModifier(PhysicalCard self, PhysicalCard target) {
        if (_twilightCostModifierSources == null)
            return 0;
        int result = 0;
        for (TwilightCostModifierSource twilightCostModifier : _twilightCostModifierSources)
            result += twilightCostModifier.getTwilightCostModifier(self.createActionContext(), target);
        return result;
    }


    public List<ActionSource> getBeforeOrAfterTriggers(RequiredType requiredType, TriggerTiming timing) {
        if (timing == TriggerTiming.BEFORE)
            return _beforeTriggers.get(requiredType);
        else if (timing == TriggerTiming.AFTER)
            return _afterTriggers.get(requiredType);
        else return null;
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


    // Helper methods



    public void throwException(String message) throws InvalidCardDefinitionException {
        throw new InvalidCardDefinitionException(message);
    }

    public void validateConsistency() throws InvalidCardDefinitionException {
        if (title == null) throwException("Card has to have a title");
        if (_cardType == null) throwException("Card has to have a type");
        if (_cardType == CardType.MISSION) {
            if (_propertyLogo != null) throwException("Mission card should not have a property logo");
            if (location == null && !title.equals("Space")) throwException("Mission card should have a location");
        } else if (_cardType == CardType.TRIBBLE) {
            if (tribblePower == null) throwException("Tribble card has to have a Tribble power");
            if (!Arrays.asList(1, 10, 100, 1000, 10000, 100000).contains(tribbleValue))
                throwException("Tribble card does not have a valid Tribble value");
        } else if (_propertyLogo == null)
            // TODO - Technically tribbles should have property logos too, they're just never relevant
            throwException("Non-mission card has to have a property logo");

        // Checks below are LotR-specific
        if (_cardType != CardType.EVENT && _playEventAction != null)
            throwException("Only events should have an event type effect");
        if (targetFilters != null && keywords != null) {
            if (keywords.size() > 1 && keywords.containsKey(Keyword.TALE))
                throwException("Attachment should not have keywords");
        }
    }



    public String getFullName() {
        if (getSubtitle() != null)
            return getTitle() + ", " + getSubtitle();
        else
            return getTitle();
    }

    public String getCardLink() {
        List<CardType> typesWithUniversalSymbol =
                Arrays.asList(CardType.MISSION, CardType.SHIP, CardType.PERSONNEL, CardType.SITE);
        boolean showUniversalSymbol = typesWithUniversalSymbol.contains(getCardType()) && isUniversal();
        return "<div class='cardHint' value='" + _blueprintId + "' + card_img_url='" + getImageUrl() + "'>" +
                (showUniversalSymbol ? "&#x2756 " : "") + " " + getFullName() + "</div>";
    }

    public boolean hasNoTransporters() {
        // TODO - No actual code built here for cards that don't have transporters
        return false;
    }

    public List<ActionSource> getInPlayPhaseActions() { return inPlayPhaseActions; }
    public List<ModifierSource> getInPlayModifiers() { return inPlayModifiers; }

    public PhysicalCard createPhysicalCard(DefaultGame game, int cardId, Player player) {
        if (game instanceof ST1EGame) {
            if (_cardType == CardType.MISSION)
                return new PhysicalMissionCard((ST1EGame) game, cardId, player, this);
            else if (_cardType == CardType.FACILITY)
                return new PhysicalFacilityCard((ST1EGame) game, cardId, player, this);
            else if (_cardType == CardType.PERSONNEL)
                return new PhysicalPersonnelCard((ST1EGame) game, cardId, player, this);
            else if (_cardType == CardType.SHIP)
                return new PhysicalShipCard((ST1EGame) game, cardId, player, this);
            else return new ST1EPhysicalCard((ST1EGame) game, cardId, player, this);
        } else if (game instanceof TribblesGame) {
            return new TribblesPhysicalCard((TribblesGame) game, cardId, player, this);
        } else return new PhysicalCardGeneric(game, cardId, player, this);
    }
}
