package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.CardTargetBlueprint;
import com.gempukku.stccg.actions.playcard.DownloadCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.filters.PlayableFilterBlueprint;
import com.gempukku.stccg.filters.YouCanDownloadFilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.modifiers.ModifierFlag;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class DownloadActionBlueprint extends DelayedEffectBlueprint {

    private final String _saveToMemoryId;
    private final CardTargetBlueprint _cardTarget;

    DownloadActionBlueprint(@JsonProperty(value = "saveToMemoryId")
                            String saveToMemoryId,
                            @JsonProperty(value = "target")
                            CardTargetBlueprint cardTarget) {
        _cardTarget = cardTarget;
        _cardTarget.addFilter(new YouCanDownloadFilterBlueprint());
        _saveToMemoryId = (saveToMemoryId == null) ? "_temp" : saveToMemoryId;
    }

    @Override
    protected List<Action> createActions(CardPerformedAction action, ActionContext actionContext)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        Action downloadAction = new DownloadCardAction(actionContext.getGame(), actionContext.getPerformingPlayer(),
                _cardTarget.getTargetResolver(actionContext));
        return List.of(downloadAction);
    }
}