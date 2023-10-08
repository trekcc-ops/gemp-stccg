package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.ActionSource;
import com.gempukku.stccg.actions.OptionalTriggerAction;
import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.common.Quadrant;
import com.gempukku.stccg.common.Zone;
import com.gempukku.stccg.effects.EffectResult;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ModifierHook;

import java.util.LinkedList;
import java.util.List;

public class PhysicalCard implements Filterable {
    protected Zone _zone;
    protected final String _blueprintId;
    protected final LotroCardBlueprint _blueprint;
    protected final String _owner;
    protected String _cardController;
    protected int _cardId;
    protected PhysicalCard _attachedTo;
    protected PhysicalCard _stackedOn;
    protected List<ModifierHook> _modifierHooks;
    protected List<ModifierHook> _modifierHooksStacked;
    protected List<ModifierHook> _modifierHooksInDiscard;
    protected List<ModifierHook> _modifierHooksControlledSite;
    protected Object _whileInZoneData;
    protected Integer _siteNumber;
    private int _locationZoneIndex;

    public PhysicalCard(int cardId, String blueprintId, String owner, LotroCardBlueprint blueprint) {
        _cardId = cardId;
        _blueprintId = blueprintId;
        _owner = owner;
        _blueprint = blueprint;
    }
    
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

    public void setCardController(String siteController) {
        _cardController = siteController;
    }

    public void startAffectingGame(DefaultGame game) {
        List<? extends Modifier> modifiers = _blueprint.getInPlayModifiers(game, this);
        if (modifiers != null) {
            _modifierHooks = new LinkedList<>();
            for (Modifier modifier : modifiers)
                _modifierHooks.add(game.getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    public void stopAffectingGame() {
        if (_modifierHooks != null) {
            for (ModifierHook modifierHook : _modifierHooks)
                modifierHook.stop();
            _modifierHooks = null;
        }
    }

    public void startAffectingGameStacked(DefaultGame game) {
        List<? extends Modifier> modifiers = _blueprint.getStackedOnModifiers(game, this);
        if (modifiers != null) {
            _modifierHooksStacked = new LinkedList<>();
            for (Modifier modifier : modifiers)
                _modifierHooksStacked.add(game.getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    public void stopAffectingGameStacked() {
        if (_modifierHooksStacked != null) {
            for (ModifierHook modifierHook : _modifierHooksStacked)
                modifierHook.stop();
            _modifierHooksStacked = null;
        }
    }

    public void startAffectingGameInDiscard(DefaultGame game) {
        List<? extends Modifier> modifiers = _blueprint.getInDiscardModifiers(game, this);
        if (modifiers != null) {
            _modifierHooksInDiscard = new LinkedList<>();
            for (Modifier modifier : modifiers)
                _modifierHooksInDiscard.add(game.getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    public void stopAffectingGameInDiscard() {
        if (_modifierHooksInDiscard != null) {
            for (ModifierHook modifierHook : _modifierHooksInDiscard)
                modifierHook.stop();
            _modifierHooksInDiscard = null;
        }
    }

    public void startAffectingGameControlledSite(DefaultGame game) {
        List<? extends Modifier> modifiers = _blueprint.getControlledSiteModifiers(game, this);
        if (modifiers != null) {
            _modifierHooksControlledSite = new LinkedList<>();
            for (Modifier modifier : modifiers)
                _modifierHooksControlledSite.add(game.getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    public LotroCardBlueprint getBlueprint() {
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

    
    public Integer getSiteNumber() {
        return _siteNumber;
    }

    
    public void setSiteNumber(Integer number) {
        _siteNumber = number;
    }

    public List<OptionalTriggerAction> getOptionalAfterTriggerActions(String playerId, DefaultGame game,
                                                                      EffectResult effectResult,
                                                                      PhysicalCard self) {
        List<OptionalTriggerAction> result = null;

        if (_blueprint.getOptionalAfterTriggers() != null) {
            result = new LinkedList<>();
            for (ActionSource optionalAfterTrigger : _blueprint.getOptionalAfterTriggers()) {
                DefaultActionContext<DefaultGame> actionContext = new DefaultActionContext<>(
                        playerId, game, self, effectResult,null);
                if (optionalAfterTrigger.isValid(actionContext)) {
                    OptionalTriggerAction action = new OptionalTriggerAction(self);
                    optionalAfterTrigger.createAction(action, actionContext);
                    result.add(action);
                }
            }

        }

        if (_blueprint.getCopiedFilters() != null) {
            if (result == null)
                result = new LinkedList<>();
            for (FilterableSource copiedFilter : _blueprint.getCopiedFilters()) {
                DefaultActionContext<DefaultGame> actionContext = new DefaultActionContext<>(
                        playerId, game, self, effectResult,null);
                final PhysicalCard firstActive = Filters.findFirstActive(
                        game, copiedFilter.getFilterable(actionContext)
                );
                if (firstActive != null)
                    addAllNotNull(result, firstActive.getOptionalAfterTriggerActions(playerId, game,
                            effectResult, self));
            }
        }

        return result;
    }

    private <T> void addAllNotNull(List<T> list, List<? extends T> possiblyNullList) {
        if (possiblyNullList != null)
            list.addAll(possiblyNullList);
    }

    public String getTitle() {
        return _blueprint.getTitle();
    }

    public boolean canInsertIntoSpaceline() {
        return false;
    }

    public void setLocationZoneIndex(int index) { _locationZoneIndex = index; }

    public int getLocationZoneIndex() { return _locationZoneIndex;  }

    public Quadrant getQuadrant() { return _blueprint.getQuadrant(); }

}
