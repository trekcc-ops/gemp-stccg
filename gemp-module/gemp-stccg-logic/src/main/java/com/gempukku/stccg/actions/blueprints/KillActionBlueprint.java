package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.actions.modifiers.KillSinglePersonnelAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class KillActionBlueprint extends DelayedEffectBlueprint {

    private final CardTargetBlueprint _targetResolver;

    KillActionBlueprint(@JsonProperty(value = "target")
                        CardTargetBlueprint target) {
        _targetResolver = target;
    }

    @Override
    protected List<Action> createActions(CardPerformedAction action, ActionContext context)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        return List.of(
                new KillSinglePersonnelAction(context.getPerformingPlayer(), context.getSource(),
                        _targetResolver.getTargetResolver(context)));
    }

}