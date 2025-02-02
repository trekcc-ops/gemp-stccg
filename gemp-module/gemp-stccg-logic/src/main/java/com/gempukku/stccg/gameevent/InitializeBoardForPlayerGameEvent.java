package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;

public class InitializeBoardForPlayerGameEvent extends GameEvent {

    @JacksonXmlProperty(localName = "allParticipantIds", isAttribute = true)
    private final String _allParticipantIds;

    @JacksonXmlProperty(localName = "discardPublic", isAttribute = true)
    private final String _discardPublic;

    public InitializeBoardForPlayerGameEvent(DefaultGame cardGame, Player player) {
        super(Type.PARTICIPANTS, player);
        GameState gameState = cardGame.getGameState();
        _allParticipantIds = String.join(",", (gameState.getPlayerOrder().getAllPlayers()));
        _discardPublic = String.valueOf(cardGame.getFormat().discardPileIsPublic());
    }

}