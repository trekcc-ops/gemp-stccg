package com.gempukku.stccg.game;

import com.gempukku.stccg.AbstractServer;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.chat.*;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.database.DeckDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.hall.GameSettings;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameServer extends AbstractServer {

    private static final long MILLIS_TO_MINUTES = 1000 * 60;
    private static final int TIMEOUT_PERIOD = 30;
    private final CardBlueprintLibrary _CardBlueprintLibrary;

    private final Map<String, CardGameMediator> _runningGames = new ConcurrentHashMap<>();
    private final Collection<String> _gameDeathWarningsSent = new HashSet<>();

    private final Map<String, Date> _finishedGamesTime = Collections.synchronizedMap(new LinkedHashMap<>());

    private int _nextGameId = 1;

    private final DeckDAO _deckDao;

    private final ChatServer _chatServer;
    private final GameRecorder _gameRecorder;

    private final ReadWriteLock _lock = new ReentrantReadWriteLock();

    public GameServer(DeckDAO deckDao, CardBlueprintLibrary library, ChatServer chatServer, GameRecorder gameRecorder) {
        _deckDao = deckDao;
        _CardBlueprintLibrary = library;
        _chatServer = chatServer;
        _gameRecorder = gameRecorder;
    }

    protected final void cleanup() {
        _lock.writeLock().lock();
        try {
            long currentTime = System.currentTimeMillis();

            Map<String, Date> copy = new LinkedHashMap<>(_finishedGamesTime);
            for (Map.Entry<String, Date> finishedGame : copy.entrySet()) {
                String gameId = finishedGame.getKey();
                // 4 minutes
                long _timeToGameDeathWarning = MILLIS_TO_MINUTES * 4;
                if (currentTime > finishedGame.getValue().getTime() + _timeToGameDeathWarning
                        && !_gameDeathWarningsSent.contains(gameId)) {
                    try {
                        String message = "This game is already finished and will be shortly removed, " +
                                "please move to the Game Hall";
                        ChatRoomMediator chatRoom = _chatServer.getChatRoom(getChatRoomName(gameId));
                        chatRoom.sendMessage(ChatStrings.SYSTEM_USER_ID, message, true);
                    } catch (PrivateInformationException exp) {
                        // Ignore, sent as admin
                    } catch (ChatCommandErrorException e) {
                        // Ignore, no command
                    }
                    _gameDeathWarningsSent.add(gameId);
                }
                // 5 minutes
                long _timeToGameDeath = MILLIS_TO_MINUTES * 5;
                if (currentTime > finishedGame.getValue().getTime() + _timeToGameDeath) {
                    _runningGames.get(gameId).destroy();
                    _gameDeathWarningsSent.remove(gameId);
                    _runningGames.remove(gameId);
                    _chatServer.destroyChatRoom(getChatRoomName(gameId));
                    _finishedGamesTime.remove(gameId);
                } else {
                    break;
                }
            }

            for (CardGameMediator cardGameMediator : _runningGames.values())
                cardGameMediator.cleanup();
        } finally {
            _lock.writeLock().unlock();
        }
    }

    private static String getChatRoomName(String gameId) {
        return "Game" + gameId;
    }

    public final CardGameMediator createNewGame(String tournamentName, final GameParticipant[] participants,
                                                GameSettings gameSettings) {
        _lock.writeLock().lock();
        try {
            if (participants.length < 2)
                throw new IllegalArgumentException("There has to be at least two players");
            final String gameId = String.valueOf(_nextGameId);

            if (gameSettings.isCompetitive()) {
                Set<String> allowedUsers = new HashSet<>();
                for (GameParticipant participant : participants)
                    allowedUsers.add(participant.getPlayerId());
                _chatServer.createPrivateChatRoom(
                        getChatRoomName(gameId), false, allowedUsers, TIMEOUT_PERIOD);
            } else
                _chatServer.createChatRoom(
                        getChatRoomName(gameId), false, TIMEOUT_PERIOD, false);

            // Allow spectators for leagues, but not tournaments
            CardGameMediator cardGameMediator = getCardGameMediator(participants, gameSettings, gameId);
            cardGameMediator.addGameResultListener(
                new GameResultListener() {
                    @Override
                    public void gameFinished(String winnerPlayerId, String winReason,
                                             Map<String, String> loserReasons) {
                        _finishedGamesTime.put(gameId, new Date());
                    }

                    @Override
                    public void gameCancelled() {
                        _finishedGamesTime.put(gameId, new Date());
                    }
                });
            String formatName = gameSettings.getGameFormat().getName();
            cardGameMediator.sendMessageToPlayers("You're starting a game of " + formatName);
            StringBuilder players = new StringBuilder();
            Map<String, CardDeck> decks =  new HashMap<>();
            for (GameParticipant participant : participants) {
                if (!players.isEmpty())
                    players.append(", ");
                String playerId = participant.getPlayerId();
                players.append(playerId);
                decks.put(playerId, participant.getDeck());
            }

            cardGameMediator.sendMessageToPlayers("Players in the game are: " + players);

            final var gameRecordingInProgress =
                    _gameRecorder.recordGame(cardGameMediator, gameSettings.getGameFormat(), tournamentName, decks);
            cardGameMediator.addGameResultListener(
                new GameResultListener() {
                    @Override
                    public void gameFinished(String winnerPlayerId, String winReason,
                                             Map<String, String> loserReasons) {
                        final var loserEntry = loserReasons.entrySet().iterator().next();

                        //potentially this is where to kick off any "reveal deck" events
                        //gameMediator.readoutParticipantDecks();
                        gameRecordingInProgress.finishRecording(
                                winnerPlayerId, winReason, loserEntry.getKey(), loserEntry.getValue());
                    }

                    @Override
                    public void gameCancelled() {
                        gameRecordingInProgress.finishRecording(participants[0].getPlayerId(),
                                "Game cancelled due to error", participants[1].getPlayerId(),
                                "Game cancelled due to error");
                    }
                }
            );

            _runningGames.put(gameId, cardGameMediator);
            _nextGameId++;
            return cardGameMediator;
        } finally {
            _lock.writeLock().unlock();
        }
    }

    private CardGameMediator getCardGameMediator(GameParticipant[] participants, GameSettings gameSettings,
                                                 String gameId) {

        GameFormat gameFormat = gameSettings.getGameFormat();

        return switch (gameFormat.getGameType()) {
            case FIRST_EDITION -> new ST1EGameMediator(gameId, participants, _CardBlueprintLibrary, gameSettings);
            case SECOND_EDITION -> new ST2EGameMediator(gameId, participants, _CardBlueprintLibrary, gameSettings);
            case TRIBBLES -> new TribblesGameMediator(gameId, participants, _CardBlueprintLibrary, gameSettings);
        };
    }

    public final CardDeck getParticipantDeck(User player, String deckName) {
        return _deckDao.getDeckForPlayer(player, deckName);
    }

    public final CardGameMediator getGameById(String gameId) throws HttpProcessingException {
        CardGameMediator mediator;
        _lock.readLock().lock();
        try {
            mediator = _runningGames.get(gameId);
        } finally {
            _lock.readLock().unlock();
        }
        if (mediator == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        else return mediator;
    }

    public void cancelGame(User user, String gameId) throws HttpProcessingException {
        CardGameMediator gameMediator = getGameById(gameId);
        gameMediator.cancel(user);
    }

    public void setPlayerAutoPassSettings(User resourceOwner, String gameId, Set<Phase> autoPassPhases)
            throws HttpProcessingException {
        CardGameMediator gameMediator = getGameById(gameId);
        gameMediator.setPlayerAutoPassSettings(resourceOwner, autoPassPhases);
    }
}