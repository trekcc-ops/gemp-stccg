package com.gempukku.stccg.actions;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.ActionsEnvironment;

import java.util.*;

public abstract class ActionyAction implements Action {
    private String _cardActionPrefix;
    protected Map<String, Boolean> _progressIndicators = new HashMap<>();
    protected boolean _wasCarriedOut;
    private final int _actionId;
    private final LinkedList<Action> _costs = new LinkedList<>();
    private final LinkedList<Action> _processedUsageCosts = new LinkedList<>();
    private final LinkedList<Action> _targeting = new LinkedList<>();
    private final LinkedList<Action> _processedCosts = new LinkedList<>();
    private final LinkedList<Action> _actionEffects = new LinkedList<>();
    private final LinkedList<Action> _processedActions = new LinkedList<>();
    private final LinkedList<Action> _usageCosts = new LinkedList<>();
    protected String _text;

    protected final String _performingPlayerId;
    private boolean _virtualCardAction;
    protected final ActionType _actionType;
    public ActionType getActionType() { return _actionType; }

    protected ActionyAction(ActionsEnvironment environment, ActionType actionType, String performingPlayerId) {
        _actionId = environment.getNextActionId();
        environment.incrementActionId();
        _actionType = actionType;
        _performingPlayerId = performingPlayerId;
    }

    protected ActionyAction(Player player, ActionType actionType) {
        this(player.getGame().getActionsEnvironment(), actionType, player.getPlayerId());
    }

    protected ActionyAction(Player player, String text, ActionType actionType) {
        this(player.getGame().getActionsEnvironment(), actionType, player.getPlayerId());
        _text = text;
    }

    protected ActionyAction(Player player, ActionType actionType, Enum<?>[] progressValues) {
        this(player.getGame().getActionsEnvironment(), actionType, player.getPlayerId());
        for (Enum<?> progressType : progressValues) {
            _progressIndicators.put(progressType.name(), false);
        }
    }


    protected ActionyAction(Player player, String text, ActionType actionType, Enum<?>[] progressTypes) {
        this(player.getGame().getActionsEnvironment(), actionType, player.getPlayerId());
        _text = text;
        for (Enum<?> progressType : progressTypes) {
            _progressIndicators.put(progressType.name(), false);
        }
    }

    // This constructor is only used for system queue actions
    protected ActionyAction(DefaultGame game) {
        this(game.getActionsEnvironment(), ActionType.OTHER, null);
    }


    @Override
    public void setVirtualCardAction(boolean virtualCardAction) {
        _virtualCardAction = virtualCardAction;
    }

    @Override
    public boolean isVirtualCardAction() {
        return _virtualCardAction;
    }

    @Override
    public String getPerformingPlayerId() {
        return _performingPlayerId;
    }

    public final void appendCost(Action cost) {
        _costs.add(cost);
    }

    public final void appendTargeting(Action targeting) {
        _targeting.add(targeting);
    }

    public final void appendEffect(Action action) {
        _actionEffects.add(action);
    }

    public final void insertCost(Action cost) {
        _costs.addAll(0, Collections.singletonList(cost));
    }

    public final void insertAction(Action action) {
        _actionEffects.addAll(0, Collections.singletonList(action));
    }

    /**
     * Sets the text shown for the action selection on the User Interface.
     * @param text the text to show for the action selection
     */
    public void setText(String text) {
        _text = text;
    }

    @Override
    public String getActionSelectionText(DefaultGame game) throws InvalidGameLogicException { return _text; }

    protected boolean isCostFailed() {
        for (Action processedCost : _processedCosts) {
            if (!processedCost.wasCarriedOut())
                return true;
        }
        for (Action usageCost : _processedUsageCosts) {
            if (!usageCost.wasCarriedOut())
                return true;
        }
        return false;
    }

    protected final Action getNextCost() {
        Action targetingCost = _targeting.poll();
        if (targetingCost != null) {
            _processedCosts.add(targetingCost);
            return targetingCost;
        }

        Action usageCost = _usageCosts.poll();
        if (usageCost != null) {
            _processedUsageCosts.add(usageCost);
            return usageCost;
        }

        Action cost = _costs.poll();
        if (cost != null)
            _processedCosts.add(cost);
        return cost;
    }

    protected final Action getNextAction() {
        final Action effect = _actionEffects.poll();
        if (effect != null)
            _processedActions.add(effect);
        return effect;
    }

    public boolean wasCarriedOut() {
        if (isCostFailed())
            return false;

        for (Action processedAction : _processedActions) {
            if (!processedAction.wasCarriedOut())
                return false;
        }

        return true;
    }

    public boolean canBeInitiated(DefaultGame cardGame) {
        return requirementsAreMet(cardGame) && costsCanBePaid(cardGame);
    }

    public abstract boolean requirementsAreMet(DefaultGame cardGame);

    public boolean costsCanBePaid(DefaultGame game) {
        // TODO - This may not accurately show if not all costs can be paid
        // TODO - Not sure on the legality here. Is it legal to initiate an action if you can't fully pay the costs?
        for (Action cost : _costs)
            if (!cost.canBeInitiated(game)) {
                return false;
            }
        for (Action usageCost : _usageCosts)
            if (!usageCost.canBeInitiated(game)) {
                return false;
            }
        return true;
    }

    public void setCardActionPrefix(String prefix) {
        _cardActionPrefix = prefix;
    }

    public String getCardActionPrefix() { return _cardActionPrefix; }

    public final void appendUsage(Action cost) {
        if (!_costs.isEmpty() || !_processedCosts.isEmpty() || !_actionEffects.isEmpty() || !_processedActions.isEmpty())
            throw new UnsupportedOperationException("Called appendUsage() in incorrect order");
        _usageCosts.add(cost);
    }

    public abstract Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException;

    public int getActionId() { return _actionId; }
    protected void setProgress(Enum<?> progressType) {
        _progressIndicators.put(progressType.name(), true);
    }

    protected List<Action> getActions() { return _actionEffects; }

    protected boolean getProgress(Enum<?> progressType) {
        return _progressIndicators.get(progressType.name());
    }

    public void insertEffect(Action actionEffect) { insertAction(actionEffect); }

}