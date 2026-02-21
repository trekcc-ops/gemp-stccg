package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.placecard.PlaceCardOnTopOfDrawDeckAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.TargetResolverBlueprint;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerResolver;
import com.gempukku.stccg.player.PlayerSource;
import com.gempukku.stccg.player.YouPlayerSource;

import java.util.List;

public class PlaceCardsOnTopOfDrawDeckSubactionBlueprint implements SubActionBlueprint {

    private final PlayerSource _performingPlayerSource;
    private final TargetResolverBlueprint _cardTarget;

    public PlaceCardsOnTopOfDrawDeckSubactionBlueprint(
            @JsonProperty(value = "target")
            TargetResolverBlueprint cardTarget,
                                                          @JsonProperty(value = "player")
                                    String playerText) throws InvalidCardDefinitionException {
        _cardTarget = cardTarget;
        _performingPlayerSource = (playerText == null) ?
                new YouPlayerSource() : PlayerResolver.resolvePlayer(playerText);
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, GameTextContext context)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        final String performingPlayerId = _performingPlayerSource.getPlayerName(cardGame, context);
        ActionCardResolver cardTarget = _cardTarget.getTargetResolver(cardGame, context);
        return List.of(new PlaceCardOnTopOfDrawDeckAction(cardGame, performingPlayerId, cardTarget));
    }

    @Override
    public boolean isPlayableInFull(DefaultGame cardGame, GameTextContext context) {
        return _cardTarget.canBeResolved(cardGame, context);
    }
}