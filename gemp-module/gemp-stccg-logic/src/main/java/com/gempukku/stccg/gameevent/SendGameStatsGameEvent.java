package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Collection;
import java.util.Map;

public class SendGameStatsGameEvent extends GameEvent {

    private final Map<String, Map<Zone, Integer>> _playerZoneSizes;
    private final Collection<Player> _players;

    public SendGameStatsGameEvent(DefaultGame cardGame) {
        super(cardGame, Type.GAME_STATS);
        _playerZoneSizes = cardGame.getZoneSizes();
        _players = cardGame.getPlayers();
    }

    public Node serialize(Document doc) {
        Element eventElem = doc.createElement("ge");

        for (Attribute attribute : _eventAttributes.keySet()) {
            if (getAttribute(attribute) != null)
                eventElem.setAttribute(attribute.name(), getAttribute(attribute));
        }
        serializeGameStats(doc, eventElem);
        return eventElem;
    }

    private void serializeGameStats(Document doc, Element eventElem) {
        for (Map.Entry<String, Map<Zone, Integer>> playerZoneSizes : _playerZoneSizes.entrySet()) {
            final Element playerZonesElem = doc.createElement("playerZones");

            playerZonesElem.setAttribute("name", playerZoneSizes.getKey());

            for (Map.Entry<Zone, Integer> zoneSizes : playerZoneSizes.getValue().entrySet())
                playerZonesElem.setAttribute(zoneSizes.getKey().name(), zoneSizes.getValue().toString());

            eventElem.appendChild(playerZonesElem);
        }

        for (Player player : _players) {
            final Element playerScoreElem = doc.createElement("playerScores");
            playerScoreElem.setAttribute("name", player.getPlayerId());
            playerScoreElem.setAttribute("score", String.valueOf(player.getScore()));
            eventElem.appendChild(playerScoreElem);
        }

        StringBuilder charStr = new StringBuilder();
        if (!charStr.isEmpty())
            charStr.delete(0, 1);

        if (!charStr.isEmpty())
            eventElem.setAttribute("charStats", charStr.toString());
    }


}