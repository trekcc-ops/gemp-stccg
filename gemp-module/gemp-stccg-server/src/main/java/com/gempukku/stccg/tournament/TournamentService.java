package com.gempukku.stccg.tournament;

import com.gempukku.stccg.async.LoggingProxy;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.database.DbAccess;
import com.gempukku.stccg.database.DbTournamentDAO;
import com.gempukku.stccg.database.DbTournamentMatchDAO;
import com.gempukku.stccg.database.DbTournamentPlayerDAO;
import com.gempukku.stccg.formats.FormatLibrary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


public class TournamentService {
    private static final Logger LOGGER = LogManager.getLogger(TournamentService.class);
    private final TournamentDAO _tournamentDao;
    private final TournamentPlayerDAO _tournamentPlayerDao;
    private final TournamentMatchDAO _tournamentMatchDao;
    private final CardBlueprintLibrary _library;
    private final Map<String, Tournament> _tournamentById = new HashMap<>();
    private final FormatLibrary _formatLibrary;

    public TournamentService(CardBlueprintLibrary library, FormatLibrary formatLibrary, DbAccess dbAccess) {
        _tournamentDao =
                LoggingProxy.createLoggingProxy(TournamentDAO.class, new DbTournamentDAO(dbAccess));
        _tournamentPlayerDao =
                LoggingProxy.createLoggingProxy(TournamentPlayerDAO.class, new DbTournamentPlayerDAO(dbAccess));
        _tournamentMatchDao =
                LoggingProxy.createLoggingProxy(TournamentMatchDAO.class, new DbTournamentMatchDAO(dbAccess));
        _library = library;
        _formatLibrary = formatLibrary;
    }


    public void clearCache() {
        _tournamentById.clear();
    }

    
    public void addPlayer(String tournamentId, String playerName, CardDeck deck) {
        _tournamentPlayerDao.addPlayer(tournamentId, playerName, deck);
    }

    
    public void dropPlayer(String tournamentId, String playerName) {
        _tournamentPlayerDao.dropPlayer(tournamentId, playerName);
    }

    
    public Set<String> getPlayers(String tournamentId) {
        return _tournamentPlayerDao.getPlayers(tournamentId);
    }

    
    public Map<String, CardDeck> getPlayerDecks(String tournamentId, String format) {
        return _tournamentPlayerDao.getPlayerDecks(tournamentId, format);
    }

    
    public Set<String> getDroppedPlayers(String tournamentId) {
        return _tournamentPlayerDao.getDroppedPlayers(tournamentId);
    }

    
    public CardDeck getPlayerDeck(String tournamentId, String player, String format) {
        return _tournamentPlayerDao.getPlayerDeck(tournamentId, player, format);
    }

    
    public void addMatch(String tournamentId, int round, String playerOne, String playerTwo) {
        _tournamentMatchDao.addMatch(tournamentId, round, playerOne, playerTwo);
    }

    
    public void setMatchResult(String tournamentId, String winner) {
        _tournamentMatchDao.setMatchResult(tournamentId, winner);
    }


    public List<TournamentMatch> getMatches(String tournamentId) {
        return _tournamentMatchDao.getMatches(tournamentId);
    }

    public Tournament addTournament(TournamentQueue queue, String tournamentId, String tournamentName) {
        String format = queue.getFormatCode();
        CollectionType collectionType = queue.getCollectionType();
        Tournament.Stage stage = queue.getStage();
        String pairingMechanism = queue.getPairingRegistryRepresentation();
        String prizeScheme = queue.getPrizesRegistryRepresentation();
        _tournamentDao.addTournament(tournamentId, null, tournamentName, format, collectionType, stage,
                pairingMechanism, prizeScheme, new Date());
        return createTournamentAndStoreInCache(new TournamentInfo(tournamentId, tournamentName, format, collectionType, stage,
                pairingMechanism, prizeScheme, 0), _library);
    }


    public void updateTournamentStage(String tournamentId, Tournament.Stage stage) {
        _tournamentDao.updateTournamentStage(tournamentId, stage);
    }

    
    public void updateTournamentRound(String tournamentId, int round) {
        _tournamentDao.updateTournamentRound(tournamentId, round);
    }

    
    public List<Tournament> getOldTournaments(long since, CardBlueprintLibrary cardLibrary) {
        List<Tournament> result = new ArrayList<>();
        for (TournamentInfo tournamentInfo : _tournamentDao.getFinishedTournamentsSince(since)) {
            Tournament tournament = _tournamentById.get(tournamentInfo.getTournamentId());
            if (tournament == null)
                tournament = createTournamentAndStoreInCache(tournamentInfo, cardLibrary);
            result.add(tournament);
        }
        return result;
    }

    
    public List<Tournament> getLiveTournaments() {
        LOGGER.debug("Calling getLiveTournaments function");
        List<Tournament> result = new ArrayList<>();
        LOGGER.debug("Created result object");
        for (TournamentInfo tournamentInfo : _tournamentDao.getUnfinishedTournaments()) {
            LOGGER.debug("Entered for loop");
            Tournament tournament = _tournamentById.get(tournamentInfo.getTournamentId());
            LOGGER.debug("Adding tournament {}", tournament);
            if (tournament == null)
                tournament = createTournamentAndStoreInCache(tournamentInfo, _library);
            result.add(tournament);
        }
        return result;
    }

    private Tournament createTournamentAndStoreInCache(TournamentInfo tournamentInfo, CardBlueprintLibrary cardLibrary) {
        Tournament tournament;
        try {
            tournament = tournamentInfo.createDefaultTournament(this,
                    tournamentInfo.getTournamentId(), cardLibrary, _formatLibrary);
        } catch (Exception exp) {
            throw new RuntimeException("Unable to create Tournament", exp);
        }
        _tournamentById.put(tournamentInfo.getTournamentId(), tournament);
        return tournament;
    }


    public void addRoundBye(String tournamentId, String player, int round) {
        _tournamentMatchDao.addBye(tournamentId, player, round);
    }

    
    public Map<String, Integer> getPlayerByes(String tournamentId) {
        return _tournamentMatchDao.getPlayerByes(tournamentId);
    }

    
    public List<TournamentQueueInfo> getFutureScheduledTournamentQueues(long tillDate) {
        return _tournamentDao.getFutureScheduledTournamentQueues(tillDate);
    }

    
    public void updateScheduledTournamentStarted(String scheduledTournamentId) {
        _tournamentDao.updateScheduledTournamentStarted(scheduledTournamentId);
    }

    public Map<String, TournamentQueue> getFutureScheduledTournamentQueuesNotInHall(long nextLoadTime,
                                                                                    Map<String, TournamentQueue> tournamentQueues) {
        Map<String, TournamentQueue> result = new HashMap<>();
        List<TournamentQueueInfo> futureTournamentQueues = getFutureScheduledTournamentQueues(nextLoadTime);
        for (TournamentQueueInfo queueInfo : futureTournamentQueues) {
            String tournamentId = queueInfo.getScheduledTournamentId();
            if (!tournamentQueues.containsKey(tournamentId)) {
                ScheduledTournamentQueue scheduledQueue = queueInfo
                        .createNewScheduledTournamentQueue(_library, _formatLibrary, Tournament.Stage.PLAYING_GAMES, this);
                result.put(tournamentId, scheduledQueue);
            }
        }
        return result;
    }
}