package com.gempukku.stccg.hall;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueService;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GameTable {

    private static final AtomicInteger nextId = new AtomicInteger(0);
    private final int _tableId = nextId.incrementAndGet();

    private final GameSettings gameSettings;
    private final Map<String, GameParticipant> players = new HashMap<>();

    private CardGameMediator cardGameMediator;
    private final int capacity;
    private TableStatus _tableStatus;

    enum TableStatus {
        WAITING, PLAYING, FINISHED
    }

    public GameTable(GameSettings gameSettings, GameParticipant... participants) {
        this.gameSettings = gameSettings;
        this.capacity = 2; // manually change Tribbles player limit
        _tableStatus = TableStatus.WAITING;
        for (GameParticipant participant : participants) {
            addPlayer(participant);
        }
    }

    public CardGameMediator createMediator(CardBlueprintLibrary cardBlueprintLibrary,
                                           String tournamentName, List<GameCreationListener> creationListeners,
                                           List<GameResultListener> resultListeners) {
        GameParticipant[] participants = getPlayers().toArray(new GameParticipant[0]);
        if (participants.length < 2)
            throw new IllegalArgumentException("There has to be at least two players");
        CardGameMediator cardGameMediator = new CardGameMediator(participants, cardBlueprintLibrary,
                gameSettings.allowsSpectators(), gameSettings.getTimeSettings(), gameSettings.getGameFormat(),
                gameSettings.getGameType(), gameSettings.isCompetitive(), tournamentName);
        for (GameCreationListener listener : creationListeners) {
            listener.process(cardGameMediator);
        }
        cardGameMediator.initialize(resultListeners);
        this.cardGameMediator = cardGameMediator;
        cardGameMediator.startGame();
        return cardGameMediator;
    }

    public final CardGameMediator getMediator() {
        return cardGameMediator;
    }

    public final boolean wasGameStarted() {
        return cardGameMediator != null;
    }

    public final void addPlayer(GameParticipant player) {
        players.put(player.getPlayerId(), player);
    }

    public final void removePlayer(String playerId) {
        players.remove(playerId);
    }

    public final boolean isEmpty() {
        return players.isEmpty();
    }

    public final boolean hasPlayer(String playerId) {
        return players.containsKey(playerId);
    }
    public final boolean hasPlayer(User user) { return hasPlayer(user.getName()); }

    public final List<String> getPlayerNames() {
        return new LinkedList<>(players.keySet());
    }

    public final Set<GameParticipant> getPlayers() {
        return Set.copyOf(players.values());
    }

    public final GameSettings getGameSettings() {
        return gameSettings;
    }

    boolean isWatchableToUser(User user) {
        return switch(_tableStatus) {
            case WAITING, FINISHED -> false;
            case PLAYING -> user.isAdmin() || gameSettings.allowsSpectators();
        };
    }

    public void setAsPlaying() { _tableStatus = TableStatus.PLAYING; }
    public void setAsFinished() { _tableStatus = TableStatus.FINISHED; }

    public int getTableId() { return _tableId; }

    public boolean isFull() {
        return players.size() == capacity;
    }

    boolean isForLeague() {
        return gameSettings.getLeague() != null;
    }

    public boolean isInLeague(League league) {
        return league != null && Objects.equals(gameSettings.getLeague(), league);
    }

    public void createGameIfFull(GameServer gameServer, HallServer hallServer, LeagueService leagueService) {
        if (isFull()) {
            String tournamentName = gameSettings.getTournamentNameForHall();
            List<GameResultListener> listenerList = new ArrayList<>();
            listenerList.add(new NotifyHallListenersGameResultListener(hallServer));
            if (isForLeague()) {
                listenerList.add(new LeagueGameResultListener(gameSettings, leagueService));
            }
            createNewGame(gameServer, tournamentName, listenerList);
        }
    }

    public void createTournamentGameInternal(GameServer gameServer, List<GameResultListener> listeners,
                                             String tournamentName) {
        createNewGame(gameServer, tournamentName, listeners);
    }

    private void createNewGame(GameServer server, String gameName, List<GameResultListener> listeners) {
        server.createNewGame(gameName, this, listeners);
    }


    public void validateOpponentForLeague(String userName, LeagueService leagueService) throws HallException {
        League league = gameSettings.getLeague();
        if (league != null) {
            if (!getPlayerNames().isEmpty() &&
                    !leagueService.canPlayRankedGameAgainst(league, gameSettings.getSeries(),
                            getPlayerNames().getFirst(), userName))
                throw new HallException(
                        "You have already played ranked league game against this player in that series");
        }
    }

    public boolean isGameFinished() {
        return cardGameMediator != null && cardGameMediator.isFinished();
    }

    public boolean playerIsPlayingForLeague(String userName, League league) {
        return hasPlayer(userName) && isInLeague(league) && _tableStatus == TableStatus.PLAYING;
    }

    public String getGameId() {
        return (cardGameMediator == null) ? null : cardGameMediator.getGameId();
    }

    TableStatus getStatus() { return _tableStatus; }


}