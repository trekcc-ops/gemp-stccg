package com.gempukku.stccg.cards.physicalcard;

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
import com.gempukku.stccg.actions.blueprints.ActionBlueprint;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.NullLocation;
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
    protected GameLocation _currentGameLocation;
    private boolean _placedOnMission = false;

    public AbstractPhysicalCard(int cardId, Player owner, CardBlueprint blueprint) {
        _cardId = cardId;
        _owner = owner;
        _blueprint = blueprint;
        _currentGameLocation = new NullLocation();
    }

    public Zone getZone() {
        return _zone;
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
            String affiliatedImage = _blueprint.getAffiliationImageUrl(affiliatedCard.getCurrentAffiliation());
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
        game.getModifiersEnvironment().addModifierHooks(game, this);
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


    private boolean canEnterPlay(DefaultGame game, List<Requirement> requirements) {
        if (cannotEnterPlayPerUniqueness())
            return false;
        if (requirements != null && !createActionContext(game).acceptsAllRequirements(requirements))
            return false;
        return !game.getModifiersQuerying().canNotPlayCard(getOwnerName(), this);
    }


    protected boolean cannotEnterPlayPerUniqueness() {
        return isUnique() && (_owner.hasACopyOfCardInPlay(getGame(), this));
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
    public GameLocation getGameLocation() {
        return _currentGameLocation;
    }


    public void setLocation(GameLocation location) {
        _currentGameLocation = location;
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

    public List<TopLevelSelectableAction> getRulesActionsWhileInPlay(Player player, DefaultGame cardGame) {
        return new LinkedList<>();
    }

    public List<TopLevelSelectableAction> getGameTextActionsWhileInPlay(Player player) {
        return _blueprint.getGameTextActionsWhileInPlay(player, this, getGame());
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

    public List<TopLevelSelectableAction> getOptionalResponseWhileInPlayActions(ActionResult actionResult) {
        List<TopLevelSelectableAction> result = new LinkedList<>();
        List<ActionBlueprint> triggers = _blueprint.getActivatedTriggers();

        if (triggers != null) {
            for (ActionBlueprint trigger : triggers) {
                TopLevelSelectableAction action = trigger.createActionWithNewContext(this, actionResult);
                if (action != null)
                    result.add(action);
            }
        }
        return result;
    }



    public TopLevelSelectableAction getDiscardedFromPlayTriggerAction(RequiredType requiredType) {
        ActionBlueprint actionBlueprint = _blueprint.getDiscardedFromPlayTrigger(requiredType);
        return (actionBlueprint == null) ?
                null : actionBlueprint.createActionWithNewContext(this, null);
    }


    private List<TopLevelSelectableAction> getActionsFromActionSources(String playerId, PhysicalCard card,
                                                     ActionResult actionResult, List<ActionBlueprint> actionBlueprints) {
        List<TopLevelSelectableAction> result = new LinkedList<>();
        actionBlueprints.forEach(actionSource -> {
            if (actionSource != null) {
                TopLevelSelectableAction action =
                        actionSource.createActionWithNewContext(card, playerId, actionResult);
                if (action != null) result.add(action);
            }
        });
        return result;
    }

    public List<TopLevelSelectableAction> getOptionalAfterTriggerActions(Player player, ActionResult actionResult)
            throws PlayerNotFoundException {
        return switch (_blueprint) {
            case Blueprint212_019 riskBlueprint ->
                    riskBlueprint.getValidResponses(this, player, actionResult, getGame());
            case Blueprint156_010 surpriseBlueprint ->
                    surpriseBlueprint.getValidResponses(this, player, actionResult, getGame());
            case Blueprint109_063 missionSpecBlueprint ->
                    missionSpecBlueprint.getValidResponses(this, player, actionResult, getGame());
            case null, default -> {
                assert _blueprint != null;
                yield getActionsFromActionSources(player.getPlayerId(), this, actionResult,
                        _blueprint.getTriggers(RequiredType.OPTIONAL));
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

    public Integer getNumberOfCopiesSeededByPlayer(Player player, DefaultGame cardGame) {
        int total = 0;
        Collection<Action> performedActions = cardGame.getActionsEnvironment().getPerformedActions();
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

    public List<TopLevelSelectableAction> createSeedCardActions() {
        List<TopLevelSelectableAction> result = new LinkedList<>();
        for (ActionBlueprint source : _blueprint.getSeedCardActionSources()) {
            result.add(source.createActionWithNewContext(this));
        }
        return result;
    }


    public boolean hasIcon(DefaultGame game, CardIcon icon) {
        return game.getModifiersQuerying().hasIcon(this, icon);
    }


    public boolean isPresentWith(PhysicalCard card) {
        return card.getGameLocation() == this.getGameLocation() &&
                card.getAttachedTo() == this.getAttachedTo();
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
            throws InvalidGameLogicException, PlayerNotFoundException {
        throw new InvalidGameLogicException(
                "Tried to call getEncounterActions for a card that does not have an encounter action");
    }

    public boolean isAtPlanetLocation() {
        return _currentGameLocation.isPlanet();
    }

    public boolean isAtSpaceLocation() {
        return _currentGameLocation.isSpace();
    }

    public Player getController() {
        return _owner;
    }

    public int getCost() { return _blueprint.getCost(); }

    public void setPlacedOnMission(boolean placedOnMission) {
        _placedOnMission = placedOnMission;
    }

    public boolean isPlacedOnMission() { return _placedOnMission; }

    public boolean isVisibleToPlayer(String playerName) {
        return _zone.isPublic() || _owner.getPlayerId().equals(playerName) ||
                isControlledBy(playerName);
    }

    public void removeFromCardGroup() {
        List<PhysicalCard> zoneCards = _owner.getCardGroupCards(_zone);
        zoneCards.remove(this);
    }

}