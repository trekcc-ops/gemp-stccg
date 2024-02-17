package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ModifierEffect;
import com.gempukku.stccg.modifiers.ModifierHook;
import com.gempukku.stccg.rules.GameUtils;

import java.util.*;

public abstract class PhysicalCard implements Filterable {
    protected Zone _zone;
    protected final String _blueprintId;
    protected final CardBlueprint _blueprint;
    protected final Player _owner;
    protected final String _ownerName;
    protected String _cardController;
    protected int _cardId;
    protected PhysicalCard _attachedTo;
    protected PhysicalCard _stackedOn;
    protected List<ModifierHook> _modifierHooks;
    protected Map<Zone, List<ModifierHook>> _modifierHooksInZone; // modifier hooks specific to stacked and discard
    protected Object _whileInZoneData;
    protected int _locationZoneIndex;
    protected ST1ELocation _currentLocation;
    public PhysicalCard(int cardId, String blueprintId, Player owner, CardBlueprint blueprint) {
        _cardId = cardId;
        _blueprintId = blueprintId;
        _owner = owner;
        _ownerName = owner.getPlayerId();
        _blueprint = blueprint;
        _cardController = _ownerName; // TODO - This is likely not 100% accurate, as it is probably setting the controller before the card enters play.
    }

    public abstract DefaultGame getGame();
    
    public Zone getZone() { return _zone; }

    public void setZone(Zone zone) { _zone = zone; }
    
    public String getBlueprintId() { return _blueprintId; }
    public String getImageUrl() { return _blueprint.getImageUrl(); }

    public String getCardController() { return _cardController; }

    public void setCardId(int cardId) {
        _cardId = cardId;
    }
    
    public int getCardId() { return _cardId; }
    public Player getOwner() { return _owner; }

    public String getOwnerName() {
        return _ownerName;
    }

    public void startAffectingGame() {
        List<? extends Modifier> modifiers = _blueprint.getInPlayModifiers(getGame(), this);
        if (modifiers != null) {
            _modifierHooks = new LinkedList<>();
            for (Modifier modifier : modifiers)
                _modifierHooks.add(getGame().getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    public void stopAffectingGame() {
        if (_modifierHooks != null) {
            for (ModifierHook modifierHook : _modifierHooks)
                modifierHook.stop();
            _modifierHooks = null;
        }
    }

    public void startAffectingGameInZone(Zone zone) {
        List<? extends Modifier> modifiers = null;
        if (zone == Zone.STACKED) {
            modifiers = _blueprint.getStackedOnModifiers(this);
        } else if (zone == Zone.DISCARD) {
            modifiers = _blueprint.getInDiscardModifiers(this);
        }
        if (modifiers != null) {
            _modifierHooksInZone.put(zone, new LinkedList<>());
            for (Modifier modifier : modifiers)
                _modifierHooksInZone.get(zone).add(getGame().getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    public void stopAffectingGameInZone(Zone zone) {
        if (_modifierHooksInZone != null)
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


    public String getTitle() {
        return _blueprint.getTitle();
    }

    public boolean canInsertIntoSpaceline() { return _blueprint.canInsertIntoSpaceline(); }

    public void setLocationZoneIndex(int index) { _locationZoneIndex = index; }

    public int getLocationZoneIndex() { return _locationZoneIndex;  }

    public Quadrant getQuadrant() { return _blueprint.getQuadrant(); }

    public boolean isAffectingGame() { return getGame().getGameState().getCurrentPlayerId().equals(_ownerName); }
    public boolean canBeSeeded() { return false; }
    public boolean canBePlayed() { return true; }

    public boolean isControlledBy(String playerId) {
        return Objects.equals(_cardController, playerId);
    }

    public String getCardLink() { return _blueprint.getCardLink(_blueprintId); }
    public ST1ELocation getLocation() { return null; }
    public void setLocation(ST1ELocation location) { _currentLocation = location; }

    public String getFullName() { return _blueprint.getFullName(); }

    public Filter getFullValidTargetFilter() {
        return Filters.and(getBlueprint().getValidTargetFilter());
    }

    public CostToEffectAction getPlayCardAction(int twilightModifier, Filterable additionalAttachmentFilter,
                                                boolean ignoreRoamingPenalty) {

        if (_blueprint.getCardType() != CardType.EVENT) {
            final Filterable validTargetFilter = _blueprint.getValidTargetFilter();
            if (validTargetFilter == null) {
                Zone playToZone = switch (_blueprint.getCardType()) {
                    case COMPANION -> Zone.FREE_CHARACTERS;
                    case MINION -> Zone.SHADOW_CHARACTERS;
                    default -> Zone.SUPPORT;
                };
                PlayPermanentAction action = new PlayPermanentAction(this, playToZone, twilightModifier,
                        ignoreRoamingPenalty);

                getGame().getModifiersQuerying().appendExtraCosts(action, this);
                getGame().getModifiersQuerying().appendPotentialDiscounts(action, this);

                return action;
            } else {
                Filter fullAttachValidTargetFilter = Filters.and(_blueprint.getValidTargetFilter(),
                        (Filter) (game1, physicalCard) -> game1.getModifiersQuerying().canHavePlayedOn(
                                game1, this, physicalCard),
                        (Filter) (game12, physicalCard) -> {
                            if (_blueprint.getSide() == Side.SHADOW) {
                                final int twilightCostOnTarget = game12.getModifiersQuerying().getTwilightCost(
                                        game12, this, physicalCard, twilightModifier, false);
                                int potentialDiscount =
                                        game12.getModifiersQuerying().getPotentialDiscount(this);
                                return twilightCostOnTarget - potentialDiscount <= game12.getGameState().getTwilightPool();
                            } else {
                                return true;
                            }
                        });

                final AttachPermanentAction action = new AttachPermanentAction(getGame(), this,
                        Filters.and(fullAttachValidTargetFilter, additionalAttachmentFilter), twilightModifier);

                getGame().getModifiersQuerying().appendPotentialDiscounts(action, this);
                getGame().getModifiersQuerying().appendExtraCosts(action, this);

                return action;
            }
        } else {
            final PlayEventAction action = _blueprint.getPlayEventCardAction(this);

            getGame().getModifiersQuerying().appendPotentialDiscounts(action, this);
            getGame().getModifiersQuerying().appendExtraCosts(action, this);

            return action;
        }
    }

    public List<Modifier> getModifiers(List<ModifierSource> sources) {
        if (sources == null)
            return null;
        List<Modifier> result = new LinkedList<>();
        for (ModifierSource inPlayModifier : sources) {
            result.add(inPlayModifier.getModifier(
                    new DefaultActionContext(_ownerName, getGame(), this, null, null)));
        }
        return result;
    }

    public boolean hasTextRemoved() {
        for (Modifier modifier : getGame().getModifiersQuerying().getModifiersAffectingCard(ModifierEffect.TEXT_MODIFIER, this)) {
            if (modifier.hasRemovedText(getGame(), this))
                return true;
        }
        return false;
    }

    public boolean hasTransporters() {
        return false;
    }

    public CardType getCardType() { return _blueprint.getCardType(); }

    public List<? extends Action> getPhaseActionsInPlay(Player player) {
        return null;
    }

    public void attachToCardAtLocation(PhysicalCard destinationCard) {
        getGame().getGameState().transferCard(this, destinationCard);
        setLocation(destinationCard.getLocation());
    }

    public String getCardInfoHTML() {
        if (getZone().isInPlay() || getZone() == Zone.HAND) {
            StringBuilder sb = new StringBuilder();

            if (getZone() == Zone.HAND)
                sb.append("<b>Card is in hand - stats are only provisional</b><br><br>");
            else if (Filters.filterActive(getGame(), this).isEmpty())
                sb.append("<b>Card is inactive - current stats may be inaccurate</b><br><br>");

            sb.append("<b>Affecting card:</b>");
            Collection<Modifier> modifiers =
                    getGame().getModifiersQuerying().getModifiersAffecting(getGame(), this);
            for (Modifier modifier : modifiers) {
                String sourceText;
                PhysicalCard source = modifier.getSource();
                if (source != null) {
                    sourceText = GameUtils.getCardLink(source);
                } else {
                    sourceText = "<i>System</i>";
                }
                sb.append("<br><b>").append(sourceText).append(":</b> ");
                sb.append(modifier.getText(getGame(), this));
            }
            if (modifiers.isEmpty())
                sb.append("<br><i>nothing</i>");

            Collection<PhysicalCard> attachedCards = getAttachedCards();
            if (!attachedCards.isEmpty()) {
                sb.append("<br><b>Attached cards:</b>");
                sb.append("<br>").append(GameUtils.getAppendedNames(attachedCards));
            }

            List<PhysicalCard> stackedCards = getStackedCards();
            if (!stackedCards.isEmpty()) {
                sb.append("<br><b>Stacked cards:</b>");
                sb.append("<br>").append(GameUtils.getAppendedNames(stackedCards));
            }

            final String extraDisplayableInformation = _blueprint.getDisplayableInformation(this);
            if (extraDisplayableInformation != null) {
                sb.append("<br><b>Extra information:</b>");
                sb.append("<br>").append(extraDisplayableInformation);
            }

            sb.append("<br><br><b>Effective stats:</b>");

            StringBuilder keywords = new StringBuilder();
            for (Keyword keyword : Keyword.values()) {
                if (keyword.isInfoDisplayable()) {
                    if (keyword.isMultiples()) {
                        int count = getGame().getModifiersQuerying().getKeywordCount(
                                getGame(), this, keyword
                        );
                        if (count > 0)
                            keywords.append(keyword.getHumanReadable()).append(" +").append(count).append(", ");
                    } else {
                        if (getGame().getModifiersQuerying().hasKeyword(getGame(), this, keyword))
                            keywords.append(keyword.getHumanReadable()).append(", ");
                    }
                }
            }
            if (!keywords.isEmpty())
                sb.append("<br><b>Keywords:</b> ").append(keywords.substring(0, keywords.length() - 2));

            return sb.append(getTypeSpecificCardInfoHTML()).toString();
        } else {
            return null;
        }
    }

    public List<PhysicalCard> getStackedCards() { return getGame().getGameState().getStackedCards(this); }
    public Collection<PhysicalCard> getAttachedCards() { return getGame().getGameState().getAttachedCards(this); }

    public String getTypeSpecificCardInfoHTML() { return ""; }
}