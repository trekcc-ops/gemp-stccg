package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;

import java.util.*;

public class ModifiersLogic {

    private final Map<ModifierEffect, List<Modifier>> _modifiers = new EnumMap<>(ModifierEffect.class);
    private final Map<Phase, List<Modifier>> _untilEndOfPhaseModifiers = new EnumMap<>(Phase.class);
    private final Map<String, List<Modifier>> _untilEndOfPlayersNextTurnThisRoundModifiers = new HashMap<>();
    private final Map<Integer, List<Modifier>> _whileThisCardInPlayModifiers = new HashMap<>();
    private final Collection<Modifier> _untilEndOfTurnModifiers = new LinkedList<>();

    public List<Modifier> getAllModifiersByEffect(ModifierEffect modifierEffect) {
        if (_modifiers.get(modifierEffect) == null) {
            return new ArrayList<>();
        } else {
            return _modifiers.get(modifierEffect);
        }
    }

    public void addWhileThisCardInPlayModifiers(Collection<Modifier> modifiers, PhysicalCard card) {
        int cardId = card.getCardId();
        _whileThisCardInPlayModifiers.computeIfAbsent(cardId, cardModifiers -> new LinkedList<>());
        for (Modifier modifier : modifiers) {
            addModifier(modifier);
            _whileThisCardInPlayModifiers.get(cardId).add(modifier);
        }
    }

    public void addAlwaysOnModifier(Modifier modifier) {
        addModifier(modifier);
    }

    public void addUntilEndOfPhaseModifier(Modifier modifier, Phase phase) {
        addModifier(modifier);
        List<Modifier> list = _untilEndOfPhaseModifiers.computeIfAbsent(phase, entry -> new LinkedList<>());
        list.add(modifier);
    }

    public void addUntilEndOfTurnModifier(Modifier modifier) {
        addModifier(modifier);
        _untilEndOfTurnModifiers.add(modifier);
    }

    private void addModifier(Modifier modifier) {
        ModifierEffect modifierEffect = modifier.getModifierType();
        _modifiers.computeIfAbsent(modifierEffect, k -> new ArrayList<>());
        _modifiers.get(modifierEffect).add(modifier);
    }



    private void removeModifiers(Collection<Modifier> modifiers) {
        _modifiers.values().forEach(list -> list.removeAll(modifiers));
    }




    public void signalStartOfTurn(String playerName) {
        List<Modifier> list = _untilEndOfPlayersNextTurnThisRoundModifiers.get(playerName);
        if (list != null) {
            for (Modifier modifier : list) {
                list.remove(modifier);
                _untilEndOfTurnModifiers.add(modifier);
            }
        }
    }


    public void signalEndOfTurn() {
        removeModifiers(_untilEndOfTurnModifiers);
        _untilEndOfTurnModifiers.clear();

        for (List<Modifier> modifiers : _untilEndOfPhaseModifiers.values())
            removeModifiers(modifiers);
        _untilEndOfPhaseModifiers.clear();
    }


    public void signalEndOfRound() {
        for (List<Modifier> modifiers: _untilEndOfPlayersNextTurnThisRoundModifiers.values())
            removeModifiers(modifiers);
        _untilEndOfPlayersNextTurnThisRoundModifiers.clear();
    }


    public void removeWhileThisCardInPlayModifiers(PhysicalCard card) {
        int cardId = card.getCardId();
        if (_whileThisCardInPlayModifiers.get(cardId) != null) {
            for (Modifier modifier : _whileThisCardInPlayModifiers.get(cardId)) {
                removeModifiers(List.of(modifier));
            }
            _whileThisCardInPlayModifiers.remove(cardId);
        }
    }

    public List<Modifier> getAllModifiers() {
        List<Modifier> result = new LinkedList<>();
        for (List<Modifier> modifiers : _modifiers.values()) {
            result.addAll(modifiers);
        }
        return result;
    }

}