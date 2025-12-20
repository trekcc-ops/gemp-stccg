package com.gempukku.stccg.league;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.async.LoggingProxy;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.competitive.BestOfOneStandingsProducer;
import com.gempukku.stccg.competitive.LeagueMatchResult;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.database.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LeagueService {
    private final LeagueDAO _leagueDAO;
    private final CachedLeagueMatchDAO _leagueMatchDAO;
    private final CachedLeagueParticipationDAO _leagueParticipationDAO;
    private final CollectionsManager _collectionsManager;
    private final Map<String, List<PlayerStanding>> _leagueStandings = new ConcurrentHashMap<>();
    private final Map<String, List<PlayerStanding>> _seriesStandings = new ConcurrentHashMap<>();
    private int _activeLeaguesLoadedDate;
    private List<League> _activeLeagues;

    public LeagueService(CollectionsManager collectionsManager, ObjectMapper leagueMapper, DbAccess dbAccess) {
        _leagueDAO =
                LoggingProxy.createLoggingProxy(LeagueDAO.class, new DbLeagueDAO(dbAccess, leagueMapper));
        _leagueParticipationDAO = new CachedLeagueParticipationDAO(dbAccess);
        _leagueMatchDAO = new CachedLeagueMatchDAO(dbAccess);
        _collectionsManager = collectionsManager;
        refreshActiveLeagues();
    }

    public LeagueService(LeagueDAO leagueDao, LeagueMatchDAO leagueMatchDao,
                         LeagueParticipationDAO leagueParticipationDAO, CollectionsManager collectionsManager) {
        _leagueDAO = leagueDao;
        _leagueMatchDAO = new CachedLeagueMatchDAO(leagueMatchDao);
        _leagueParticipationDAO = new CachedLeagueParticipationDAO(leagueParticipationDAO);
        _collectionsManager = collectionsManager;
//        refreshActiveLeagues();
    }

    public synchronized void clearCache() {
        _seriesStandings.clear();
        _leagueStandings.clear();
        _activeLeaguesLoadedDate = 0;

        _leagueMatchDAO.clearCache();
        _leagueParticipationDAO.clearCache();
    }

    private synchronized void refreshActiveLeagues() {
        int currentDate = DateUtils.getCurrentDateAsInt();
        _leagueMatchDAO.clearCache();
        _leagueParticipationDAO.clearCache();

        try {
            _activeLeagues = _leagueDAO.loadActiveLeagues();
            _activeLeaguesLoadedDate = currentDate;
            processLoadedLeagues(currentDate);
        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException("Unable to load Leagues", e);
        }
    }

    public synchronized List<League> getActiveLeagues() {
        if (DateUtils.getCurrentDateAsInt() != _activeLeaguesLoadedDate) {
            refreshActiveLeagues();
        }
        return Collections.unmodifiableList(_activeLeagues);
    }

    private void processLoadedLeagues(int currentDate) {
        for (League activeLeague : _activeLeagues) {
            int oldStatus = activeLeague.getStatus();
            List<PlayerStanding> leagueStandings = getLeagueStandings(activeLeague);
            activeLeague.process(_collectionsManager, leagueStandings);
            int newStatus = activeLeague.getStatus();
            if (newStatus != oldStatus)
                _leagueDAO.setStatus(activeLeague, newStatus);
        }
    }

    public synchronized boolean isPlayerInLeague(League league, String userName) {
        return _leagueParticipationDAO.getUsersParticipating(league.getType()).contains(userName);
    }

    public synchronized boolean isPlayerInLeague(League league, User player) {
        return _leagueParticipationDAO.getUsersParticipating(league.getType()).contains(player.getName());
    }

    public synchronized boolean playerJoinsLeague(League league, User player, String remoteAddress)
            throws SQLException, IOException {
        if (isPlayerInLeague(league, player))
            return false;
        int cost = league.getCost();
        if (_collectionsManager.removeCurrencyFromPlayerCollection("Joining "+league.getName()+" league",
                player, cost)) {
            league.joinLeague(_collectionsManager, player);
            _leagueParticipationDAO.userJoinsLeague(league.getType(), player, remoteAddress);
            _leagueStandings.remove(LeagueMapKeys.getLeagueMapKey(league));
            return true;
        } else {
            return false;
        }
    }

    public synchronized League getLeagueByType(String type) throws LeagueNotFoundException {
        for (League league : getActiveLeagues()) {
            if (league.getType().equals(type))
                return league;
        }
        throw new LeagueNotFoundException();
    }

    public synchronized CollectionType getCollectionTypeByCode(String collectionTypeCode) {
        for (League league : getActiveLeagues()) {
            if (league.getCollectionType() != null && league.getCollectionType().getCode().equals(collectionTypeCode)) {
                return league.getCollectionType();
            }
        }
        return null;
    }

    public synchronized LeagueSeries getCurrentLeagueSeriesNew(League league) {
        ZonedDateTime currentDate = ZonedDateTime.now();
        for (LeagueSeries series : league) {
            if (currentDate.isAfter(series.getStart()) && currentDate.isBefore(series.getEnd()))
                return series;
        }

        return null;
    }


    public synchronized void reportLeagueGameResult(League league, LeagueSeries series, String winner,
                                                    String loser) {
        _leagueMatchDAO.addPlayedMatch(league.getType(), series.getName(), winner, loser);

        _leagueStandings.remove(LeagueMapKeys.getLeagueMapKey(league));
        _seriesStandings.remove(LeagueMapKeys.getLeagueSeriesMapKey(league, series));

        awardPrizesToPlayer(league, series, winner, true);
        awardPrizesToPlayer(league, series, loser, false);
    }

    private void awardPrizesToPlayer(League league, LeagueSeries seriesData, String player, boolean winner) {
        int count = 0;
        Collection<LeagueMatchResult> playerMatchesPlayedOn = getPlayerMatchesInSeries(league, seriesData, player);
        for (LeagueMatchResult leagueMatch : playerMatchesPlayedOn) {
            if (leagueMatch.getWinner().equals(player))
                count++;
        }

        if (winner) {
            _collectionsManager.addItemsToPlayerMyCardsCollection(
                    true, "Prize for winning league game", player,
                    league.getPrizeForLeagueMatchWinner(count).getAll()
            );
        }
    }


    public synchronized Collection<LeagueMatchResult> getPlayerMatchesInSeries(League league,
                                                                               LeagueSeries series,
                                                                               String player) {
        final Collection<LeagueMatchResult> allMatches = _leagueMatchDAO.getLeagueMatches(league.getType());
        Set<LeagueMatchResult> result = new HashSet<>();
        for (LeagueMatchResult match : allMatches) {
            if (match.getSeriesName().equals(series.getName()) && (match.getWinner().equals(player) ||
                    match.getLoser().equals(player)))
                result.add(match);
        }
        return result;
    }


    public synchronized List<PlayerStanding> getLeagueStandings(League league) {
        List<PlayerStanding> leagueStandings = _leagueStandings.get(LeagueMapKeys.getLeagueMapKey(league));
        if (leagueStandings == null) {
            synchronized (this) {
                leagueStandings = createLeagueStandings(league);
                _leagueStandings.put(LeagueMapKeys.getLeagueMapKey(league), leagueStandings);
            }
        }
        return leagueStandings;
    }

    public synchronized List<PlayerStanding> getLeagueSeriesStandings(League league, LeagueSeries series) {
        List<PlayerStanding> standings = _seriesStandings.get(LeagueMapKeys.getLeagueSeriesMapKey(league, series));
        if (standings == null) {
            synchronized (this) {
                standings = createLeagueSeriesStandings(league, series);
                _seriesStandings.put(LeagueMapKeys.getLeagueSeriesMapKey(league, series), standings);
            }
        }
        return standings;
    }


    private List<PlayerStanding> createLeagueSeriesStandings(League league, LeagueSeries series) {
        final Collection<String> playersParticipating = _leagueParticipationDAO.getUsersParticipating(league.getType());
        final Collection<LeagueMatchResult> matches = _leagueMatchDAO.getLeagueMatches(league.getType());

        Set<LeagueMatchResult> matchResults = new HashSet<>();
        for (LeagueMatchResult match : matches) {
            if (match.getSeriesName().equals(series.getName()))
                matchResults.add(match);
        }

        return createStandingsForMatchesAndPoints(playersParticipating, matchResults);
    }

    private List<PlayerStanding> createLeagueStandings(League league) {
        final Collection<String> playersParticipating = _leagueParticipationDAO.getUsersParticipating(league.getType());
        final Collection<LeagueMatchResult> matches = _leagueMatchDAO.getLeagueMatches(league.getType());

        return createStandingsForMatchesAndPoints(playersParticipating, matches);
    }

    private List<PlayerStanding> createStandingsForMatchesAndPoints(Collection<String> playersParticipating,
                                                                    Collection<? extends LeagueMatchResult> matches) {
        return BestOfOneStandingsProducer.produceStandings(
                playersParticipating, matches, 2, 1, Collections.emptyMap());
    }

    public synchronized boolean canPlayRankedGame(League league, LeagueSeries season, String player) {
        int maxMatches = season.getMaxMatches();
        Collection<LeagueMatchResult> playedInSeason = getPlayerMatchesInSeries(league, season, player);
        return playedInSeason.size() < maxMatches;
    }

    public synchronized boolean canPlayRankedGameAgainst(League league, LeagueSeries season, String playerOne,
                                                         String playerTwo) {
        Collection<LeagueMatchResult> playedInSeason = getPlayerMatchesInSeries(league, season, playerOne);
        int maxGames = league.getMaxRepeatMatchesPerSeries();
        int totalGames = 0;
        for (LeagueMatchResult leagueMatch : playedInSeason) {
            if (playerTwo.equals(leagueMatch.getWinner()) || playerTwo.equals(leagueMatch.getLoser()))
                totalGames++;
        }
        return totalGames < maxGames;
    }


    public void addLeague(League league) {
        _leagueDAO.addLeague(league);
    }

}