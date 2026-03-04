
package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerResolver;
import com.gempukku.stccg.player.PlayerSource;
import com.gempukku.stccg.requirement.Requirement;

import java.util.Collection;
import java.util.Objects;

public class SelectCardSubActionBlueprint implements SubActionBlueprint {

    private final FilterBlueprint _filterBlueprint;
    private final Requirement _requirement;
    private final String _saveToMemoryId;
    private final PlayerSource _selectingPlayer;

    @JsonCreator
    private SelectCardSubActionBlueprint(
            @JsonProperty(value = "filter", required = true) FilterBlueprint filterBlueprint,
            @JsonProperty("requires") Requirement requirementToInitiateSelection,
            @JsonProperty("selectingPlayer") String playerText,
            @JsonProperty(value = "saveToMemoryId", required = true) String saveToMemoryId) throws InvalidCardDefinitionException {
        _filterBlueprint = filterBlueprint;
        _requirement = requirementToInitiateSelection;
        _saveToMemoryId = Objects.requireNonNullElse(saveToMemoryId, "temp");
        _selectingPlayer = PlayerResolver.resolvePlayer(playerText);
    }

    @Override
    public Action createAction(DefaultGame cardGame, GameTextContext context) {
        CardFilter filter = _filterBlueprint.getFilterable(cardGame, context);
        Collection<PhysicalCard> filteredCards = Filters.filter(cardGame, filter);
        String performingPlayer = _selectingPlayer.getPlayerName(cardGame, context);
        return new SelectVisibleCardAction(
                cardGame, performingPlayer, "Select a card", filteredCards, context, _saveToMemoryId);
    }

    @Override
    public boolean isPlayableInFull(DefaultGame cardGame, GameTextContext context) {
        return _requirement == null || _requirement.accepts(context, cardGame);
    }

}