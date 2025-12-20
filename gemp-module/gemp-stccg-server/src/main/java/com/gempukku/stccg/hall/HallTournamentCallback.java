package com.gempukku.stccg.hall;

import com.gempukku.stccg.chat.ChatCommandErrorException;
import com.gempukku.stccg.chat.PrivateInformationException;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.GameParticipant;
import com.gempukku.stccg.game.GameResultListener;
import com.gempukku.stccg.game.GameServer;
import com.gempukku.stccg.tournament.Tournament;
import com.gempukku.stccg.tournament.TournamentCallback;

import java.util.Map;

public class HallTournamentCallback implements TournamentCallback {
    private final HallServer _hallServer;
    private final GameServer _gameServer;
    private final Tournament _tournament;
    private final GameSettings tournamentGameSettings;

    HallTournamentCallback(HallServer hallServer, GameServer gameServer, Tournament tournament,
                           GameFormat tournamentFormat) {
        _hallServer = hallServer;
        _gameServer = gameServer;
        _tournament = tournament;
        tournamentGameSettings =
                new GameSettings(tournamentFormat, null, null, true, false,
                        false, false, GameTimer.TOURNAMENT_TIMER, null);
    }

    @Override
    public final void createGame(String playerOne, CardDeck deckOne, String playerTwo, CardDeck deckTwo) {
        final GameParticipant[] participants = new GameParticipant[2];
        participants[0] = new GameParticipant(playerOne, deckOne);
        participants[1] = new GameParticipant(playerTwo, deckTwo);
        createGameInternal(participants);
    }

    private void createGameInternal(final GameParticipant[] participants) {
        GameResultListener listener = new MyGameResultListener(participants);
        _hallServer.createTournamentGameInternal(_gameServer,
                tournamentGameSettings, participants, _tournament.getTournamentName(), listener);
    }

    @Override
    public final void broadcastMessage(String message) {
        try {
            _hallServer.sendAdminMessage("TournamentSystem", message);
        } catch (PrivateInformationException exp) {
            // Ignore, sent as admin
        } catch (ChatCommandErrorException e) {
            // Ignore, no command
        }
    }

    private class MyGameResultListener implements GameResultListener {
        private final GameParticipant[] participants;

        public MyGameResultListener(GameParticipant[] participants) {
            this.participants = participants;
        }

        @Override
        public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserReasons) {
            _tournament.reportGameFinished(winnerPlayerId, loserReasons.keySet().iterator().next());
        }

        @Override
        public void gameCancelled() {
            createGameInternal(participants);
        }
    }
}