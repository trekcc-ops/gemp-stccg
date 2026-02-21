
package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.placecard.ShuffleCardsIntoDrawDeckAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.TargetResolverBlueprint;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerResolver;
import com.gempukku.stccg.player.PlayerSource;
import com.gempukku.stccg.player.YouPlayerSource;

import java.util.List;

public class ShuffleCardsIntoDrawDeckSubActionBlueprint implements SubActionBlueprint {

    private final PlayerSource _performingPlayerSource;
    private final TargetResolverBlueprint _cardTarget;

    public ShuffleCardsIntoDrawDeckSubActionBlueprint(
            @JsonProperty(value = "cards")
            TargetResolverBlueprint cardTarget,
                                                          @JsonProperty(value = "player")
                                    String playerText) throws InvalidCardDefinitionException {
        _cardTarget = cardTarget;
        _performingPlayerSource = (playerText == null) ?
                new YouPlayerSource() : PlayerResolver.resolvePlayer(playerText);
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, GameTextContext context) {
        String performingPlayerName = _performingPlayerSource.getPlayerName(cardGame, context);
        ActionCardResolver cardTarget = _cardTarget.getTargetResolver(cardGame, context);
        return List.of(new ShuffleCardsIntoDrawDeckAction(cardGame, context.card(),
                performingPlayerName, cardTarget));
    }

}