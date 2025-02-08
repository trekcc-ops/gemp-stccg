package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;

public class InitializeBoardForPlayerGameEvent extends GameEvent {

    @JsonProperty("allParticipantIds")
    private final String _allParticipantIds;

    @JsonProperty("discardPublic")
    private final boolean _discardPublic;

    public InitializeBoardForPlayerGameEvent(DefaultGame cardGame, Player player) {
        super(Type.PARTICIPANTS, player);
        GameState gameState = cardGame.getGameState();
        _allParticipantIds = String.join(",", (gameState.getPlayerOrder().getAllPlayers()));
        _discardPublic = cardGame.getFormat().discardPileIsPublic();
        _eventAttributes.put(Attribute.allParticipantIds, _allParticipantIds);
        _eventAttributes.put(Attribute.discardPublic, String.valueOf(_discardPublic));
    }

}