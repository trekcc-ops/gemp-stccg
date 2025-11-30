package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.blueprints.ActionBlueprint;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.blueprints.Blueprint109_063;
import com.gempukku.stccg.cards.blueprints.Blueprint156_010;
import com.gempukku.stccg.cards.blueprints.Blueprint212_019;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.NullLocation;
import com.gempukku.stccg.modifiers.ExtraPlayCost;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ModifierEffect;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.requirement.Requirement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractPhysicalCard implements PhysicalCard {

    protected final CardBlueprint _blueprint;
    protected final String _ownerName;
    protected final int _cardId;
    protected Zone _zone;
    private Integer _attachedToCardId;
    private Integer _stackedOnCardId;
    protected GameLocation _currentGameLocation;
    private boolean _placedOnMission = false;
    private boolean _revealedSeedCard = false;

    public AbstractPhysicalCard(int cardId, Player owner, CardBlueprint blueprint) {
        _cardId = cardId;
        _ownerName = owner.getPlayerId();
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

    public String getOwnerName() {
        return _ownerName;
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
        if (requirements != null && !createActionContext().acceptsAllRequirements(game, requirements))
            return false;
        return !game.getGameState().getModifiersQuerying().canNotPlayCard(getOwnerName(), this);
    }


    protected boolean cannotEnterPlayPerUniqueness() {
        for (PhysicalCard cardInPlay : getGame().getGameState().getAllCardsInPlay()) {
            if (cardInPlay.isCopyOf(this) && cardInPlay.isOwnedBy(_ownerName) && !cardInPlay.isUniversal())
                return true;
        }
        return false;
    }

    public boolean isOwnedBy(String playerName) {
        return Objects.equals(_ownerName, playerName);
    }

    public boolean canBeSeeded(DefaultGame game) { return canEnterPlay(game, _blueprint.getSeedRequirements()); }

    public boolean canBePlayed(DefaultGame game) { return canEnterPlay(game, _blueprint.getPlayRequirements()); }


    public boolean isControlledBy(String playerId) {
        // TODO - Need to set modifiers for when cards get temporary control
        // PhysicalFacilityCard has an override for headquarters. Updates to this method should be made there as well.
        if (!_zone.isInPlay())
            return false;
        return playerId.equals(_ownerName);
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
                game.getGameState().getModifiersQuerying().getModifiersAffectingCard(ModifierEffect.TEXT_MODIFIER, this)) {
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

    public Collection<PhysicalCard> getAttachedCards(DefaultGame game) {
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCard physicalCard : game.getGameState().getAllCardsInPlay()) {
            if (physicalCard.getAttachedTo() == this)
                result.add(physicalCard);
        }
        return result;
    }

    public List<? extends ExtraPlayCost> getExtraCostToPlay(DefaultGame game) {
        if (_blueprint.getExtraPlayCosts() == null)
            return null;

        List<ExtraPlayCost> result = new LinkedList<>();
        _blueprint.getExtraPlayCosts().forEach(
                extraPlayCost -> result.add(extraPlayCost.getExtraPlayCost(createActionContext())));
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

    ActionContext createActionContext() {
        return new DefaultActionContext(getOwnerName(), this, null);
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

    public Integer getNumberOfCopiesSeededByPlayer(String playerName, DefaultGame cardGame) {
        int total = 0;
        Collection<Action> performedActions = cardGame.getActionsEnvironment().getPerformedActions();
        for (Action action : performedActions) {
            if (action instanceof SeedCardAction seedCardAction) {
                if (Objects.equals(seedCardAction.getPerformingPlayerId(), playerName) &&
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
            result.add(source.createActionWithNewContext(getGame(), this));
        }
        return result;
    }


    public boolean hasIcon(DefaultGame game, CardIcon icon) {
        return game.getGameState().getModifiersQuerying().hasIcon(this, icon);
    }


    public boolean isPresentWith(PhysicalCard card) {
        return card.getGameLocation() == this.getGameLocation() &&
                card.getAttachedTo() == this.getAttachedTo();
    }

    public boolean hasSkill(SkillName skillName) { return false; }
    // TODO May need to implement something here for weird non-personnel cards that have skills

    public boolean checkTurnLimit(DefaultGame game, int max) {
        // TODO This isn't right since it checks against the card instead of the action
        return game.getGameState().getModifiersQuerying().getUntilEndOfTurnLimitCounter(this).getUsedLimit() < max;
    }


    public boolean isInPlay() {
        if (_zone == null)
            return false;
        else return _zone.isInPlay();
    }

    public boolean hasCharacteristic(Characteristic characteristic) {
        return _blueprint.hasCharacteristic(characteristic);
    }


    public boolean isAtPlanetLocation() {
        return _currentGameLocation.isPlanet();
    }

    public boolean isAtSpaceLocation() {
        return _currentGameLocation.isSpace();
    }

    public String getControllerName() {
        return _ownerName;
    }


    public int getCost() { return _blueprint.getCost(); }

    public void setPlacedOnMission(boolean placedOnMission) {
        _placedOnMission = placedOnMission;
    }

    public boolean isPlacedOnMission() { return _placedOnMission; }

    public boolean isKnownToPlayer(String playerName) {
        return _zone.isPublic() || _ownerName.equals(playerName) ||
                isControlledBy(playerName) || _revealedSeedCard;
    }

    public boolean isVisibleToPlayer(String playerName) {
        if (_zone.isPublic())
            return true;
        if (_zone == Zone.DISCARD && getGame().isDiscardPilePublic())
            return true;
        return _zone.isVisibleByOwner() && Objects.equals(_ownerName, playerName);
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
}