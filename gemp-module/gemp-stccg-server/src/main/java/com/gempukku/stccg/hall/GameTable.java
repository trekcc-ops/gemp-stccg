package com.gempukku.stccg.hall;

import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameParticipant;
import com.gempukku.stccg.game.GameResultListener;
import com.gempukku.stccg.game.GameServer;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class GameTable {
    private static final Logger LOGGER = LogManager.getLogger(GameTable.class);

    private final GameSettings gameSettings;
    private final Map<String, GameParticipant> players = new HashMap<>();
    private final int _tableId;

    private CardGameMediator cardGameMediator;
    private final int capacity;
    private TableStatus _tableStatus;

    enum TableStatus {
        WAITING, PLAYING, FINISHED
    }

    public GameTable(int tableId, GameSettings gameSettings, GameParticipant... participants) {
        this.gameSettings = gameSettings;
        this.capacity = 2; // manually change Tribbles player limit
        _tableId = tableId;
        _tableStatus = TableStatus.WAITING;
        LOGGER.debug("Capacity of game: {}", this.capacity);
        for (GameParticipant participant : participants) {
            addPlayer(participant);
        }
    }

    public final void startGame(CardGameMediator cardGameMediator) {
        LOGGER.debug("GameTable - startGame function called;");
        this.cardGameMediator = cardGameMediator;
        cardGameMediator.startGame();
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

    private void createGame(GameServer gameServer, CardBlueprintLibrary cardLibrary, ServerObjects serverObjects) {
        Set<GameParticipant> players = getPlayers();
        GameParticipant[] participants = players.toArray(new GameParticipant[0]);
        String tournamentName = gameSettings.getTournamentNameForHall();

        List<GameResultListener> listenerList = new ArrayList<>();
        listenerList.add(new NotifyHallListenersGameResultListener(serverObjects));
        if (isForLeague()) {
            listenerList.add(new LeagueGameResultListener(gameSettings, serverObjects));
        }
            gameServer.createNewGame(tournamentName, participants, this, cardLibrary, listenerList);
    }

    public void createGameIfFull(ServerObjects serverObjects) {
        if (isFull()) {
            createGame(serverObjects.getGameServer(), serverObjects.getCardBlueprintLibrary(), serverObjects);
        }
    }

    public void validateOpponentForLeague(String userName, LeagueService leagueService) throws HallException {
        League league = gameSettings.getLeague();
        if (league != null) {
            if (!getPlayerNames().isEmpty() &&
                    !leagueService.canPlayRankedGameAgainst(league, gameSettings.getSeriesData(),
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