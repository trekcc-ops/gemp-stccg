package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.blueprints.*;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.turn.RequiredTriggerAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.blueprints.ModifierBlueprint;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.requirement.PlayOutOfSequenceRequirement;
import com.gempukku.stccg.requirement.Requirement;

import java.util.*;

@JsonIgnoreProperties({"headquarters", "playable", "java-blueprint"})
public class CardBlueprint {

    @JsonProperty(value = "blueprintId", required = true)
    protected String _blueprintId;
    private GameType _gameType;
    private String _baseBlueprintId;
    @JsonProperty(value = "title", required = true)
    protected String title;
    @JsonProperty("subtitle")
    protected String subtitle;
    @JsonProperty("ship-class")
    protected ShipClass _shipClass;

    @JsonProperty("anyCanAttempt")
    private boolean _anyCanAttempt;

    @JsonProperty("anyExceptBorgCanAttempt")
    private boolean _anyExceptBorgCanAttempt;
    @JsonProperty("type")
    protected CardType _cardType;

    @JsonProperty("image-url")
    protected String imageUrl;

    @JsonProperty("rarity")
    protected String _rarity;
    @JsonProperty("property-logo")
    protected PropertyLogo _propertyLogo;
    private String _persona;

    @JsonProperty("lore")
    protected String _lore;
    @JsonProperty("species")
    protected List<Species> _species;

    @JsonProperty("characteristic")
    private final List<Characteristic> _characteristics = new ArrayList<>();

    @JsonProperty("uniqueness")
    protected Uniqueness uniqueness = null;

    @JsonProperty("icons")
    protected List<CardIcon> _icons;

    @JsonProperty("quadrant")
    protected Quadrant quadrant;

    @JsonProperty("location")
    protected String location;

    @JsonProperty("point-box")
    private PointBox _pointBox;

    @JsonProperty("mission-requirements")
    protected MissionRequirement _missionRequirements;
    @JsonProperty("affiliation")
    protected final List<Affiliation> _affiliations = new ArrayList<>();
    @JsonProperty("region")
    protected Region region;

    @JsonProperty("classification")
    protected SkillName _classification;
    private boolean _canInsertIntoSpaceline;
    private final List<Keyword> _keywords = new LinkedList<>();

    @JsonProperty("gametext")
    private final List<ShipSpecialEquipment> _specialEquipment = new LinkedList<>();

    @JsonProperty("affiliation-icons")
    private final List<Affiliation> _ownerAffiliationIcons = new ArrayList<>();
    private final List<Affiliation> _opponentAffiliationIcons = new ArrayList<>();
    private int _span;
    private Integer _opponentSpan;

    @JsonProperty("mission-type")
    protected MissionType _missionType;

    @JsonProperty("facility-type")
    protected FacilityType _facilityType;
    private int cost = -1;

    @JsonProperty("integrity")
    protected int _integrity;

    @JsonProperty("cunning")
    protected int _cunning;

    @JsonProperty("strength")
    protected int _strength;

    @JsonProperty("range")
    protected int _range;

    @JsonProperty("weapons")
    protected int _weapons;

    @JsonProperty("shields")
    protected int _shields;

    @JsonProperty("skill-box")
    protected SkillBox _skillBox;

    @JsonProperty("tribble-value")
    protected int tribbleValue;

    @JsonProperty("staffing")
    protected List<CardIcon> _staffing = new LinkedList<>();
    private String _missionRequirementsText;

    @JsonProperty("tribble-power")
    protected TribblePower tribblePower;
    private List<Requirement> _seedRequirements;
    private List<Requirement> _playRequirements;
    private List<FilterBlueprint> targetFilters;

    @JsonProperty("image-options")
    private final Map<Affiliation, String> _imageOptions = new HashMap<>();
    private final Map<RequiredType, List<ActionBlueprint>> _beforeTriggers = new HashMap<>();
    private final Map<RequiredType, List<ActionBlueprint>> _afterTriggers = new HashMap<>();
    private final Map<RequiredType, ActionBlueprint> _discardedFromPlayTriggers = new HashMap<>();
    private final List<TriggerActionBlueprint> _optionalInHandTriggers = new ArrayList<>();
    private final List<TriggerActionBlueprint> _activatedTriggers = new ArrayList<>();

    private List<ActionBlueprint> inDiscardPhaseActions;

    @JsonProperty("modifiers")
    protected final List<ModifierBlueprint> inPlayModifiers = new LinkedList<>();

    private List<ExtraPlayCostSource> extraPlayCosts;
    private List<Requirement> playInOtherPhaseConditions;

    @JsonProperty("playOutOfSequenceCondition")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<PlayOutOfSequenceRequirement> playOutOfSequenceConditions;

    @JsonProperty("actions")
    protected List<ActionBlueprint> _actionBlueprints = new LinkedList<>();

    public CardBlueprint() {
        for (RequiredType requiredType : RequiredType.values()) {
            _beforeTriggers.put(requiredType, new LinkedList<>());
            _afterTriggers.put(requiredType, new LinkedList<>());
        }
    }

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
    public String getMissionRequirementsText() { return _missionRequirements.toString(); }

    public int getPointsShown() {
        return (_pointBox == null) ? 0 : _pointBox.getPointsShown();
    }

    public boolean hasNoPointBox() { return _pointBox == null; }
    public void addOwnerAffiliationIcon(Affiliation affiliation) { _ownerAffiliationIcons.add(affiliation); }
    public Set<Affiliation> getOwnerAffiliationIcons() { return new HashSet<>(_ownerAffiliationIcons); }
    public Set<Affiliation> getOpponentAffiliationIcons() { return new HashSet<>(_ownerAffiliationIcons); }
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
    public Set<Affiliation> getAffiliations() { return new HashSet<>(_affiliations); }

    public int getIntegrity() { return _integrity;
    }

    public int getCunning() { return _cunning; }

    public int getStrength() { return _strength; }

    public int getRange() {
        return _range;
    }

    public int getWeapons() {
        return _weapons;
    }

    public int getShields() {
        return _shields;
    }


    public void setStaffing(List<CardIcon> staffing) { _staffing = staffing; }
    public List<CardIcon> getStaffing() { return _staffing; }
    public void setClassification(SkillName classification) { _classification = classification; }
    public SkillName getClassification() { return _classification; }

    public List<RegularSkill> getRegularSkills() {
        List<RegularSkill> result = new LinkedList<>();
        for (Skill skill : _skillBox.getSkillList()) {
            if (skill instanceof RegularSkill regularSkill)
                result.add(regularSkill);
        }
        return result;
    }

    public int getSkillDotCount() {
        return (_skillBox == null) ? 0 : _skillBox.getSkillDots();
    }
    public int getSpecialDownloadIconCount() {
        return (_skillBox == null) ? 0 : _skillBox.getSdIcons();
    }

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


    public List<ActionBlueprint> getSeedCardActionSources() {
        List<ActionBlueprint> result = new LinkedList<>();
        for (ActionBlueprint source : _actionBlueprints) {
            if (source instanceof SeedCardActionBlueprint)
                result.add(source);
        }
        return result;
    }


    public void appendPlayInOtherPhaseCondition(Requirement requirement) {
        if (playInOtherPhaseConditions == null)
            playInOtherPhaseConditions = new LinkedList<>();
        playInOtherPhaseConditions.add(requirement);
    }

    public List<TopLevelSelectableAction> getOptionalResponseActionsWhileInHand(DefaultGame cardGame,
                                                                                PhysicalCard thisCard, Player player,
                                                                                ActionResult actionResult) {
        List<TopLevelSelectableAction> result = new LinkedList<>();
        for (TriggerActionBlueprint trigger : _optionalInHandTriggers) {
            TopLevelSelectableAction action = trigger.createAction(cardGame, player.getPlayerId(), thisCard, actionResult);
            if (action != null) result.add(action);
        }
        return result;
    }


    public void appendPlayRequirement(Requirement requirement) {
        if (_playRequirements == null)
            _playRequirements = new LinkedList<>();
        _playRequirements.add(requirement);
    }

    public void appendTargetFilter(FilterBlueprint targetFilter) {
        if (targetFilters == null)
            targetFilters = new LinkedList<>();
        targetFilters.add(targetFilter);
    }

    public void appendInDiscardPhaseAction(ActionBlueprint actionBlueprint) {
        if (inDiscardPhaseActions == null)
            inDiscardPhaseActions = new LinkedList<>();
        inDiscardPhaseActions.add(actionBlueprint);
    }

    public void setDiscardedFromPlayTrigger(RequiredType requiredType, ActionBlueprint actionBlueprint) {
        _discardedFromPlayTriggers.put(requiredType, actionBlueprint);
    }
    public ActionBlueprint getDiscardedFromPlayTrigger(RequiredType requiredType) {
        return _discardedFromPlayTriggers.get(requiredType);
    }
    public List<Requirement> getSeedRequirements() { return _seedRequirements; }
    public List<Requirement> getPlayRequirements() { return _playRequirements; }

    public List<ExtraPlayCostSource> getExtraPlayCosts() { return extraPlayCosts; }

    public List<ActionBlueprint> getInDiscardPhaseActions() { return inDiscardPhaseActions; }
    public List<TriggerActionBlueprint> getActivatedTriggers() {
        return _activatedTriggers;
    }
    public List<TopLevelSelectableAction> getPlayActionsFromGameText(PhysicalCard thisCard, Player player,
                                                                     DefaultGame cardGame) {
        return new ArrayList<>();
    }
    public List<? extends Requirement> getPlayOutOfSequenceConditions() { return playOutOfSequenceConditions; }


    public List<TriggerActionBlueprint> getTriggers(RequiredType requiredType) {
        List<TriggerActionBlueprint> sourceResult = new ArrayList<>();
        for (ActionBlueprint source : _actionBlueprints) {
            if (source instanceof TriggerActionBlueprint triggerBlueprint) {
                if (requiredType == RequiredType.REQUIRED) {
                    if (source instanceof RequiredTriggerActionBlueprint)
                        sourceResult.add(triggerBlueprint);
                } else {
                    if (source instanceof OptionalTriggerActionBlueprint)
                        sourceResult.add(triggerBlueprint);
                }
            }
        }
        return sourceResult;
    }


    public String getFullName() {
        if (getSubtitle() != null)
            return getTitle() + ", " + getSubtitle();
        else
            return getTitle();
    }

    public boolean hasUniversalIcon() {
        List<CardType> typesWithUniversalSymbol =
                Arrays.asList(CardType.MISSION, CardType.SHIP, CardType.PERSONNEL, CardType.SITE);
        return typesWithUniversalSymbol.contains(_cardType) && isUniversal();
    }

    public String getCardLink() {
        return "<div class='cardHint' value='" + _blueprintId + "' + card_img_url='" + imageUrl + "'>" +
                (hasUniversalIcon() ? "&#x2756&nbsp;" : "") + getFullName() + "</div>";
    }

    protected List<Modifier> getGameTextWhileActiveInPlayModifiersFromJava(DefaultGame cardGame, PhysicalCard thisCard)
            throws InvalidGameLogicException {
        return new LinkedList<>();
    }


    public List<Modifier> getGameTextWhileActiveInPlayModifiers(DefaultGame cardGame, PhysicalCard thisCard,
                                                                ActionContext actionContext) {
        List<Modifier> result = new LinkedList<>();

        // Add in-play modifiers created through JSON definitions
        inPlayModifiers.forEach(modifierSource ->
                result.add(modifierSource.createModifier(cardGame, thisCard, actionContext)));

        // Add in-play modifiers created through Java definitions
        try {
            result.addAll(getGameTextWhileActiveInPlayModifiersFromJava(cardGame, thisCard));
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
        }

        return result;
    }

    public boolean hasCharacteristic(Characteristic characteristic) {
        return _characteristics.contains(characteristic);
    }

    public List<TopLevelSelectableAction> getRequiredAfterTriggerActions(DefaultGame cardGame,
                                                                         ActionResult actionResult, PhysicalCard card) {
        List<TopLevelSelectableAction> result = new LinkedList<>();
        getTriggers(RequiredType.REQUIRED).forEach(actionSource -> {
            if (actionSource instanceof RequiredTriggerActionBlueprint triggerSource) {
                RequiredTriggerAction action =
                        triggerSource.createAction(cardGame, card.getControllerName(), card, actionResult);
                if (action != null) result.add(action);
            }
        });
        return result;
    }


    public List<Skill> getSkills(DefaultGame game, PhysicalCard thisCard) {
        return _skillBox.getSkillList();
    }

    public void setPersona(String persona) { _persona = persona; }

    public String getPersona() {
        if (_persona == null)
            return title;
        else return _persona;
    }

    public boolean doesNotWorkWithPerRestrictionBox(AffiliatedCard thisCard, AffiliatedCard otherCard) {
        return false;
    }

    public String getBaseBlueprintId() { return _baseBlueprintId; }
    public void setBaseBlueprintId(String baseBlueprintId) { _baseBlueprintId = baseBlueprintId; }

    public List<Action> getEncounterActionsFromJava(ST1EPhysicalCard thisCard, DefaultGame game,
                                                    AttemptingUnit attemptingUnit,
                                                    EncounterSeedCardAction action,
                                                    MissionLocation missionLocation) throws PlayerNotFoundException {
        return new LinkedList<>();
    }

    public List<Action> getEncounterSeedCardActions(ST1EPhysicalCard thisCard, AttemptMissionAction attemptAction,
                                                    DefaultGame game, AttemptingUnit attemptingUnit,
                                                    MissionLocation missionLocation)
            throws InvalidGameLogicException, PlayerNotFoundException {
        List<Action> result = new LinkedList<>();
        for (ActionBlueprint blueprint : _actionBlueprints) {
            if (blueprint instanceof EncounterSeedCardActionBlueprint encounterBlueprint) {
                result.add(encounterBlueprint.createAction(game, attemptingUnit.getControllerName(), thisCard, attemptingUnit,
                        missionLocation, attemptAction));
            }
        }
        if (result.isEmpty()) {
            EncounterSeedCardAction action = new EncounterSeedCardAction(game, attemptingUnit.getControllerName(), thisCard,
                    attemptingUnit, attemptAction, missionLocation.getLocationId());
            List<Action> javaActions =
                    getEncounterActionsFromJava(thisCard, game, attemptingUnit, action, missionLocation);
            if (!javaActions.isEmpty()) {
                for (Action javaAction : javaActions) {
                    action.appendEffect(javaAction);
                }
                result.add(action);
            }
        }
        if (result.isEmpty()) {
            throw new InvalidGameLogicException("Unable to identify encounter seed card actions");
        } else {
            return result;
        }
    }


    public void setShipClass(ShipClass shipClass) {
        _shipClass = shipClass;
    }

    public void addSpecialEquipment(Collection<ShipSpecialEquipment> specialEquipment) {
        _specialEquipment.addAll(specialEquipment);
    }


    public List<TopLevelSelectableAction> getGameTextActionsWhileInPlay(Player player, PhysicalCard thisCard,
                                                                        DefaultGame cardGame) {
        List<TopLevelSelectableAction> result = new ArrayList<>();
        for (ActionBlueprint actionBlueprint : _actionBlueprints) {
            if (actionBlueprint instanceof ActivateCardActionBlueprint) {
                TopLevelSelectableAction action = actionBlueprint.createAction(cardGame, player.getPlayerId(), thisCard);
                if (action != null) result.add(action);
            }
        }
        return result;
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

    public PhysicalCard createPhysicalCard(int cardId, String playerName) {
        return switch(_cardType) {
            case EQUIPMENT -> new EquipmentCard(cardId, playerName, this);
            case FACILITY -> new FacilityCard(cardId, playerName, this);
            case MISSION -> new MissionCard(cardId, playerName, this);
            case PERSONNEL -> new PersonnelCard(cardId, playerName, this);
            case SHIP -> new ShipCard(cardId, playerName, this);
            default -> new ST1EPhysicalCard(cardId, playerName, this);
        };
    }
    

    public PhysicalCard createPhysicalCard(int cardId, Player player) {
        return createPhysicalCard(cardId, player.getPlayerId());
    }

}