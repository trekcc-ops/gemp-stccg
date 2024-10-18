package com.gempukku.stccg.game;

import com.gempukku.stccg.async.handler.HTMLUtils;
import com.gempukku.stccg.database.DBData;
import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.database.PlayerDAO;
import com.gempukku.stccg.gamestate.GameEvent;
import com.gempukku.stccg.hall.GameTimer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class GameRecorder {
    private final GameHistoryService _gameHistoryService;
    private final PlayerDAO _playerDAO;

    public GameRecorder(GameHistoryService gameHistoryService, PlayerDAO playerDAO) {
        _gameHistoryService = gameHistoryService;
        _playerDAO = playerDAO;
    }


    public final GameRecordingInProgress recordGame(CardGameMediator game, GameFormat format,
                                                    final String tournamentName,
                                                    final Map<String, ? extends CardDeck> decks) {
        final ZonedDateTime startDate = ZonedDateTime.now(ZoneOffset.UTC);
        final Map<String, GameCommunicationChannel> recordingChannels = new HashMap<>();
        for (String playerId : game.getPlayersPlaying()) {
            var recordChannel = new GameCommunicationChannel(game.getGame(), playerId, 0);
            game.addGameStateListener(playerId, recordChannel);
            recordingChannels.put(playerId, recordChannel);
        }

        return (winnerName, winReason, loserName, loseReason) -> {
            final ZonedDateTime endDate = ZonedDateTime.now(ZoneOffset.UTC);

            var time = game.getTimeSettings();
            var clocks = game.getPlayerClocks();
            var gameInfo = new MyGameHistory(game, winnerName, loserName, winReason, loseReason, startDate, endDate,
                    format, decks, tournamentName, time, clocks);

            Map<String, String> playerRecordingId = saveRecordedChannels(recordingChannels, gameInfo, decks);
            gameInfo.id = _gameHistoryService.addGameHistory(gameInfo);

            if(format.isPlaytest())
            {
                game.sendMessageToPlayers(HTMLUtils.getPlayTestMessage(playerRecordingId, winnerName, loserName));
            }

        };
    }

    public interface GameRecordingInProgress {
        void finishRecording(String winner, String winReason, String loser, String loseReason);
    }

    private static File getRecordingFileVersion0(String playerId, String gameId) {
        File playerReplayFolder = new File(AppConfig.getReplayPath(), playerId);
        return new File(playerReplayFolder, gameId + ".xml.gz");
    }

    private static File getRecordingFileVersion1(String playerId, String gameId,
                                                 ChronoZonedDateTime<LocalDate> startDate) {
        //This dumb-ass formatting output is because anything that otherwise interacts with the
        // year subfield appears to trigger a JVM segfault in the guts of the java ecosystem.
        // Super-dumb.  Don't touch these two lines.
        var year = startDate.format(DateTimeFormatter.ofPattern("yyyy"));
        var month = startDate.format(DateTimeFormatter.ofPattern("MM"));

        var yearFolder = new File(AppConfig.getReplayPath(), year);
        var monthFolder = new File(yearFolder, month);
        var playerReplayFolder = new File(monthFolder, playerId);
        return new File(playerReplayFolder, gameId + ".xml.gz");
    }

    private static File getSummaryFile(DBData.GameHistory history) {
        var summaryFolder = new File(AppConfig.getReplayPath(), "summaries");
        var yearFolder = new File(summaryFolder, String.format("%04d", history.start_date.getYear()));
        var monthFolder = new File(yearFolder, String.format("%02d", history.start_date.getMonthValue()));
        //noinspection ResultOfMethodCallIgnored
        monthFolder.mkdirs();
        return new File(monthFolder, history.winner + "_vs_" + history.loser + "_" +
                history.win_recording_id + "_" + history.lose_recording_id+ ".json");
    }

    private static OutputStream getRecordingWriteStream(String playerId, String gameId,
                                                        ChronoZonedDateTime<LocalDate> startDate) throws IOException {
        File recordingFile = getRecordingFileVersion1(playerId, gameId, startDate);
        //noinspection ResultOfMethodCallIgnored
        recordingFile.getParentFile().mkdirs();

        Deflater deflater = new Deflater(9);
        return new DeflaterOutputStream(new FileOutputStream(recordingFile), deflater);
    }

    private Map<String, String> saveRecordedChannels(Map<String, GameCommunicationChannel> gameProgress,
                                                     DBData.GameHistory gameInfo,
                                                     Map<String, ? extends CardDeck> decks) {
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, GameCommunicationChannel> playerRecordings : gameProgress.entrySet()) {
            String playerId = playerRecordings.getKey();
            String recordingId;
            if(playerId.equals(gameInfo.winner)) {
                recordingId = gameInfo.win_recording_id;
            }
            else {
                recordingId = gameInfo.lose_recording_id;
            }

            try {
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

                var info = doc.createElement("info");

                info.setAttribute("replay_version", String.valueOf(gameInfo.replay_version));
                info.setAttribute("winner", gameInfo.winner);
                info.setAttribute("loser", gameInfo.loser);
                info.setAttribute("win_reason", gameInfo.win_reason);
                info.setAttribute("lose_reason", gameInfo.lose_reason);
                info.setAttribute("start_date", gameInfo.start_date.toString());
                info.setAttribute("end_date", gameInfo.end_date.toString());
                info.setAttribute("format", gameInfo.format_name);
                info.setAttribute("tournament", gameInfo.tournament);

                for(var pair : decks.entrySet()) {
                    String player = pair.getKey();
                    var deck = pair.getValue();

                    var deckElement = doc.createElement("deckReadout");
                    deckElement.setAttribute("playerId", player);
                    deckElement.setAttribute("name", deck.getDeckName());
                    deckElement.setAttribute("deck", String.join(",", deck.getDrawDeckCards()));

                    info.appendChild(deckElement);
                }

                Element gameReplay = doc.createElement("gameReplay");
                gameReplay.appendChild(info);

                final List<GameEvent> gameEvents = playerRecordings.getValue().consumeGameEvents();
                ReplayMetadata metadata = new ReplayMetadata(gameInfo, decks, playerId, gameEvents);

                for (GameEvent gameEvent : gameEvents)
                    gameReplay.appendChild(gameEvent.serialize(doc));

                doc.appendChild(gameReplay);

                try(var out = new PrintWriter(getSummaryFile(gameInfo).getAbsolutePath())) {
                    out.println(JsonUtils.toJsonString(metadata));
                }

                try (OutputStream replayStream = getRecordingWriteStream(playerId, recordingId, gameInfo.start_date)) {
                    // Prepare the DOM document for writing
                    Source source = new DOMSource(doc);
                    // Prepare the output file
                    Result streamResult = new StreamResult(replayStream);
                    // Write the DOM document to the file
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.transform(source, streamResult);
                }
                result.put(playerId, recordingId);

            } catch (Exception ignored) {

            }

        }
        return result;
    }

    public final InputStream getRecordedGame(String playerId, String recordId) throws IOException {
        var history = _gameHistoryService.getGameHistory(recordId);

        if(history == null)
            return null;

        File recordingFile = null;

        if(history.replay_version == 0) {
            recordingFile = getRecordingFileVersion0(playerId, recordId);
        }
        else if(history.replay_version == 1) {
            recordingFile = getRecordingFileVersion1(playerId, recordId, history.start_date);
        }

        if (recordingFile == null || !recordingFile.exists() || !recordingFile.isFile())
            return null;

        return new InflaterInputStream(new FileInputStream(recordingFile));
    }

    private class MyGameHistory extends DBData.GameHistory {
        public MyGameHistory(CardGameMediator game, String winnerName, String loserName, String winReason,
                             String loseReason, ZonedDateTime startDate, ZonedDateTime endDate, GameFormat format,
                             Map<String, ? extends CardDeck> decks, String tournamentName, GameTimer time,
                             Map<String, Integer> clocks) {
            gameId = game.getGameId();

            winner = winnerName;
            winnerId = _playerDAO.getPlayer(winnerName).getId();
            loser = loserName;
            loserId = _playerDAO.getPlayer(loserName).getId();

            win_reason = winReason;
            lose_reason = loseReason;

            win_recording_id = getNewRecordingID();
            lose_recording_id = getNewRecordingID();

            start_date = startDate;
            end_date = endDate;

            format_name = format.getName();

            winner_deck_name = decks.get(winner).getDeckName();
            loser_deck_name = decks.get(loser).getDeckName();

            tournament = tournamentName;

            winner_site = 0;
            loser_site = 0;

            game_length_type = time.name();
            max_game_time = time.maxSecondsPerPlayer();
            game_timeout = time.maxSecondsPerDecision();
            winner_clock_remaining = clocks.getOrDefault(winnerName, -1);
            loser_clock_remaining = clocks.getOrDefault(loserName, -1);

            //Update this version as needed; note that this is the REPLAY FORMAT, not the JSON summary
            replay_version = 1;

        }

        private String getNewRecordingID() {
            String id;
            do {
                StringBuilder sb = new StringBuilder();
                final String possibleCharacters =
                        TextUtils.getAllCharacters(false, false);
                int idLength = 16;
                Random rnd = ThreadLocalRandom.current();
                for (int i = 0; i < idLength; i++)
                    sb.append(possibleCharacters.charAt(rnd.nextInt(possibleCharacters.length())));
                id = sb.toString();
            } while (_gameHistoryService.doesReplayIDExist(id));
            return id;
        }

    }
}