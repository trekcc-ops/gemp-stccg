package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.DiscountSource;
import com.gempukku.stccg.cards.blueprints.actionsource.ActionSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.SnapshotData;
import com.gempukku.stccg.game.Snapshotable;
import com.gempukku.stccg.gamestate.LoggingThreadLocal;

import java.util.*;

public class ModifiersLogic implements ModifiersEnvironment, ModifiersQuerying, Snapshotable<ModifiersLogic> {
    private final Map<ModifierEffect, List<Modifier>> _modifiers = new HashMap<>();

    private final Map<Phase, List<Modifier>> _untilStartOfPhaseModifiers = new HashMap<>();
    private final Map<Phase, List<Modifier>> _untilEndOfPhaseModifiers = new HashMap<>();

    private final Map<String, List<Modifier>> _untilEndOfPlayersNextTurnThisRoundModifiers = new HashMap<>();
    private final List<Modifier> _untilEndOfTurnModifiers = new LinkedList<>();

    private final Set<Modifier> _skipSet = new HashSet<>();

    private final Map<Phase, Map<String, LimitCounter>> _endOfPhaseLimitCounters = new HashMap<>();
    private final Map<Phase, Map<String, LimitCounter>> _startOfPhaseLimitCounters = new HashMap<>();
    private final Map<String, LimitCounter> _turnLimitCounters = new HashMap<>();
    private final Map<ActionSource, LimitCounter> _turnLimitActionSourceCounters = new HashMap<>();

    private final DefaultGame _game;
    private final Map<Player, Integer> _normalCardPlaysAvailable = new HashMap<>();
    private final int _normalCardPlaysPerTurn;

    public ModifiersLogic(DefaultGame game) {
        _game = game;
        _normalCardPlaysPerTurn = 1; // TODO - Eventually this needs to be a format-driven parameter
    }

    @Override
    public LimitCounter getUntilEndOfPhaseLimitCounter(PhysicalCard card, Phase phase) {
        return getUntilEndOfPhaseLimitCounter(card, "", phase);
    }

    @Override
    public LimitCounter getUntilStartOfPhaseLimitCounter(PhysicalCard card, Phase phase) {
        return getUntilStartOfPhaseLimitCounter(card, "", phase);
    }

    @Override
    public LimitCounter getUntilEndOfPhaseLimitCounter(PhysicalCard card, String prefix, Phase phase) {
        Map<String, LimitCounter> limitCounterMap = _endOfPhaseLimitCounters.computeIfAbsent(phase, k -> new HashMap<>());
        return limitCounterMap.computeIfAbsent(prefix + card.getCardId(), k -> new DefaultLimitCounter());
    }

    @Override
    public LimitCounter getUntilStartOfPhaseLimitCounter(PhysicalCard card, String prefix, Phase phase) {
        Map<String, LimitCounter> limitCounterMap = _startOfPhaseLimitCounters.computeIfAbsent(phase, k -> new HashMap<>());
        return limitCounterMap.computeIfAbsent(prefix + card.getCardId(), k -> new DefaultLimitCounter());
    }

    @Override
    public LimitCounter getUntilEndOfTurnLimitCounter(PhysicalCard card) {
        return getUntilEndOfTurnLimitCounter(card, "");
    }

    @Override
    public LimitCounter getUntilEndOfTurnLimitCounter(PhysicalCard card, String prefix) {
        return _turnLimitCounters.computeIfAbsent(prefix + card.getCardId(), k -> new DefaultLimitCounter());
    }

    @Override
    public LimitCounter getUntilEndOfTurnLimitCounter(ActionSource actionSource) {
        return _turnLimitActionSourceCounters.computeIfAbsent(actionSource, k -> new DefaultLimitCounter());
    }

    private List<Modifier> getEffectModifiers(ModifierEffect modifierEffect) {
        return _modifiers.computeIfAbsent(modifierEffect, k -> new LinkedList<>());
    }

    private void removeModifiers(List<Modifier> modifiers) {
        _modifiers.values().forEach(list -> list.removeAll(modifiers));
    }

    private void removeModifier(Modifier modifier) {
        _modifiers.values().forEach(list -> list.remove(modifier));
    }

    @Override
    public ModifierHook addAlwaysOnModifier(Modifier modifier) {
        addModifier(modifier);
        return new ModifierHookImpl(modifier);
    }

    private void addModifier(Modifier modifier) {
        ModifierEffect modifierEffect = modifier.getModifierEffect();
        getEffectModifiers(modifierEffect).add(modifier);
    }

    private List<Modifier> getModifiers(ModifierEffect modifierEffect) {
        return getKeywordModifiersAffectingCard(modifierEffect, null, null);
    }

    public List<Modifier> getModifiersAffectingCard(ModifierEffect modifierEffect, PhysicalCard card) {
        return getKeywordModifiersAffectingCard(modifierEffect, null, card);
    }

    private List<Modifier> getKeywordModifiersAffectingCard(ModifierEffect modifierEffect,
                                                            Keyword keyword, PhysicalCard card) {
        List<Modifier> modifiers = _modifiers.get(modifierEffect);
        if (modifiers == null)
            return Collections.emptyList();
        else {
            LinkedList<Modifier> liveModifiers = new LinkedList<>();
            for (Modifier modifier : modifiers) {
                if (keyword == null || ((KeywordAffectingModifier) modifier).getKeyword() == keyword) {
                    if (!_skipSet.contains(modifier)) {
                        _skipSet.add(modifier);
                        Condition condition = modifier.getCondition();
                        if (condition == null || condition.isFulfilled())
                            if (modifierEffect == ModifierEffect.TEXT_MODIFIER || modifier.getSource() == null ||
                                    modifier.isNonCardTextModifier() ||
                                    !modifier.getSource().hasTextRemoved()) {
                                if ((card == null || modifier.affectsCard(card)) &&
                                        (foundNoCumulativeConflict(liveModifiers, modifier)))
                                    liveModifiers.add(modifier);
                            }
                        _skipSet.remove(modifier);
                    }
                }
            }

            return liveModifiers;
        }
    }

    private List<Modifier> getIconModifiersAffectingCard(ModifierEffect modifierEffect,
                                                         CardIcon icon, PhysicalCard card) {
        List<Modifier> modifiers = _modifiers.get(modifierEffect);
        if (modifiers == null)
            return Collections.emptyList();
        else {
            LinkedList<Modifier> liveModifiers = new LinkedList<>();
            for (Modifier modifier : modifiers) {
                if (icon == null || ((IconAffectingModifier) modifier).getIcon() == icon) {
                    if (!_skipSet.contains(modifier)) {
                        _skipSet.add(modifier);
                        Condition condition = modifier.getCondition();
                        if (condition == null || condition.isFulfilled())
                            if (modifierEffect == ModifierEffect.TEXT_MODIFIER || modifier.getSource() == null ||
                                    modifier.isNonCardTextModifier() ||
                                    !modifier.getSource().hasTextRemoved()) {
                                if ((card == null || modifier.affectsCard(card)) &&
                                        (foundNoCumulativeConflict(liveModifiers, modifier)))
                                    liveModifiers.add(modifier);
                            }
                        _skipSet.remove(modifier);
                    }
                }
            }

            return liveModifiers;
        }
    }


    @Override
    public boolean hasIcon(PhysicalCard physicalCard, CardIcon icon) {
        try {
                    // TODO - Not accurate if the card has LOST an icon
            if (physicalCard.getBlueprint().hasIcon(icon))
                return true;

            for (Modifier modifier : getIconModifiersAffectingCard(
                    ModifierEffect.GAIN_ICON_MODIFIER, icon, physicalCard)) {
                if (appliesIconModifier(physicalCard, modifier.getSource(), icon))
                    if (modifier.hasIcon(physicalCard, icon))
                        return true;
            }
            return false;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    private boolean appliesIconModifier(PhysicalCard affecting, PhysicalCard modifierSource, CardIcon icon) {
        return false;
            // TODO - No real code here
    }


    @Override
    public boolean hasTextRemoved(PhysicalCard card) {
        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.TEXT_MODIFIER, card)) {
            if (modifier.hasRemovedText(_game, card))
                return true;
        }
        return false;
    }

    public void signalEndOfPhase() {
        Phase phase = _game.getGameState().getCurrentPhase();
        List<Modifier> list = _untilEndOfPhaseModifiers.get(phase);
        if (list != null) {
            removeModifiers(list);
            list.clear();
        }
        Map<String, LimitCounter> counterMap = _endOfPhaseLimitCounters.get(phase);
        if (counterMap != null)
            counterMap.clear();

        int _drawnThisPhaseCount = 0;
    }


    public void signalStartOfPhase(Phase phase) {
        List<Modifier> list = _untilStartOfPhaseModifiers.get(phase);
        if (list != null) {
            removeModifiers(list);
            list.clear();
        }

        Map<String, LimitCounter> counterMap = _startOfPhaseLimitCounters.get(phase);
        if (counterMap != null)
            counterMap.clear();
    }

    public void signalStartOfTurn() { signalStartOfTurn(_game.getCurrentPlayer().getPlayerId()); }

    public void signalStartOfTurn(String playerId) {
        List<Modifier> list = _untilEndOfPlayersNextTurnThisRoundModifiers.get(playerId);
        if (list != null) {
            for (Modifier modifier : list) {
                list.remove(modifier);
                _untilEndOfTurnModifiers.add(modifier);
            }
        }
        _normalCardPlaysAvailable.put(_game.getGameState().getPlayer(playerId), _normalCardPlaysPerTurn);
    }

    public void signalEndOfTurn() {
        removeModifiers(_untilEndOfTurnModifiers);
        _untilEndOfTurnModifiers.clear();

        for (List<Modifier> modifiers : _untilStartOfPhaseModifiers.values())
            removeModifiers(modifiers);
        _untilStartOfPhaseModifiers.clear();

        for (List<Modifier> modifiers : _untilEndOfPhaseModifiers.values())
            removeModifiers(modifiers);
        _untilEndOfPhaseModifiers.clear();

        _turnLimitCounters.clear();
        _turnLimitActionSourceCounters.clear();
        _startOfPhaseLimitCounters.clear();
        _endOfPhaseLimitCounters.clear();
    }

    public void signalEndOfRound() {
        for (List<Modifier> modifiers: _untilEndOfPlayersNextTurnThisRoundModifiers.values())
            removeModifiers(modifiers);
        _untilEndOfPlayersNextTurnThisRoundModifiers.clear();
    }

    @Override
    public void addUntilEndOfPhaseModifier(Modifier modifier, Phase phase) {
        addModifier(modifier);
        List<Modifier> list = _untilEndOfPhaseModifiers.computeIfAbsent(phase, k -> new LinkedList<>());
        list.add(modifier);
    }

    @Override
    public void addUntilStartOfPhaseModifier(Modifier modifier, Phase phase) {
        addModifier(modifier);
        List<Modifier> list = _untilStartOfPhaseModifiers.computeIfAbsent(phase, k -> new LinkedList<>());
        list.add(modifier);
    }

    @Override
    public void addUntilEndOfTurnModifier(Modifier modifier) {
        addModifier(modifier);
        _untilEndOfTurnModifiers.add(modifier);
    }

    @Override
    public void addUntilEndOfPlayersNextTurnThisRoundModifier(Modifier modifier, String playerId) {
        addModifier(modifier);
        List<Modifier> list = _untilEndOfPlayersNextTurnThisRoundModifiers.computeIfAbsent(playerId, k -> new LinkedList<>());
        list.add(modifier);
    }

    @Override
    public Collection<Modifier> getModifiersAffecting(PhysicalCard card) {
        Set<Modifier> result = new HashSet<>();
        for (List<Modifier> modifiers : _modifiers.values()) {
            for (Modifier modifier : modifiers) {
                Condition condition = modifier.getCondition();
                if (condition == null || condition.isFulfilled())
                    if (affectsCardWithSkipSet(card, modifier) && (foundNoCumulativeConflict(result, modifier)))
                        result.add(modifier);
            }
        }
        return result;
    }

    private boolean affectsCardWithSkipSet(PhysicalCard physicalCard, Modifier modifier) {
        if (!_skipSet.contains(modifier) && physicalCard != null) {
            _skipSet.add(modifier);
            boolean result = modifier.affectsCard(physicalCard);
            _skipSet.remove(modifier);
            return result;
        } else {
            return false;
        }
    }

    private boolean hasAllKeywordsRemoved(PhysicalCard card) {
        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.LOSE_ALL_KEYWORDS_MODIFIER, card)) {
            if (modifier.lostAllKeywords(card))
                return true;
        }
        return false;
    }

    @Override
    public boolean hasKeyword(PhysicalCard physicalCard, Keyword keyword) {
        LoggingThreadLocal.logMethodStart();
        try {
            if ((physicalCard.hasTextRemoved() || hasAllKeywordsRemoved(physicalCard)))
                return false;

            for (Modifier modifier : getKeywordModifiersAffectingCard(ModifierEffect.REMOVE_KEYWORD_MODIFIER, keyword, physicalCard)) {
                if (modifier.isKeywordRemoved(_game, physicalCard, keyword))
                    return false;
            }

            if (physicalCard.getBlueprint().hasKeyword(keyword))
                return true;

            for (Modifier modifier : getKeywordModifiersAffectingCard(ModifierEffect.GIVE_KEYWORD_MODIFIER, keyword, physicalCard)) {
                if (appliesKeywordModifier(physicalCard, modifier.getSource(), keyword))
                    if (modifier.hasKeyword(physicalCard, keyword))
                        return true;
            }
            return false;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    private boolean appliesKeywordModifier(PhysicalCard affecting, PhysicalCard modifierSource, Keyword keyword) {
        if (modifierSource == null)
            return true;
        for (Modifier modifier : getKeywordModifiersAffectingCard(
                        ModifierEffect.CANCEL_KEYWORD_BONUS_TARGET_MODIFIER, keyword, affecting)) {
            if (!modifier.appliesKeywordModifier(_game, modifierSource, keyword))
                return false;
        }
        return true;
    }

    @Override
    public int getMoveLimit(int baseMoveLimit) {
        int result = baseMoveLimit;
        for (Modifier modifier : getModifiers(ModifierEffect.MOVE_LIMIT_MODIFIER))
            result += modifier.getMoveLimitModifier();
        return Math.max(1, result);
    }

    @Override
    public int getAttribute(PhysicalCard card, CardAttribute attribute) {
        LoggingThreadLocal.logMethodStart();
        try {
            int result = card.getBlueprint().getAttribute(attribute);
            ModifierEffect effectType = null;
            if (attribute == CardAttribute.STRENGTH)
                effectType = ModifierEffect.STRENGTH_MODIFIER;
            else if (attribute == CardAttribute.CUNNING)
                effectType = ModifierEffect.CUNNING_MODIFIER;
            else if (attribute == CardAttribute.INTEGRITY)
                effectType = ModifierEffect.INTEGRITY_MODIFIER;
            List<Modifier> attributeModifiers = new LinkedList<>();
            if (effectType != null)
                attributeModifiers.addAll(getModifiersAffectingCard(effectType, card));
                    // TODO - Need to separate ships vs. personnel here
            attributeModifiers.addAll(getModifiersAffectingCard(ModifierEffect.ALL_ATTRIBUTE_MODIFIER, card));
            for (Modifier modifier : attributeModifiers) {
                result += modifier.getAttributeModifier(card);
            }
            return Math.max(0, result);
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public int getStrength(PhysicalCard physicalCard) {
        return getAttribute(physicalCard, CardAttribute.STRENGTH);
    }

    @Override
    public boolean canPlayAction(String performingPlayer, Action action) {
        for (Modifier modifier : getModifiers(ModifierEffect.ACTION_MODIFIER))
            if (!modifier.canPlayAction(_game, performingPlayer, action))
                return false;
        return true;
    }

    @Override
    public boolean canNotPlayCard(String performingPlayer, PhysicalCard card) {
        for (Modifier modifier : getModifiers(ModifierEffect.ACTION_MODIFIER))
            if (modifier.cantPlayCard(_game, performingPlayer, card))
                return true;
        return false;
    }

    @Override
    public boolean canHavePlayedOn(PhysicalCard playedCard, PhysicalCard target) {
        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.TARGET_MODIFIER, target))
            if (!modifier.canHavePlayedOn(_game, playedCard, target))
                return false;
        return true;
    }

    @Override
    public boolean shouldSkipPhase(DefaultGame game, Phase phase, String playerId) {
        for (Modifier modifier : getModifiers(ModifierEffect.ACTION_MODIFIER))
            if (modifier.shouldSkipPhase(game, phase, playerId))
                return true;
        return false;
    }

    @Override
    public List<? extends Action> getExtraPhaseActions(DefaultGame game, PhysicalCard target) {
        List<Action> activateCardActions = new LinkedList<>();

        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.EXTRA_ACTION_MODIFIER, target)) {
            List<? extends Action> actions = modifier.getExtraPhaseAction(game, target);
            if (actions != null)
                activateCardActions.addAll(actions);
        }

        return activateCardActions;
    }

    @Override
    public void appendExtraCosts(CostToEffectAction action, PhysicalCard target) {
        final List<? extends ExtraPlayCost> playCosts = target.getExtraCostToPlay();
        if (playCosts != null)
            for (ExtraPlayCost playCost : playCosts) {
                final Condition condition = playCost.getCondition();
                if (condition == null || condition.isFulfilled())
                    playCost.appendExtraCosts(target.getGame(), action, target);
            }

        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.EXTRA_COST_MODIFIER, target)) {
            modifier.appendExtraCosts(target.getGame(), action, target);
        }
    }

    @Override
    public boolean canBeDiscardedFromPlay(String performingPlayer, PhysicalCard card, PhysicalCard source) {
        LoggingThreadLocal.logMethodStart();
        try {
            for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.DISCARD_FROM_PLAY_MODIFIER, card))
                if (!modifier.canBeDiscardedFromPlay(_game, performingPlayer, card, source))
                    return false;
            return true;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean canBeReturnedToHand(PhysicalCard card, PhysicalCard source) {
        LoggingThreadLocal.logMethodStart();
        try {
            for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.RETURN_TO_HAND_MODIFIER, card))
                if (!modifier.canBeReturnedToHand(_game, card, source))
                    return false;
            return true;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    /**
     * Rule of 4. "You cannot draw (or take into hand) more than 4 cards during your fellowship phase."
     *
     * @param playerId
     * @return
     */
    @Override
    public boolean canDrawCardNoIncrement(String playerId) {
        _game.getGameState().getCurrentPlayerId();
        return true;
    }

    @Override
    public boolean canLookOrRevealCardsInHand(String revealingPlayerId, String performingPlayerId) {
        for (Modifier modifier : getModifiers(ModifierEffect.LOOK_OR_REVEAL_MODIFIER))
            if (!modifier.canLookOrRevealCardsInHand(_game, revealingPlayerId, performingPlayerId))
                return false;
        return true;
    }


    @Override
    public boolean canDiscardCardsFromHand(String playerId, PhysicalCard source) {
        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.DISCARD_NOT_FROM_PLAY, source))
            if (!modifier.canDiscardCardsFromHand(_game, playerId, source))
                return false;
        return true;
    }

    @Override
    public boolean canDiscardCardsFromTopOfDeck(String playerId, PhysicalCard source) {
        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.DISCARD_NOT_FROM_PLAY, source))
            if (!modifier.canDiscardCardsFromTopOfDeck(_game, playerId, source))
                return false;
        return true;
    }

    public boolean canPlayOutOfSequence(PhysicalCard source) {
        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.PLAY_OUT_OF_SEQUENCE, source))
            if (modifier.canPlayCardOutOfSequence(source)) {
                return true;
            }
        return false;
    }

    @Override
    public boolean hasFlagActive(ModifierFlag modifierFlag) {
        return getModifiers(ModifierEffect.SPECIAL_FLAG_MODIFIER).stream()
                .anyMatch(modifier -> modifier.hasFlagActive(modifierFlag));
    }

    @Override
    public void appendPotentialDiscounts(CostToEffectAction action, PhysicalCard playedCard) {
        if (playedCard.getBlueprint().getDiscountSources() != null) {
            ActionContext actionContext = playedCard.createActionContext();
            for (DiscountSource discountSource : playedCard.getBlueprint().getDiscountSources()) {
                action.appendPotentialDiscount(discountSource.getDiscountEffect(action, actionContext));
            }
        }

        for (Modifier modifier : getModifiersAffectingCard(
                ModifierEffect.POTENTIAL_DISCOUNT_MODIFIER, playedCard)) {
            modifier.appendPotentialDiscounts(action, playedCard);
        }
    }

    @Override
    public void generateSnapshot(ModifiersLogic selfSnapshot, SnapshotData snapshotData) {
        // TODO SNAPSHOT - Basically need to copy everything here
    }

    private class ModifierHookImpl implements ModifierHook {
        private final Modifier _modifier;

        private ModifierHookImpl(Modifier modifier) {
            _modifier = modifier;
        }

        @Override
        public void stop() {
            removeModifier(_modifier);
        }
    }

    private boolean foundNoCumulativeConflict(Collection<Modifier> modifierList, Modifier modifier) {
        // If modifier is not cumulative, then check if modifiers from another copy
        // card of same title is already in the list
        if (!modifier.isCumulative() && modifier.getSource() != null) {

            ModifierEffect modifierEffect = modifier.getModifierEffect();
            String cardTitle = modifier.getSource().getTitle();
            String forPlayer = modifier.getForPlayer();

            for (Modifier liveModifier : modifierList) {
                if (liveModifier.getModifierEffect() == modifierEffect
                        && liveModifier.getSource() != null
                        && liveModifier.getSource().getTitle().equals(cardTitle)
                        && liveModifier.isForPlayer(forPlayer)) {
                    return false;
                }
            }
        }
        return true;
    }

    public DefaultGame getGame() { return _game; }

    public int getNormalCardPlaysAvailable(Player player) { return _normalCardPlaysAvailable.get(player); }
    public void useNormalCardPlay(Player player) {
        int currentPlaysAvailable = _normalCardPlaysAvailable.get(player);
        _normalCardPlaysAvailable.put(player, currentPlaysAvailable - 1);
    }

}