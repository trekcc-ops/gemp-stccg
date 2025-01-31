package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;

import java.util.Objects;

public class PlayedTriggerChecker implements TriggerChecker {

    private final FilterBlueprint _filter;
    private final FilterBlueprint _onFilter;
    private final String _saveToMemoryId;
    private final PlayerSource _playingPlayer;

    PlayedTriggerChecker(
            @JsonProperty(value = "filter", required = true)
            FilterBlueprint filter,
            @JsonProperty("on")
            FilterBlueprint onFilter,
            @JsonProperty("memorize")
            String memorize,
            @JsonProperty("player")
            String playerText
    ) throws InvalidCardDefinitionException {
        _playingPlayer = PlayerResolver.resolvePlayer(Objects.requireNonNullElse(playerText, "you"));
        _filter = filter;
        _onFilter = onFilter;
        _saveToMemoryId = Objects.requireNonNullElse(memorize, "_temp");
    }
    @Override
    public boolean accepts(ActionContext actionContext) {
        try {
            final Filterable filterable = _filter.getFilterable(actionContext);
            final String playingPlayerId = _playingPlayer.getPlayerId(actionContext);
            final ActionResult actionResult = actionContext.getEffectResult();
            final boolean played;

            if (_onFilter != null) {
                final Filterable onFilterable = _onFilter.getFilterable(actionContext);
                played = playedOn(actionContext.getGame(), actionResult, onFilterable, filterable);
            } else {
                played = played(actionContext.getGame(),
                        actionContext.getGame().getPlayer(playingPlayerId), actionResult, filterable);
            }

            if (played && _saveToMemoryId != null)
                actionContext.setCardMemory(_saveToMemoryId, ((PlayCardResult) actionResult).getPlayedCard());
            return played;
        } catch(PlayerNotFoundException exp) {
            actionContext.getGame().sendErrorMessage(exp);
            return false;
        }
    }

    private static boolean playedOn(DefaultGame game, ActionResult actionResult,
                                   Filterable targetFilter, Filterable... filters) {
        if (actionResult.getType() == ActionResult.Type.PLAY_CARD) {
            final PlayCardResult playResult = (PlayCardResult) actionResult;
            final PhysicalCard attachedTo = playResult.getAttachedTo();
            if (attachedTo == null)
                return false;
            PhysicalCard playedCard = playResult.getPlayedCard();
            return Filters.and(filters).accepts(game, playedCard)
                    && Filters.and(targetFilter).accepts(game, attachedTo);
        }
        return false;
    }

    private static boolean played(DefaultGame game, Player player, ActionResult actionResult, Filterable... filters) {
        if (actionResult.getType() == ActionResult.Type.PLAY_CARD) {
            if (actionResult.getPerformingPlayerId().equals(player.getPlayerId())) {
                PhysicalCard playedCard = ((PlayCardResult) actionResult).getPlayedCard();
                return Filters.and(filters).accepts(game, playedCard);
            }
        }
        return false;
    }



}