package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.AttachPermanentAction;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.PlayEventAction;
import com.gempukku.stccg.actions.PlayPermanentAction;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ModifierHook;
import com.gempukku.stccg.rules.GameUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class PhysicalCard implements Filterable {
    protected Zone _zone;
    protected final String _blueprintId;
    protected final CardBlueprint _blueprint;
    protected final String _owner;
    protected String _cardController;
    protected int _cardId;
    protected PhysicalCard _attachedTo;
    protected PhysicalCard _stackedOn;
    protected List<ModifierHook> _modifierHooks;
    protected Map<Zone, List<ModifierHook>> _modifierHooksInZone; // modifier hooks specific to stacked and discard
    protected Object _whileInZoneData;
    protected int _locationZoneIndex;
    public PhysicalCard(int cardId, String blueprintId, String owner, CardBlueprint blueprint) {
        _cardId = cardId;
        _blueprintId = blueprintId;
        _owner = owner;
        _blueprint = blueprint;
        _cardController = _owner; // TODO - This is likely not 100% accurate, as it is probably setting the controller before the card enters play.
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

    public String getOwner() {
        return _owner;
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

    public boolean isAffectingGame() { return getGame().getGameState().getCurrentPlayerId().equals(_owner); }
    public boolean canBeSeeded() { return false; }
    public boolean canBePlayed() { return true; }

    public boolean isControlledBy(String playerId) {
        return Objects.equals(_cardController, playerId);
    }

    public String getCardLink() { return _blueprint.getCardLink(_blueprintId); }
    public ST1ELocation getCurrentLocation() { return null; }

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
                    new DefaultActionContext(_owner, getGame(), this, null, null)));
        }
        return result;
    }
}