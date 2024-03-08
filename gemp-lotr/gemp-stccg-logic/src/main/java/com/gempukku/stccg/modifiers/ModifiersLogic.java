package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.sources.ActionSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.common.filterable.lotr.Culture;
import com.gempukku.stccg.common.filterable.lotr.Keyword;
import com.gempukku.stccg.common.filterable.lotr.Side;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.filters.Filters;
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

    private int _drawnThisPhaseCount = 0;
    private final Map<Integer, Integer> _woundsPerPhaseMap = new HashMap<>();
    private final DefaultGame _game;
    private final Map<Player, Integer> _normalCardPlaysAvailable = new HashMap<>();
    private final int _normalCardPlaysPerTurn = 1; // TODO - Eventually this needs to be a format-driven parameter

    public ModifiersLogic(DefaultGame game) {
        _game = game;
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
/*            if (isCandidateForKeywordRemovalWithTextRemoval(_game, physicalCard, icon) &&
                    (hasTextRemoved(_game, physicalCard) || hasAllKeywordsRemoved(_game, physicalCard)))
                return false;
*/
/*            for (Modifier modifier : getIconModifiersAffectingCard(_game, ModifierEffect.REMOVE_KEYWORD_MODIFIER, keyword, physicalCard)) {
                if (modifier.isKeywordRemoved(game, physicalCard, keyword))
                    return false;
            }*/

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
 /*       if (modifierSource == null)
            return true;
        for (Modifier modifier : getIconModifiersAffectingCard(
                game, ModifierEffect.CANCEL_KEYWORD_BONUS_TARGET_MODIFIER, keyword, affecting)) {
            if (!modifier.appliesIconModifier(modifierSource, icon))
                return false;
        } */
        return true;
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

        _drawnThisPhaseCount = 0;
        _woundsPerPhaseMap.clear();
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
            if (isCandidateForKeywordRemovalWithTextRemoval(keyword) &&
                    (physicalCard.hasTextRemoved() || hasAllKeywordsRemoved(physicalCard)))
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

    @Override
    public int getKeywordCount(PhysicalCard physicalCard, Keyword keyword) {
        LoggingThreadLocal.logMethodStart();
        try {
            if (isCandidateForKeywordRemovalWithTextRemoval(keyword)
                    && (physicalCard.hasTextRemoved() || hasAllKeywordsRemoved(physicalCard)))
                return 0;

            for (Modifier modifier : getKeywordModifiersAffectingCard(ModifierEffect.REMOVE_KEYWORD_MODIFIER, keyword, physicalCard)) {
                if (modifier.isKeywordRemoved(_game, physicalCard, keyword))
                    return 0;
            }

            int result = physicalCard.getBlueprint().getKeywordCount(keyword);
            for (Modifier modifier : getKeywordModifiersAffectingCard(ModifierEffect.GIVE_KEYWORD_MODIFIER, keyword, physicalCard)) {
                if (appliesKeywordModifier(physicalCard, modifier.getSource(), keyword))
                    result += modifier.getKeywordCountModifier(physicalCard, keyword);
            }
            return Math.max(0, result);
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    private boolean isCandidateForKeywordRemovalWithTextRemoval(Keyword keyword) {
        return keyword != Keyword.ROAMING;
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
    public int getStrength(PhysicalCard physicalCard) {
        LoggingThreadLocal.logMethodStart();
        try {
            int result = physicalCard.getBlueprint().getAttribute(CardAttribute.STRENGTH);
            for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.STRENGTH_MODIFIER, physicalCard)) {
                final int strengthModifier = modifier.getStrengthModifier(physicalCard);
                if (strengthModifier <= 0 || appliesStrengthBonusModifier(modifier.getSource(), physicalCard))
                    result += strengthModifier;
            }
            return Math.max(0, result);
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean appliesStrengthBonusModifier(PhysicalCard modifierSource, PhysicalCard modifierTarget) {
        if (modifierSource != null)
            for (Modifier modifier :
                    getModifiersAffectingCard(ModifierEffect.STRENGTH_BONUS_SOURCE_MODIFIER, modifierSource)) {
                if (modifier.cancelsStrengthBonusModifier(_game, modifierSource, modifierTarget))
                    return false;
            }
        if (modifierTarget != null)
            for (Modifier modifier :
                    getModifiersAffectingCard(ModifierEffect.STRENGTH_BONUS_TARGET_MODIFIER, modifierTarget)) {
                if (modifier.cancelsStrengthBonusModifier(_game, modifierSource, modifierTarget))
                    return false;
            }
        return true;
    }

    @Override
    public int getTwilightCost(DefaultGame game, PhysicalCard physicalCard, PhysicalCard target, int twilightCostModifier, boolean ignoreRoamingPenalty) {
        LoggingThreadLocal.logMethodStart();
        try {
            int result = physicalCard.getBlueprint().getTwilightCost() + twilightCostModifier;
            result += physicalCard.getBlueprint().getTwilightCostModifier(physicalCard, target);
            for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.TWILIGHT_COST_MODIFIER, physicalCard)) {
                result += modifier.getTwilightCostModifier(physicalCard, target, ignoreRoamingPenalty);
            }
            result = Math.max(0, result);

            if (!ignoreRoamingPenalty && hasKeyword(physicalCard, Keyword.ROAMING)) {
                int roamingPenalty = getRoamingPenalty(game, physicalCard);
                result += Math.max(0, roamingPenalty);
            }
            return result;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public int getRoamingPenalty(DefaultGame game, PhysicalCard physicalCard) {
        LoggingThreadLocal.logMethodStart();
        try {
            int result = 2;
            for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.TWILIGHT_COST_MODIFIER, physicalCard)) {
                result += modifier.getRoamingPenaltyModifier(game, physicalCard);
            }
            return Math.max(0, result);
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean isAdditionalCardType(DefaultGame game, PhysicalCard card, CardType cardType) {
        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.ADDITIONAL_CARD_TYPE, card))
            if (modifier.isAdditionalCardTypeModifier(game, card, cardType))
                return true;
        return false;
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
    public boolean canCancelSkirmish(DefaultGame game, PhysicalCard card) {
        for (Modifier modifier : getModifiers(ModifierEffect.CANCEL_SKIRMISH_MODIFIER))
            if (!modifier.canCancelSkirmish(game, card))
                return false;
        return true;
    }

    @Override
    public boolean canHavePlayedOn(PhysicalCard playedCard, PhysicalCard target) {
        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.TARGET_MODIFIER, target))
            if (!modifier.canHavePlayedOn(_game, playedCard, target))
                return false;
        return true;
    }

    @Override
    public boolean canHaveTransferredOn(DefaultGame game, PhysicalCard playedCard, PhysicalCard target) {
        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.TARGET_MODIFIER, target))
            if (!modifier.canHaveTransferredOn(game, playedCard, target))
                return false;
        return true;
    }

    @Override
    public boolean canBeTransferred(PhysicalCard attachment) {
        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.TRANSFER_MODIFIER, attachment))
            if (!modifier.canBeTransferred(attachment))
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
    public List<? extends Action> getExtraPhaseActionsFromStacked(DefaultGame game, PhysicalCard target) {
        List<Action> activateCardActions = new LinkedList<>();

        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.EXTRA_ACTION_MODIFIER, target)) {
            List<? extends Action> actions = modifier.getExtraPhaseActionFromStacked(game, target);
            if (actions != null)
                activateCardActions.addAll(actions);
        }

        return activateCardActions;
    }

    @Override
    public boolean canPayExtraCostsToPlay(DefaultGame game, PhysicalCard target) {
        final List<? extends ExtraPlayCost> playCosts = target.getExtraCostToPlay();
        if (playCosts != null)
            for (ExtraPlayCost playCost : playCosts) {
                final Condition condition = playCost.getCondition();
                if ((condition == null || condition.isFulfilled()) && !playCost.canPayExtraCostsToPlay(game, target))
                    return false;
            }

        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.EXTRA_COST_MODIFIER, target)) {
            if (!modifier.canPayExtraCostsToPlay(game, target))
                return false;
        }

        return true;
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
    public boolean isValidAssignments(Side side, Map<PhysicalCard, Set<PhysicalCard>> assignments) {
        for (Modifier ignored : getModifiers(ModifierEffect.ASSIGNMENT_MODIFIER)) {
            return false;
        }
        return true;
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
        if (_game.getGameState().getCurrentPlayerId().equals(playerId)) {
            if (_game.getGameState().getCurrentPhase() != Phase.FELLOWSHIP)
                return true;
            return _game.getGameState().getCurrentPhase() == Phase.FELLOWSHIP && _drawnThisPhaseCount < 4;
        }
        return true;
    }

    /**
     * Rule of 4. "You cannot draw (or take into hand) more than 4 cards during your fellowship phase."
     *
     * @param game
     * @param playerId
     * @return
     */
    @Override
    public boolean canDrawCardAndIncrementForRuleOfFour(DefaultGame game, String playerId) {
        if (game.getGameState().getCurrentPlayerId().equals(playerId)) {
            if (game.getGameState().getCurrentPhase() != Phase.FELLOWSHIP)
                return true;
            if (game.getGameState().getCurrentPhase() == Phase.FELLOWSHIP && _drawnThisPhaseCount < 4) {
                _drawnThisPhaseCount++;
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canLookOrRevealCardsInHand(DefaultGame game, String revealingPlayerId, String performingPlayerId) {
        for (Modifier modifier : getModifiers(ModifierEffect.LOOK_OR_REVEAL_MODIFIER))
            if (!modifier.canLookOrRevealCardsInHand(game, revealingPlayerId, performingPlayerId))
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
    public boolean canBeLiberated(DefaultGame game, String playerId, PhysicalCard card, PhysicalCard source) {
        LoggingThreadLocal.logMethodStart();
        try {
            for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.LIBERATION_MODIFIER, card))
                if (!modifier.canBeLiberated(game, playerId, card, source))
                    return false;
            return true;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public Side hasInitiative() {
        for (Modifier modifier : getModifiers(ModifierEffect.INITIATIVE_MODIFIER)) {
            if (!modifier.shadowCanHaveInitiative(_game))
                return Side.FREE_PEOPLE;
        }

        for (Modifier modifier : getModifiers(ModifierEffect.INITIATIVE_MODIFIER)) {
            Side initiative = modifier.hasInitiative();
            if (initiative != null)
                return initiative;
        }

        int freePeopleInitiativeHandSize = _game.getGameState().getHand(_game.getGameState().getCurrentPlayerId()).size()
                + _game.getGameState().getVoidFromHand(_game.getGameState().getCurrentPlayerId()).size();

        int initiativeHandSize = 4;
        for (Modifier modifier : getModifiers(ModifierEffect.INITIATIVE_MODIFIER))
            initiativeHandSize += modifier.getInitiativeHandSizeModifier(_game);

        if (freePeopleInitiativeHandSize < initiativeHandSize)
            return Side.SHADOW;
        else
            return Side.FREE_PEOPLE;
    }

    private boolean canPlayerSpotCulture() {
        for (Modifier ignored : getModifiers(ModifierEffect.SPOT_MODIFIER))
            return false;
        return true;
    }

    @Override
    public int getNumberOfSpottableShadowCultures(DefaultGame game, String playerId) {
        Set<Culture> spottableCulturesBasedOnCards = new HashSet<>();
        for (PhysicalCard spottableFPCard : Filters.filterActive(game, Side.SHADOW, Filters.spottable)) {
            final Culture fpCulture = spottableFPCard.getBlueprint().getCulture();
            if (fpCulture != null)
                spottableCulturesBasedOnCards.add(fpCulture);
        }

        int result = 0;
        for (Culture spottableCulturesBasedOnCardsOnCard : spottableCulturesBasedOnCards) {
            if (canPlayerSpotCulture())
                result++;
        }

        return result;
    }

    @Override
    public int getSpotBonus(Filterable filter) {
        int result = 0;
        for (Modifier modifier : getModifiers(ModifierEffect.SPOT_MODIFIER))
            result += modifier.getSpotCountModifier(_game, filter);
        return Math.max(0, result);
    }

    @Override
    public boolean hasFlagActive(ModifierFlag modifierFlag) {
        return getModifiers(ModifierEffect.SPECIAL_FLAG_MODIFIER).stream()
                .anyMatch(modifier -> modifier.hasFlagActive(modifierFlag));
    }

    @Override
    public boolean canReplaceSite(String playerId, PhysicalCard siteToReplace) {
        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.REPLACE_SITE_MODIFIER, siteToReplace))
            if (!modifier.isSiteReplaceable(_game, playerId))
                return false;

        return true;
    }

    @Override
    public boolean canPlaySite(String playerId) {
        for (Modifier modifier : getModifiers(ModifierEffect.PLAY_SITE_MODIFIER))
            if (!modifier.canPlaySite(_game, playerId))
                return false;

        return true;
    }

    @Override
    public int getPotentialDiscount(PhysicalCard playedCard) {
        return playedCard.getPotentialDiscount();
    }

    @Override
    public void appendPotentialDiscounts(CostToEffectAction action, PhysicalCard playedCard) {
        playedCard.appendPotentialDiscountEffectsToAction(action);

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
