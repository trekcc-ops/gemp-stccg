package com.gempukku.stccg.tournament;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.db.vo.CollectionType;
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

    public TournamentService(TournamentDAO tournamentDao, TournamentPlayerDAO tournamentPlayerDao,
                             TournamentMatchDAO tournamentMatchDao, CardBlueprintLibrary library) {
        _tournamentDao = tournamentDao;
        _tournamentPlayerDao = tournamentPlayerDao;
        _tournamentMatchDao = tournamentMatchDao;
        _library = library;
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

    
    public Tournament addTournament(String tournamentId, String draftType, String tournamentName, String format,
                                    CollectionType collectionType, Tournament.Stage stage, String pairingMechanism,
                                    String prizeScheme, Date start) {
        _tournamentDao.addTournament(tournamentId, draftType, tournamentName, format, collectionType, stage,
                pairingMechanism, prizeScheme, start);
        return createTournamentAndStoreInCache(tournamentId,
                new TournamentInfo(tournamentId, tournamentName, format, collectionType, stage,
                        pairingMechanism, prizeScheme, 0));
    }

    
    public void updateTournamentStage(String tournamentId, Tournament.Stage stage) {
        _tournamentDao.updateTournamentStage(tournamentId, stage);
    }

    
    public void updateTournamentRound(String tournamentId, int round) {
        _tournamentDao.updateTournamentRound(tournamentId, round);
    }

    
    public List<Tournament> getOldTournaments(long since) {
        List<Tournament> result = new ArrayList<>();
        for (TournamentInfo tournamentInfo : _tournamentDao.getFinishedTournamentsSince(since)) {
            Tournament tournament = _tournamentById.get(tournamentInfo.getTournamentId());
            if (tournament == null)
                tournament = createTournamentAndStoreInCache(tournamentInfo.getTournamentId(), tournamentInfo);
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
            LOGGER.debug("Adding tournament " + tournament);
            if (tournament == null)
                tournament = createTournamentAndStoreInCache(tournamentInfo.getTournamentId(), tournamentInfo);
            result.add(tournament);
        }
        return result;
    }

    
    public Tournament getTournamentById(String tournamentId) {
        Tournament tournament = _tournamentById.get(tournamentId);
        if (tournament == null) {
            TournamentInfo tournamentInfo = _tournamentDao.getTournamentById(tournamentId);
            if (tournamentInfo == null)
                return null;

            tournament = createTournamentAndStoreInCache(tournamentId, tournamentInfo);
        }
        return tournament;
    }

    private Tournament createTournamentAndStoreInCache(String tournamentId, TournamentInfo tournamentInfo) {
        Tournament tournament;
        try {
            tournament = new DefaultTournament(this,
                    tournamentId,  tournamentInfo.getTournamentName(), tournamentInfo.getTournamentFormat(),
                    tournamentInfo.getCollectionType(), tournamentInfo.getTournamentRound(),
                    tournamentInfo.getTournamentStage(),
                    PairingMechanismRegistry.getPairingMechanism(tournamentInfo.getPairingMechanism()),
                    TournamentPrizeSchemeRegistry.getTournamentPrizes(_library, tournamentInfo.getPrizesScheme()));
        } catch (Exception exp) {
            throw new RuntimeException("Unable to create Tournament", exp);
        }
        _tournamentById.put(tournamentId, tournament);
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
}