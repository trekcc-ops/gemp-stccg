package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.actionsource.ActionSource;
import com.gempukku.stccg.cards.blueprints.effect.ModifierSource;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.SnapshotData;
import com.gempukku.stccg.game.Snapshotable;

import java.util.*;

@JsonSerialize(using = ModifiersLogicSerializer.class)
public class ModifiersLogic implements ModifiersEnvironment, ModifiersQuerying, Snapshotable<ModifiersLogic> {

    private final Map<ModifierEffect, List<Modifier>> _modifiers = new EnumMap<>(ModifierEffect.class);
    private final Map<Phase, List<Modifier>> _untilEndOfPhaseModifiers = new EnumMap<>(Phase.class);
    private final Map<String, List<Modifier>> _untilEndOfPlayersNextTurnThisRoundModifiers = new HashMap<>();
    private final Map<PhysicalCard, List<ModifierHook>> _modifierHooks = new HashMap<>();

    private final Collection<Modifier> _untilEndOfTurnModifiers = new LinkedList<>();
    private final Collection<Modifier> _skipSet = new HashSet<>();
    private final Map<String, LimitCounter> _turnLimitCounters = new HashMap<>();
    private final Map<ActionSource, LimitCounter> _turnLimitActionSourceCounters = new HashMap<>();
    private final DefaultGame _game;
    private final Map<Player, Integer> _normalCardPlaysAvailable = new HashMap<>();
    private final int _normalCardPlaysPerTurn;

    public ModifiersLogic(DefaultGame game) {
        _game = game;
        _normalCardPlaysPerTurn = 1; // TODO - Eventually this needs to be a format-driven parameter
    }

    public ModifiersLogic(DefaultGame game, JsonNode node) {
        _game = game;
        _normalCardPlaysPerTurn = 1; // TODO - Eventually this needs to be a format-driven parameter
        // TODO - load all modifiers from JsonNode
    }


    @Override
    public LimitCounter getUntilEndOfTurnLimitCounter(PhysicalCard card) {
        return getUntilEndOfTurnLimitCounter(card, "");
    }

    @Override
    public LimitCounter getUntilEndOfTurnLimitCounter(PhysicalCard card, String prefix) {
        return _turnLimitCounters.computeIfAbsent(prefix + card.getCardId(), entry -> new DefaultLimitCounter());
    }

    @Override
    public LimitCounter getUntilEndOfTurnLimitCounter(ActionSource actionSource) {
        return _turnLimitActionSourceCounters.computeIfAbsent(actionSource, entry -> new DefaultLimitCounter());
    }

    private List<Modifier> getEffectModifiers(ModifierEffect modifierEffect) {
        return _modifiers.computeIfAbsent(modifierEffect, entry -> new LinkedList<>());
    }

    private void removeModifiers(Collection<Modifier> modifiers) {
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
        return getModifiersAffectingCard(modifierEffect, null);
    }

    public List<Modifier> getModifiersAffectingCard(ModifierEffect modifierEffect, PhysicalCard card) {
        List<Modifier> modifiers = _modifiers.get(modifierEffect);
        if (modifiers == null)
            return Collections.emptyList();
        else {
            LinkedList<Modifier> liveModifiers = new LinkedList<>();
            for (Modifier modifier : modifiers) {
                if (!_skipSet.contains(modifier)) {
                    _skipSet.add(modifier);
                    Condition condition = modifier.getCondition();
                    if (condition == null || condition.isFulfilled())
                        if (shouldAdd(modifierEffect, modifier)) {
                            if ((card == null || modifier.affectsCard(card)) &&
                                    (foundNoCumulativeConflict(liveModifiers, modifier)))
                                liveModifiers.add(modifier);
                        }
                    _skipSet.remove(modifier);
                }
            }
            return liveModifiers;
        }
    }

    private boolean shouldAdd(ModifierEffect modifierEffect, Modifier modifier) {
        return modifierEffect == ModifierEffect.TEXT_MODIFIER || modifier.getSource() == null ||
                modifier.isNonCardTextModifier() ||
                !modifier.getSource().hasTextRemoved(_game);
    }

    private List<Modifier> getIconModifiersAffectingCard(CardIcon icon, PhysicalCard card) {
        List<Modifier> modifiers = _modifiers.get(ModifierEffect.GAIN_ICON_MODIFIER);
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
                            if (shouldAdd(ModifierEffect.GAIN_ICON_MODIFIER, modifier)) {
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

    private List<Modifier> getSkillModifiersAffectingCard(SkillName skill, PhysicalCard card) {
        List<Modifier> modifiers = _modifiers.get(ModifierEffect.GAIN_SKILL_MODIFIER);
        if (modifiers == null)
            return Collections.emptyList();
        else {
            LinkedList<Modifier> liveModifiers = new LinkedList<>();
            for (Modifier modifier : modifiers) {
                if (skill == null || ((SkillAffectingModifier) modifier).getSkill() == skill) {
                    if (!_skipSet.contains(modifier)) {
                        _skipSet.add(modifier);
                        Condition condition = modifier.getCondition();
                        if (condition == null || condition.isFulfilled())
                            if (shouldAdd(ModifierEffect.GAIN_SKILL_MODIFIER, modifier)) {
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
        // TODO - Not accurate if the card has LOST an icon
        // TODO - Does not account for modifiers that are inactive
        if (physicalCard.getBlueprint().hasIcon(icon))
            return true;

        for (Modifier modifier : getIconModifiersAffectingCard(
                icon, physicalCard)) {
            if (modifier.hasIcon(physicalCard, icon))
                return true;
        }
        return false;
    }

    public Integer getSkillLevel(PhysicalCard physicalCard, SkillName skillName) {
        int level = 0;
        for (Skill skill : physicalCard.getBlueprint().getSkills()) {
            if (skill instanceof RegularSkill regularSkill) {
                if (regularSkill.getRegularSkill() == skillName) {
                    level += regularSkill.getLevel();
                }
            }
        }
        if (physicalCard.getBlueprint().getClassification() == skillName)
            level += 1;

        for (Modifier modifier : getSkillModifiersAffectingCard(
                skillName, physicalCard)) {
            if (modifier instanceof GainSkillModifier skillModifier && skillModifier.getSkill() == skillName)
                level += 1;
        }
        return level;
    }


    @Override
    public boolean hasTextRemoved(PhysicalCard card) {
        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.TEXT_MODIFIER, card)) {
            if (modifier.hasRemovedText(_game, card))
                return true;
        }
        return false;
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

        // Unstop all "stopped" cards
        // TODO - Does not account for cards that can be stopped for multiple turns
        for (PhysicalCard card : _game.getGameState().getAllCardsInPlay()) {
            if (card instanceof ST1EPhysicalCard stCard && stCard.isStopped()) {
                stCard.unstop();
            }
        }
    }

    public void signalEndOfTurn() {
        removeModifiers(_untilEndOfTurnModifiers);
        _untilEndOfTurnModifiers.clear();

        for (List<Modifier> modifiers : _untilEndOfPhaseModifiers.values())
            removeModifiers(modifiers);
        _untilEndOfPhaseModifiers.clear();

        _turnLimitCounters.clear();
        _turnLimitActionSourceCounters.clear();
    }

    public boolean canPlayerSolveMission(String playerId, MissionCard mission) {
        for (Modifier modifier : getModifiers(ModifierEffect.SOLVE_MISSION_MODIFIER)) {
            if (modifier instanceof PlayerCannotSolveMissionModifier missionModifier)
                if (missionModifier.cannotSolveMission(mission, playerId))
                    return false;
        }
        return true;
    }

    public void signalEndOfRound() {
        for (List<Modifier> modifiers: _untilEndOfPlayersNextTurnThisRoundModifiers.values())
            removeModifiers(modifiers);
        _untilEndOfPlayersNextTurnThisRoundModifiers.clear();
    }

    @Override
    public void addUntilEndOfPhaseModifier(Modifier modifier, Phase phase) {
        addModifier(modifier);
        List<Modifier> list = _untilEndOfPhaseModifiers.computeIfAbsent(phase, entry -> new LinkedList<>());
        list.add(modifier);
    }

    @Override
    public void addUntilEndOfTurnModifier(Modifier modifier) {
        addModifier(modifier);
        _untilEndOfTurnModifiers.add(modifier);
    }

    @Override
    public Collection<Modifier> getModifiersAffecting(PhysicalCard card) {
        Collection<Modifier> result = new HashSet<>();
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

    @Override
    public int getAttribute(PhysicalCard card, CardAttribute attribute) {
        int result = card.getBlueprint().getAttribute(attribute);
        ModifierEffect effectType = null;
        if (attribute == CardAttribute.STRENGTH)
            effectType = ModifierEffect.STRENGTH_MODIFIER;
        else if (attribute == CardAttribute.CUNNING)
            effectType = ModifierEffect.CUNNING_MODIFIER;
        else if (attribute == CardAttribute.INTEGRITY)
            effectType = ModifierEffect.INTEGRITY_MODIFIER;
        Collection<Modifier> attributeModifiers = new LinkedList<>();
        if (effectType != null)
            attributeModifiers.addAll(getModifiersAffectingCard(effectType, card));
        // TODO - Need to separate ships vs. personnel here
        attributeModifiers.addAll(getModifiersAffectingCard(ModifierEffect.ALL_ATTRIBUTE_MODIFIER, card));
        for (Modifier modifier : attributeModifiers) {
            result += modifier.getAttributeModifier(card);
        }
        return Math.max(0, result);
    }

    @Override
    public int getStrength(PhysicalCard physicalCard) {
        return getAttribute(physicalCard, CardAttribute.STRENGTH);
    }

    @Override
    public boolean canPerformAction(String performingPlayer, Action action) {
        for (Modifier modifier : getModifiers(ModifierEffect.ACTION_MODIFIER))
            if (!modifier.canPerformAction(_game, performingPlayer, action))
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
    public void appendExtraCosts(Action action, PhysicalCard target) {
        final List<? extends ExtraPlayCost> playCosts = target.getExtraCostToPlay(_game);
        if (playCosts != null)
            for (ExtraPlayCost playCost : playCosts) {
                final Condition condition = playCost.getCondition();
                if (condition == null || condition.isFulfilled())
                    playCost.appendExtraCosts(_game, action, target);
            }

        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.EXTRA_COST_MODIFIER, target)) {
            modifier.appendExtraCosts(_game, action, target);
        }
    }

    @Override
    public boolean canBeDiscardedFromPlay(String performingPlayer, PhysicalCard card, PhysicalCard source) {
        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.DISCARD_FROM_PLAY_MODIFIER, card))
            if (!modifier.canBeDiscardedFromPlay(_game, performingPlayer, card, source))
                return false;
        return true;
    }

    @Override
    public boolean canBeReturnedToHand(PhysicalCard card, PhysicalCard source) {
        for (Modifier modifier : getModifiersAffectingCard(ModifierEffect.RETURN_TO_HAND_MODIFIER, card))
            if (!modifier.canBeReturnedToHand(_game, card, source))
                return false;
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
    public ModifiersLogic generateSnapshot(SnapshotData snapshotData) {
        // TODO SNAPSHOT - Basically need to copy everything here
        return this;
    }

    final class ModifierHookImpl implements ModifierHook {
        private final Modifier _modifier;

        private ModifierHookImpl(Modifier modifier) {
            _modifier = modifier;
        }

        @Override
        public void stop() {
            removeModifier(_modifier);
        }
    }

    private static boolean foundNoCumulativeConflict(Iterable<Modifier> modifierList, Modifier modifier) {
        // If modifier is not cumulative, then check if modifiers from another copy
        // card of same title is already in the list
        if (!modifier.isCumulative() && modifier.getSource() != null) {

            ModifierEffect modifierEffect = modifier.getModifierEffect();
            String cardTitle = modifier.getSource().getTitle();
            String forPlayer = modifier.getForPlayer();

            for (Modifier liveModifier : modifierList) {
                if (isSameEffectFromSameCard(liveModifier, modifierEffect, cardTitle, forPlayer)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isSameEffectFromSameCard(Modifier liveModifier, ModifierEffect modifierEffect,
                                                    String cardTitle, String forPlayer) {
        return liveModifier.getModifierEffect() == modifierEffect
                && liveModifier.getSource() != null
                && liveModifier.getSource().getTitle().equals(cardTitle)
                && liveModifier.isForPlayer(forPlayer);
    }

    public int getNormalCardPlaysAvailable(Player player) { return _normalCardPlaysAvailable.get(player); }
    public void useNormalCardPlay(Player player) {
        int currentPlaysAvailable = _normalCardPlaysAvailable.get(player);
        _normalCardPlaysAvailable.put(player, currentPlaysAvailable - 1);
    }

    @Override
    public void removeModifierHooks(PhysicalCard card) {
        if (_modifierHooks.get(card) != null) {
            for (ModifierHook modifierHook : _modifierHooks.get(card))
                modifierHook.stop();
            _modifierHooks.remove(card);
        }
    }

    @Override
    public void addModifierHooks(PhysicalCard card) {
        CardBlueprint blueprint = card.getBlueprint();
        List<ModifierSource> inPlayModifiers = blueprint.getInPlayModifiers();

        Collection<Modifier> modifiers = new LinkedList<>();

        for (ModifierSource modifierSource : inPlayModifiers) {
            ActionContext context =
                    new DefaultActionContext(card.getOwnerName(), _game, card, null, null);
            modifiers.add(modifierSource.getModifier(context));
        }

        modifiers.addAll(blueprint.getWhileInPlayModifiersNew(card.getOwner(), card));
        _modifierHooks.computeIfAbsent(card, k -> new LinkedList<>());
        for (Modifier modifier : modifiers)
            _modifierHooks.get(card).add(addAlwaysOnModifier(modifier));
    }

    public List<Modifier> getModifiers() {
        List<Modifier> result = new LinkedList<>();
        for (List<Modifier> modifiers : _modifiers.values()) {
            result.addAll(modifiers);
        }
        return result;
    }

}