package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.choose.RevealHandAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerResolver;
import com.gempukku.stccg.player.PlayerSource;

public class RevealHandActionBlueprint implements SubActionBlueprint {

    private final PlayerSource _playerSource;
    private final String _saveToMemoryId;

    @JsonCreator
    private RevealHandActionBlueprint(@JsonProperty("handPlayer") String playerText,
                                      @JsonProperty("saveToMemoryId") String saveToMemoryId)
            throws InvalidCardDefinitionException {
        _saveToMemoryId = saveToMemoryId;
        _playerSource = PlayerResolver.resolvePlayer(playerText);
    }
    @Override
    public Action createAction(DefaultGame cardGame, GameTextContext context) {
        String handPlayer = _playerSource.getPlayerName(cardGame, context);
        if (_saveToMemoryId != null) {
            context.setCardMemory(_saveToMemoryId, cardGame.getGameState().getZoneCards(handPlayer, Zone.HAND));
        }
        return new RevealHandAction(cardGame, _playerSource.getPlayerName(cardGame, context));
    }
}