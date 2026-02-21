package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.blueprints.ActionBlueprint;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.SpecialResponseActionSkill;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ChildCardRelationshipType;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.NullLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ModifierEffect;
import com.gempukku.stccg.player.Player;

import java.util.*;

import static com.gempukku.stccg.gamestate.ChildCardRelationshipType.ATOP;

public abstract class AbstractPhysicalCard implements PhysicalCard {

    protected final CardBlueprint _blueprint;
    protected final String _ownerName;

    @JsonProperty("cardId")
    protected int _cardId;
    protected Zone _zone;
    private Integer _stackedOnCardId;

    @JsonProperty("locationId")
    protected int _currentLocationId;
    private boolean _revealedSeedCard = false;

    @JsonProperty("parentCard")
    @JsonIdentityReference(alwaysAsId=true)
    protected PhysicalCard _parentCard;

    @JsonProperty("relationToParent")
    protected ChildCardRelationshipType _parentCardRelationship;


    @JsonProperty("childrenCards")
    @JsonIdentityReference(alwaysAsId=true)
    protected final Map<ChildCardRelationshipType, List<PhysicalCard>> _childrenCards = new HashMap<>();

    public AbstractPhysicalCard(int cardId, String ownerName, CardBlueprint blueprint) {
        _cardId = cardId;
        _ownerName = ownerName;
        _blueprint = blueprint;
        _currentLocationId = -999;
    }


    public AbstractPhysicalCard(int cardId, Player owner, CardBlueprint blueprint) {
        _cardId = cardId;
        _ownerName = owner.getPlayerId();
        _blueprint = blueprint;
        _currentLocationId = -999;
    }

    public Zone getZone() {
        return _zone;
    }

    public boolean isInDiscard(DefaultGame cardGame) {
        for (Player player : cardGame.getPlayers()) {
            if (player.getCardGroupCards(Zone.DISCARD).contains(this)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInHand(DefaultGame cardGame) {
        for (Player player : cardGame.getPlayers()) {
            if (player.getCardsInHand().contains(this)) {
                return true;
            }
        }
        return false;
    }

    public void setZone(Zone zone) { _zone = zone; }

    public String getBlueprintId() { return _blueprint.getBlueprintId(); }
    public String getImageUrl() {
        // TODO - Replace with a client communication method that pulls image options from the library
        String result;
        if (this instanceof AffiliatedCard affiliatedCard) {
            String affiliatedImage = _blueprint.getAffiliationImageUrl(affiliatedCard.getAffiliationForCardArt());
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

    public String getOwnerName() {
        return _ownerName;
    }


    public CardBlueprint getBlueprint() {
        return _blueprint;
    }

    public Integer getAttachedToCardId() {
        if (_parentCard == null) {
            return null;
        } else {
            return _parentCard.getCardId();
        }
    }

    public PhysicalCard getAttachedTo(DefaultGame cardGame) {
        return _parentCard;
    }


    public void stackOn(PhysicalCard physicalCard) {
        _stackedOnCardId = physicalCard.getCardId();
    }


    public PhysicalCard getStackedOn(DefaultGame cardGame) {
        if (_stackedOnCardId == null) {
            return null;
        } else {
            try {
                return cardGame.getCardFromCardId(_stackedOnCardId);
            } catch(CardNotFoundException exp) {
                cardGame.sendErrorMessage(exp);
                return null;
            }
        }
    }

    public String getTitle() { return _blueprint.getTitle(); }

    public boolean canInsertIntoSpaceline() { return _blueprint.canInsertIntoSpaceline(); }

    public boolean isOwnedBy(String playerName) {
        return Objects.equals(_ownerName, playerName);
    }
    public boolean isControlledBy(String playerId) {
        // TODO - Need to set modifiers for when cards get temporary control
        // PhysicalFacilityCard has an override for headquarters. Updates to this method should be made there as well.
        if (!_zone.isInPlay())
            return false;
        return playerId.equals(_ownerName);
    }
    public boolean isControlledBy(Player player) { return isControlledBy(player.getPlayerId()); }

    public String getCardLink() { return _blueprint.getCardLink(); }

    public GameLocation getGameLocation(ST1EGame cardGame) {
        return getGameLocation(cardGame.getGameState());
    }

    public GameLocation getGameLocation(ST1EGameState gameState) {
        GameLocation location = gameState.getLocationById(_currentLocationId);
        return (location == null) ? new NullLocation() : location;
    }



    public int getLocationId() {
        return _currentLocationId;
    }

    public boolean isAtSameLocationAsCard(PhysicalCard otherCard) {
        return _currentLocationId == otherCard.getLocationId() &&
                _currentLocationId >= 0;
    }


    public void setLocation(DefaultGame cardGame, GameLocation location) {
        setLocationId(cardGame, location.getLocationId());
    }

    public void setLocationId(int locationId) {
        _currentLocationId = locationId;
        for (ChildCardRelationshipType relationshipType : _childrenCards.keySet()) {
            for (PhysicalCard card : _childrenCards.get(relationshipType)) {
                card.setLocationId(locationId);
            }
        }
    }

    public void setLocationId(DefaultGame cardGame, int locationId) {
        _currentLocationId = locationId;
        for (PhysicalCard attachedCard : getAttachedCards(cardGame)) {
            attachedCard.setLocationId(cardGame, locationId);
        }
        for (PhysicalCard stackedCard : getStackedCards(cardGame)) {
            stackedCard.setLocationId(cardGame, locationId);
        }
    }



    public String getFullName() { return _blueprint.getFullName(); }

    public TopLevelSelectableAction getPlayCardAction(DefaultGame cardGame) {
        return getPlayCardAction(cardGame, false);
    }

    public abstract TopLevelSelectableAction getPlayCardAction(DefaultGame cardGame, boolean forFree);


    public boolean hasTextRemoved(DefaultGame game) {
        for (Modifier modifier :
                game.getModifiersAffectingCardByEffect(ModifierEffect.TEXT_MODIFIER, this)) {
            if (modifier.hasRemovedText(game, this))
                return true;
        }
        return false;
    }


    public boolean hasTransporters() {
        return false;
    }

    public CardType getCardType() { return _blueprint.getCardType(); }

    public List<TopLevelSelectableAction> getRulesActionsWhileInPlay(Player player, DefaultGame cardGame) {
        return new LinkedList<>();
    }

    public List<PhysicalCard> getStackedCards(DefaultGame game) {
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCard card : game.getGameState().getAllCardsInPlay()) {
            if (card.getStackedOn(game) == this)
                result.add(card);
        }
        return result;
    }

    public Collection<PhysicalCard> getAttachedCards(DefaultGame game) {
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCard physicalCard : game.getGameState().getAllCardsInPlay()) {
            if (Objects.equals(physicalCard.getAttachedToCardId(), _cardId)) {
                result.add(physicalCard);
            }
        }
        return result;
    }


    public List<TopLevelSelectableAction> getRequiredResponseActions(DefaultGame cardGame, ActionResult actionResult) {
        return _blueprint.getRequiredAfterTriggerActions(cardGame, actionResult, this);
    }


    public boolean isUnique() {
        return _blueprint.isUnique();
    }


    public boolean isCopyOf(PhysicalCard card) {
        return card.getBlueprint() == _blueprint;
    }

    public List<TopLevelSelectableAction> createSeedCardActions(DefaultGame cardGame) {
        List<TopLevelSelectableAction> result = new LinkedList<>();
        for (ActionBlueprint source : _blueprint.getSeedCardActionSources()) {
            result.add(source.createAction(cardGame, _ownerName, this));
        }
        return result;
    }


    public boolean hasIcon(DefaultGame game, CardIcon icon) {
        return game.hasIcon(this, icon);
    }


    // TODO May need to implement something here for weird non-personnel cards that have skills
    public boolean hasSkill(SkillName skillName, DefaultGame cardGame) { return false; }


    public boolean isInPlay() {
        if (_zone == null)
            return false;
        else return _zone.isInPlay();
    }

    public boolean hasCharacteristic(Characteristic characteristic) {
        return _blueprint.hasCharacteristic(characteristic);
    }

    public boolean isAtPlanetLocation(ST1EGame cardGame) {
        GameLocation location = cardGame.getGameState().getLocationById(_currentLocationId);
        return location != null && location.isPlanet();
    }

    public boolean isAtSpaceLocation(ST1EGame cardGame) {
        GameLocation location = cardGame.getGameState().getLocationById(_currentLocationId);
        return location != null && location.isSpace();
    }


    public String getControllerName() {
        return _ownerName;
    }


    public int getCost() { return _blueprint.getCost(); }

    public boolean isPlacedOnMission() { return _parentCardRelationship == ATOP &&
            _parentCard instanceof MissionCard;
    }

    public boolean isKnownToPlayer(String playerName) {
        return _zone.isPublic() || _ownerName.equals(playerName) ||
                isControlledBy(playerName) || _revealedSeedCard;
    }

    public void reveal() {
        _revealedSeedCard = true;
    }

    public boolean hasSameControllerAsCard(DefaultGame cardGame, PhysicalCard otherCard) {
        for (Player player : cardGame.getPlayers()) {
            if (this.isControlledBy(player) && otherCard.isControlledBy(player)) {
                return true;
            }
        }
        return false;
    }

    public List<CardIcon> getIcons() { return _blueprint.getIcons(); }

    @JsonProperty("cardId")
    public void setCardId(int cardId) {
        _cardId = cardId;
    }

    @Override
    public Collection<TopLevelSelectableAction> getOptionalResponseActionsWhileInPlay(DefaultGame game, Player player) {
        String playerName = player.getPlayerId();
        if (_blueprint == null || hasTextRemoved(game)) {
            return new ArrayList<>();
        } else {
            List<TopLevelSelectableAction> playerActions = new ArrayList<>();
            _blueprint.getTriggers(RequiredType.OPTIONAL).forEach(actionSource -> {
                if (actionSource != null) {
                    TopLevelSelectableAction action = actionSource.createAction(game, playerName, this);
                    if (action != null) {
                        playerActions.add(action);
                    }
                }
            });
            if (this instanceof PersonnelCard personnel) {
                for (Skill skill : personnel.getSkills(game)) {
                    if (skill instanceof SpecialResponseActionSkill responseSkill &&
                            responseSkill.isOptional()) {
                        ActionBlueprint blueprint = responseSkill.getActionBlueprint(this);
                        if (blueprint != null) {
                            TopLevelSelectableAction action = blueprint.createAction(game, playerName, this);
                            if (action != null) {
                                playerActions.add(action);
                            }
                        }
                    }
                }
            }
            return playerActions;
        }
    }

    public boolean isBeingEncounteredBy(String playerName, DefaultGame cardGame) {
        Stack<Action> actionStack = cardGame.getActionsEnvironment().getActionStack();
        for (Action action : actionStack) {
            if (action instanceof EncounterSeedCardAction encounterAction &&
                    encounterAction.getPerformingPlayerId().equals(playerName) &&
                    encounterAction.getEncounteredCard() == this) {
                return true;
            }
        }
        return false;
    }

    public boolean isBeingEncounteredBy(DefaultGame cardGame, PhysicalCard encounteringCard) {
        Stack<Action> actionStack = cardGame.getActionsEnvironment().getActionStack();
        for (Action action : actionStack) {
            if (action instanceof EncounterSeedCardAction encounterAction &&
                    encounterAction.getEncounteredCard() == this &&
                    encounterAction.getAttemptingUnit().includesInAttemptingUnit(cardGame, encounteringCard)
            ) {
                return true;
            }
        }
        return false;
    }


    public boolean isBeingEncountered(DefaultGame cardGame) {
        Stack<Action> actionStack = cardGame.getActionsEnvironment().getActionStack();
        for (Action action : actionStack) {
            if (action instanceof EncounterSeedCardAction encounterAction &&
                    encounterAction.getEncounteredCard() == this) {
                return true;
            }
        }
        return false;
    }

    public void setParentCardRelationship(PhysicalCard parentCard, ChildCardRelationshipType relationshipType) {
        clearParentCardRelationship();
        _parentCard = (parentCard instanceof MissionCard mission) ? mission.getBottomMission() : parentCard;
        _parentCardRelationship = relationshipType;
        parentCard.addChildCardRelationship(this, relationshipType);
        setLocationId(parentCard.getLocationId());
    }

    @Override
    public PhysicalCard getAtopCard() {
        if (_parentCardRelationship == ChildCardRelationshipType.ATOP) {
            return _parentCard;
        } else {
            return null;
        }
    }

    public void clearParentCardRelationship() {
        if (_parentCard != null) {
            _parentCard.clearChildRelationship(this);
            _parentCard = null;
            _parentCardRelationship = null;
        }
    }

    public void clearChildRelationship(PhysicalCard childCard) {
        // Assumes this is only ever called from childCard's "clearParentCardRelationship" method
        for (ChildCardRelationshipType relationshipType : _childrenCards.keySet()) {
            _childrenCards.get(relationshipType).remove(childCard);
        }
    }

    public void addChildCardRelationship(PhysicalCard childCard, ChildCardRelationshipType childCardRelationshipType) {
        _childrenCards.computeIfAbsent(childCardRelationshipType, k -> new ArrayList<>());
        _childrenCards.get(childCardRelationshipType).add(childCard);
    }

    public PhysicalCard getParentCard() {
        return _parentCard;
    }

    public boolean isAboard(PhysicalCard card) {
        return _parentCardRelationship == ChildCardRelationshipType.ABOARD && card == _parentCard;
    }

    public void setAsAtop(PhysicalCard destination) {
        setParentCardRelationship(destination, ATOP);
    }

}