package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.turn.RequiredTriggerAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.actionsource.ActionSource;
import com.gempukku.stccg.cards.blueprints.actionsource.RequiredTriggerActionSource;
import com.gempukku.stccg.cards.blueprints.actionsource.TriggerActionSource;
import com.gempukku.stccg.cards.blueprints.effect.ModifierSource;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.modifiers.Modifier;

import java.util.*;

public class CardBlueprint {
    private final String _blueprintId;
    private GameType _gameType;
    private String _baseBlueprintId;
    private String title;
    private String subtitle;
    private ShipClass _shipClass;
    private boolean _anyCanAttempt;
    private boolean _anyExceptBorgCanAttempt;
    protected CardType _cardType;
    private String imageUrl;
    private String _rarity;
    private PropertyLogo _propertyLogo;
    private String _persona;
    private String _lore;
    private List<Species> _species;
    private final Set<Characteristic> _characteristics = new HashSet<>();
    private Uniqueness uniqueness = null;
    private List<CardIcon> _icons;
    private Quadrant quadrant;
    private String location;
    private int _pointsShown;
    int _skillDots;
    private boolean _hasPointBox;
    private MissionRequirement _missionRequirements;
    final List<Skill> _skills = new LinkedList<>();
    private final Set<Affiliation> _affiliations = new HashSet<>();
    private Region region;
    private SkillName _classification;
    private boolean _canInsertIntoSpaceline;
    private final List<Keyword> _keywords = new LinkedList<>();
    private final Set<ShipSpecialEquipment> _specialEquipment = new HashSet<>();
    private final Set<Affiliation> _ownerAffiliationIcons = new HashSet<>();
    private final Set<Affiliation> _opponentAffiliationIcons = new HashSet<>();
    private int _span;
    private Integer _opponentSpan;
    private MissionType _missionType;
    private FacilityType _facilityType;
    private int cost = -1;
    private final Map<CardAttribute, Integer> _cardAttributes = new HashMap<>();
    private int _specialDownloadIcons;
    private int tribbleValue;
    private List<CardIcon> _staffing = new LinkedList<>();
    private String _missionRequirementsText;
    private TribblePower tribblePower;
    private List<Requirement> _seedRequirements;
    private List<Requirement> _playRequirements;
    private List<FilterableSource> targetFilters;
    private final Map<Affiliation, String> _imageOptions = new HashMap<>();
    private final Map<RequiredType, List<ActionSource>> _beforeTriggers = new HashMap<>();
    private final Map<RequiredType, List<ActionSource>> _afterTriggers = new HashMap<>();
    private final Map<RequiredType, ActionSource> _discardedFromPlayTriggers = new HashMap<>();
    private final Map<TriggerTiming, List<ActionSource>> _optionalInHandTriggers = new HashMap<>();
    private final Map<TriggerTiming, List<ActionSource>> _activatedTriggers = new HashMap<>();

    private final List<ActionSource> inPlayPhaseActions = new LinkedList<>();
    private List<ActionSource> inDiscardPhaseActions;

    private final List<ModifierSource> inPlayModifiers = new LinkedList<>();

    private List<ExtraPlayCostSource> extraPlayCosts;
    private List<Requirement> playInOtherPhaseConditions;
    private List<Requirement> playOutOfSequenceConditions;
    private ActionSource _seedCardActionSource;

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
    public void setRarity(String rarity) { _rarity = rarity; }
    public String getRarity() { return _rarity; }
    public void addImageOption(Affiliation affiliation, String imageUrl) { _imageOptions.put(affiliation, imageUrl); }
    public String getAffiliationImageUrl(Affiliation affiliation) { return _imageOptions.get(affiliation); }
    public void setPropertyLogo(PropertyLogo propertyLogo) { _propertyLogo = propertyLogo; }
    public PropertyLogo getPropertyLogo() { return _propertyLogo; }
    public void setLore(String lore) { _lore = lore; }
    public String getLore() { return _lore; }
    public void setUniqueness(Uniqueness uniqueness) { this.uniqueness = uniqueness;}
    public Uniqueness getUniqueness() { return this.uniqueness;}
    public boolean isUnique() { return this.uniqueness == Uniqueness.UNIQUE; }
    public boolean isUniversal() { return this.uniqueness == Uniqueness.UNIVERSAL; }
    public void setIcons(List<CardIcon> icons) { _icons = icons; }
    public void addIcons(CardIcon... icons) {
        if (_icons == null) {
            _icons = new LinkedList<>();
        }
        Collections.addAll(_icons, icons);
    }
    public List<CardIcon> getIcons() {
        return Objects.requireNonNullElseGet(_icons, LinkedList::new);
    }
    public boolean hasIcon(CardIcon icon) { return _icons != null && _icons.contains(icon); }
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
    public void setMissionRequirements(MissionRequirement requirement) { _missionRequirements = requirement; }
    public MissionRequirement getMissionRequirements() { return _missionRequirements; }
    public void setMissionRequirementsText(String requirements) { _missionRequirementsText = requirements;}
    public String getMissionRequirementsText() { return _missionRequirementsText; }
    public void setPointsShown(int pointsShown) { _pointsShown = pointsShown; }
    public int getPointsShown() { return _pointsShown; }
    public void setHasPointBox(boolean hasPointBox) { _hasPointBox = hasPointBox; }
    public boolean hasNoPointBox() { return !_hasPointBox; }
    public void addOwnerAffiliationIcon(Affiliation affiliation) { _ownerAffiliationIcons.add(affiliation); }
    public Set<Affiliation> getOwnerAffiliationIcons() { return _ownerAffiliationIcons; }
    public Set<Affiliation> getOpponentAffiliationIcons() { return _opponentAffiliationIcons; }
    public void setSpan(int span) { _span = span; }
    public void setOpponentSpan(int span) { _opponentSpan = span; }
    public int getOwnerSpan() { return _span; }
    public int getOpponentSpan() {
        if (_opponentSpan == null)
            return _span;
        else return _opponentSpan;
    }

    // Noun cards
    public void setFacilityType(FacilityType facilityType) { _facilityType = facilityType; }
    public FacilityType getFacilityType() { return _facilityType; }
    public void addAffiliation(Affiliation affiliation) { _affiliations.add(affiliation); }
    public Set<Affiliation> getAffiliations() { return _affiliations; }
    public void setAttribute(CardAttribute attribute, int attributeValue) {
        _cardAttributes.put(attribute, attributeValue);
    }

    public int getIntegrity() { return _cardAttributes.get(CardAttribute.INTEGRITY);
    }

    public int getCunning() { return _cardAttributes.get(CardAttribute.CUNNING); }

    public int getStrength() { return _cardAttributes.get(CardAttribute.STRENGTH); }
    public int getRange() {
        Integer range = _cardAttributes.get(CardAttribute.RANGE);
        return (range == null) ? 0 : range;
    }

    public int getWeapons() {
        Integer weapons = _cardAttributes.get(CardAttribute.WEAPONS);
        return (weapons == null) ? 0 : weapons;
    }

    public int getShields() {
        Integer shields = _cardAttributes.get(CardAttribute.SHIELDS);
        return (shields == null) ? 0 : shields;
    }


    public void setStaffing(List<CardIcon> staffing) { _staffing = staffing; }
    public List<CardIcon> getStaffing() { return _staffing; }
    public void setClassification(SkillName classification) { _classification = classification; }
    public SkillName getClassification() { return _classification; }
    public void addSkill(Skill skill) { _skills.add(skill); }
    public void addSkill(SkillName skillName) { _skills.add(new RegularSkill(skillName, 1)); }
    public void addSkill(SkillName skillName, int level) { _skills.add(new RegularSkill(skillName, level)); }
        // TODO - Not an exact match for how skills are processed
    public List<RegularSkill> getRegularSkills() {
        List<RegularSkill> result = new LinkedList<>();
        for (Skill skill : _skills) {
            if (skill instanceof RegularSkill regularSkill)
                result.add(regularSkill);
        }
        return result;
    }
    public void setSkillDotIcons(int dots) { _skillDots = dots; }
    public int getSkillDotCount() { return _skillDots; }
    public int getSpecialDownloadIconCount() { return _specialDownloadIcons; }
    public void setSpecialDownloadIcons(int icons) { _specialDownloadIcons = icons; }
    public void setSpecies(List<Species> species) { _species = species; }

    public boolean isSpecies(Species species) {
        if (_species == null)
            return false;
        else
            return _species.contains(species);
    }

    // Tribbles
    public void setTribbleValue(int tribbleValue) { this.tribbleValue = tribbleValue; }
    public int getTribbleValue() { return tribbleValue; }
    public void setTribblePower(TribblePower tribblePower) {
        this.tribblePower = tribblePower;
    }
    public TribblePower getTribblePower() { return tribblePower; }

    // LotR
    public void setCost(int cost) { this.cost = cost; }
    public int getCost() { return this.cost; }


    public boolean canInsertIntoSpaceline() { return _canInsertIntoSpaceline; }
    public boolean canAnyAttempt() { return _anyCanAttempt; }
    public void setAnyCrewOrAwayTeamCanAttempt() { _anyCanAttempt = true; }
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
    public void setSeedCardActionSource(ActionSource actionSource) { _seedCardActionSource = actionSource; }
    public ActionSource getSeedCardActionSource() { return _seedCardActionSource; }

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

    public void appendSeedRequirement(Requirement requirement) {
        if (_seedRequirements == null)
            _seedRequirements = new LinkedList<>();
        _seedRequirements.add(requirement);
    }

    public void appendInPlayModifier(ModifierSource modifierSource) {
        inPlayModifiers.add(modifierSource);
    }

    public void appendTargetFilter(FilterableSource targetFilter) {
        if (targetFilters == null)
            targetFilters = new LinkedList<>();
        targetFilters.add(targetFilter);
    }

    public void appendInPlayPhaseAction(ActionSource actionSource) {
        inPlayPhaseActions.add(actionSource);
    }

    public void appendInDiscardPhaseAction(ActionSource actionSource) {
        if (inDiscardPhaseActions == null)
            inDiscardPhaseActions = new LinkedList<>();
        inDiscardPhaseActions.add(actionSource);
    }

    public void setDiscardedFromPlayTrigger(RequiredType requiredType, ActionSource actionSource) {
        _discardedFromPlayTriggers.put(requiredType, actionSource);
    }
    public ActionSource getDiscardedFromPlayTrigger(RequiredType requiredType) {
        return _discardedFromPlayTriggers.get(requiredType);
    }
    public List<Requirement> getSeedRequirements() { return _seedRequirements; }
    public List<Requirement> getPlayRequirements() { return _playRequirements; }
    public List<ActionSource> getOptionalInHandTriggers(TriggerTiming timing) {
        return _optionalInHandTriggers.get(timing);
    }
    public List<ExtraPlayCostSource> getExtraPlayCosts() { return extraPlayCosts; }
    public List<Requirement> getPlayInOtherPhaseConditions() { return playInOtherPhaseConditions; }
    public List<ActionSource> getInDiscardPhaseActions() { return inDiscardPhaseActions; }
    public List<ActionSource> getActivatedTriggers(TriggerTiming timing) { return _activatedTriggers.get(timing); }
    public List<Requirement> getPlayOutOfSequenceConditions() { return playOutOfSequenceConditions; }


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


    public String getFullName() {
        if (getSubtitle() != null)
            return getTitle() + ", " + getSubtitle();
        else
            return getTitle();
    }

    public String getCardLink() {
        List<CardType> typesWithUniversalSymbol =
                Arrays.asList(CardType.MISSION, CardType.SHIP, CardType.PERSONNEL, CardType.SITE);
        boolean showUniversalSymbol = typesWithUniversalSymbol.contains(_cardType) && isUniversal();
        return "<div class='cardHint' value='" + _blueprintId + "' + card_img_url='" + imageUrl + "'>" +
                (showUniversalSymbol ? "&#x2756&nbsp;" : "") + getFullName() + "</div>";
    }

    public PhysicalCard createPhysicalCard(ST1EGame st1egame, int cardId, Player player) {
        return switch(_cardType) {
            case EQUIPMENT -> new PhysicalReportableCard1E(st1egame, cardId, player, this);
            case FACILITY -> new FacilityCard(st1egame, cardId, player, this);
            case MISSION -> new MissionCard(st1egame, cardId, player, this);
            case PERSONNEL -> new PersonnelCard(st1egame, cardId, player, this);
            case SHIP -> new PhysicalShipCard(st1egame, cardId, player, this);
            default -> new ST1EPhysicalCard(st1egame, cardId, player, this);
        };
    }

    protected List<Modifier> getGameTextWhileActiveInPlayModifiersFromJava(PhysicalCard thisCard)
            throws InvalidGameLogicException {
        return new LinkedList<>();
    }

    public List<Modifier> getGameTextWhileActiveInPlayModifiers(PhysicalCard card) {
        List<Modifier> result = new LinkedList<>();

        // Add in-play modifiers created through JSON definitions
        for (ModifierSource modifierSource : inPlayModifiers) {
            ActionContext context =
                    new DefaultActionContext(card.getOwnerName(), card.getGame(), card, null);
            result.add(modifierSource.getModifier(context));
        }

        // Add in-play modifiers created through Java definitions
        try {
            result.addAll(getGameTextWhileActiveInPlayModifiersFromJava(card));
        } catch(InvalidGameLogicException exp) {
            card.getGame().sendErrorMessage(exp);
        }

        return result;
    }

    public boolean hasCharacteristic(Characteristic characteristic) {
        return _characteristics.contains(characteristic);
    }

    public void addCharacteristic(Characteristic characteristic) {
        _characteristics.add(characteristic);
    }

    public List<TopLevelSelectableAction> getRequiredAfterTriggerActions(ActionResult actionResult, PhysicalCard card) {
        List<TopLevelSelectableAction> result = new LinkedList<>();
        getBeforeOrAfterTriggers(RequiredType.REQUIRED, TriggerTiming.AFTER).forEach(actionSource -> {
            if (actionSource instanceof RequiredTriggerActionSource triggerSource) {
                RequiredTriggerAction action = triggerSource.createActionWithNewContext(card, actionResult);
                if (action != null) result.add(action);
            }
        });
        return result;
    }

    public List<Skill> getSkills(DefaultGame game, PhysicalCard thisCard) {
        return _skills;
    }

    public void setPersona(String persona) { _persona = persona; }

    public String getPersona() {
        if (_persona == null)
            return title;
        else return _persona;
    }

    public boolean doesNotWorkWithPerRestrictionBox(PhysicalNounCard1E thisCard, PhysicalNounCard1E otherCard) {
        return false;
    }

    public String getBaseBlueprintId() { return _baseBlueprintId; }
    public void setBaseBlueprintId(String baseBlueprintId) { _baseBlueprintId = baseBlueprintId; }

    public List<Action> getEncounterActions(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                            EncounterSeedCardAction action,
                                            MissionLocation missionLocation) throws PlayerNotFoundException {
        return new LinkedList<>();
    }

    public void setShipClass(ShipClass shipClass) {
        _shipClass = shipClass;
    }

    public void addSpecialEquipment(Collection<ShipSpecialEquipment> specialEquipment) {
        _specialEquipment.addAll(specialEquipment);
    }

    public List<TopLevelSelectableAction> getActionsFromActionSources(String playerId, PhysicalCard card,
                                                    ActionResult actionResult, List<ActionSource> actionSources) {
        List<TopLevelSelectableAction> result = new LinkedList<>();
        actionSources.forEach(actionSource -> {
            if (actionSource != null) {
                TopLevelSelectableAction action = actionSource.createActionWithNewContext(card, playerId, actionResult);
                if (action != null) result.add(action);
            }
        });
        return result;
    }


    public List<TopLevelSelectableAction> getGameTextActionsWhileInPlay(Player player, PhysicalCard thisCard,
                                                                        DefaultGame cardGame) {
        return getActionsFromActionSources(
                player.getPlayerId(), thisCard, null, inPlayPhaseActions);
    }


    public void setAnyExceptBorgCanAttempt() {
        _anyExceptBorgCanAttempt = true;
    }

    public boolean canAnyExceptBorgAttempt() { return _anyExceptBorgCanAttempt; }

    public void setKeywords(Collection<Keyword> keywords) {
        _keywords.addAll(keywords);
    }

    public void setGameType(GameType gameType) {
        _gameType = gameType;
    }

    public GameType getGameType() { return _gameType; }

    public ShipClass getShipClass() {
        return _shipClass;
    }
}