package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.TargetResolverBlueprint;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerResolver;
import com.gempukku.stccg.player.PlayerSource;
import com.gempukku.stccg.player.YouPlayerSource;

public class PlaceCardsOnBottomOfDrawDeckSubactionBlueprint implements SubActionBlueprint {

    private final PlayerSource _performingPlayerSource;
    private final TargetResolverBlueprint _cardTarget;
    private final boolean _showOpponent;

    public PlaceCardsOnBottomOfDrawDeckSubactionBlueprint(
            @JsonProperty(value = "cards")
            TargetResolverBlueprint cardTarget,
                                                          @JsonProperty(value = "player")
                                    String playerText,
            @JsonProperty(value = "showOpponent", required = true) boolean showOpponent) throws InvalidCardDefinitionException {
        _cardTarget = cardTarget;
        _performingPlayerSource = (playerText == null) ?
                new YouPlayerSource() : PlayerResolver.resolvePlayer(playerText);
        _showOpponent = showOpponent;
    }

    @Override
    public Action createAction(DefaultGame cardGame, GameTextContext context) {
        final String performingPlayerId = _performingPlayerSource.getPlayerName(cardGame, context);
        ActionCardResolver cardTarget = _cardTarget.getTargetResolver(cardGame, context);
        return new PlaceCardsOnBottomOfDrawDeckAction(cardGame, performingPlayerId, cardTarget, _showOpponent);
    }

    @Override
    public boolean isPlayableInFull(DefaultGame cardGame, GameTextContext context) {
        return _cardTarget.canBeResolved(cardGame, context);
    }
}