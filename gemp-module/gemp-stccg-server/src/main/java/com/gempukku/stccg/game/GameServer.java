package com.gempukku.stccg.game;

import com.gempukku.stccg.AbstractServer;
import com.gempukku.stccg.PrivateInformationException;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.chat.ChatCommandErrorException;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.db.DeckDAO;
import com.gempukku.stccg.db.User;
import com.gempukku.stccg.hall.GameSettings;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameServer extends AbstractServer {

    private static final long MILLIS_TO_MINUTES = 1000 * 60;
    private final CardBlueprintLibrary _CardBlueprintLibrary;

    private final Map<String, CardGameMediator> _runningGames = new ConcurrentHashMap<>();
    private final Set<String> _gameDeathWarningsSent = new HashSet<>();

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

    protected void cleanup() {
        _lock.writeLock().lock();
        try {
            long currentTime = System.currentTimeMillis();

            LinkedHashMap<String, Date> copy = new LinkedHashMap<>(_finishedGamesTime);
            for (Map.Entry<String, Date> finishedGame : copy.entrySet()) {
                String gameId = finishedGame.getKey();
                // 4 minutes
                long _timeToGameDeathWarning = MILLIS_TO_MINUTES * 4;
                if (currentTime > finishedGame.getValue().getTime() + _timeToGameDeathWarning
                        && !_gameDeathWarningsSent.contains(gameId)) {
                    try {
                        _chatServer.getChatRoom(getChatRoomName(gameId)).sendMessage("System", "This game is already finished and will be shortly removed, please move to the Game Hall", true);
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

    private String getChatRoomName(String gameId) {
        return "Game" + gameId;
    }

    public CardGameMediator createNewGame(String tournamentName, final GameParticipant[] participants, GameSettings gameSettings) {
        _lock.writeLock().lock();
        try {
            if (participants.length < 2)
                throw new IllegalArgumentException("There has to be at least two players");
            final String gameId = String.valueOf(_nextGameId);

            if (gameSettings.isCompetitive()) {
                Set<String> allowedUsers = new HashSet<>();
                for (GameParticipant participant : participants)
                    allowedUsers.add(participant.getPlayerId());
                _chatServer.createPrivateChatRoom(getChatRoomName(gameId), false, allowedUsers, 30);
            } else
                _chatServer.createChatRoom(getChatRoomName(gameId), false, 30, false, null);

            // Allow spectators for leagues, but not tournaments
            CardGameMediator cardGameMediator = getCardGameMediator(participants, gameSettings, gameId);
            cardGameMediator.addGameResultListener(
                new GameResultListener() {
                    @Override
                    public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserPlayerIdsWithReasons) {
                        _finishedGamesTime.put(gameId, new Date());
                    }

                    @Override
                    public void gameCancelled() {
                        _finishedGamesTime.put(gameId, new Date());
                    }
                });
            var formatName = gameSettings.getGameFormat().getName();
            cardGameMediator.sendMessageToPlayers("You're starting a game of " + formatName);
            if(formatName.contains("PC")) {
                cardGameMediator.sendMessageToPlayers("""
                        As a reminder, PC formats incorporate the following changes:
                         - <a href="https://wiki.lotrtcgpc.net/wiki/PC_Errata">PC Errata are in effect</a>
                         - Set V1 is legal
                         - Discard piles are public information for both sides
                         - The game ends after Regroup actions are made (instead of at the start of Regroup)
                        """);
            }

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

            final var gameRecordingInProgress = _gameRecorder.recordGame(cardGameMediator, gameSettings.getGameFormat(), tournamentName, decks);
            cardGameMediator.addGameResultListener(
                new GameResultListener() {
                    @Override
                    public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserPlayerIdsWithReasons) {
                        final var loserEntry = loserPlayerIdsWithReasons.entrySet().iterator().next();

                        //potentially this is where to kick off any "reveal deck" events
                        //gameMediator.readoutParticipantDecks();
                        gameRecordingInProgress.finishRecording(winnerPlayerId, winReason, loserEntry.getKey(), loserEntry.getValue());
                    }

                    @Override
                    public void gameCancelled() {
                        gameRecordingInProgress.finishRecording(participants[0].getPlayerId(), "Game cancelled due to error", participants[1].getPlayerId(), "Game cancelled due to error");
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

    private CardGameMediator getCardGameMediator(GameParticipant[] participants, GameSettings gameSettings, String gameId) {
        boolean spectate = (gameSettings.getLeague() != null) ||
                (!gameSettings.isCompetitive() && !gameSettings.isPrivateGame() && !gameSettings.isHiddenGame());

        CardGameMediator cardGameMediator;

        if (Objects.equals(gameSettings.getGameFormat().getGameType(), "tribbles")) {
            cardGameMediator = new TribblesGameMediator(gameId, gameSettings.getGameFormat(), participants,
                    _CardBlueprintLibrary, gameSettings.getTimeSettings(), spectate, gameSettings.isHiddenGame());
        } else if (Objects.equals(gameSettings.getGameFormat().getGameType(), "st1e")){
            cardGameMediator = new ST1EGameMediator(gameId, gameSettings.getGameFormat(), participants,
                    _CardBlueprintLibrary, gameSettings.getTimeSettings(), spectate, gameSettings.isHiddenGame());
        } else if (Objects.equals(gameSettings.getGameFormat().getGameType(), "st2e")){
            cardGameMediator = new ST2EGameMediator(gameId, gameSettings.getGameFormat(), participants,
                    _CardBlueprintLibrary, gameSettings.getTimeSettings(), spectate, gameSettings.isHiddenGame());
        } else {
            throw new RuntimeException("Format '" + gameSettings.getGameFormat().getName() + "' does not belong to 1E, 2E, or Tribbles");
        }
        return cardGameMediator;
    }

    public CardDeck getParticipantDeck(User player, String deckName) {
        return _deckDao.getDeckForPlayer(player, deckName);
    }

    public CardDeck createDeckWithValidate(String deckName, String contents, String targetFormat, String notes) {
        return new CardDeck(deckName, contents, targetFormat, notes);
    }

    public CardGameMediator getGameById(String gameId) {
        _lock.readLock().lock();
        try {
            return _runningGames.get(gameId);
        } finally {
            _lock.readLock().unlock();
        }
    }

}