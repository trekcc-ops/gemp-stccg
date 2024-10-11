package com.gempukku.stccg.league;

import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.competitive.BestOfOneStandingsProducer;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.db.LeagueDAO;
import com.gempukku.stccg.db.LeagueMatchDAO;
import com.gempukku.stccg.db.LeagueParticipationDAO;
import com.gempukku.stccg.db.User;
import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.db.vo.League;
import com.gempukku.stccg.db.vo.LeagueMatchResult;
import com.gempukku.stccg.draft.SoloDraftDefinitions;
import com.gempukku.stccg.formats.FormatLibrary;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LeagueService {
    private final LeagueDAO _leagueDao;
    private final CardBlueprintLibrary _cardLibrary;
    private final FormatLibrary _formatLibrary;

    // Cached on this layer
    private final CachedLeagueMatchDAO _leagueMatchDao;
    private final CachedLeagueParticipationDAO _leagueParticipationDAO;

    private final CollectionsManager _collectionsManager;
    private final SoloDraftDefinitions _soloDraftDefinitions;

    private final Map<String, List<PlayerStanding>> _leagueStandings = new ConcurrentHashMap<>();
    private final Map<String, List<PlayerStanding>> _seriesStandings = new ConcurrentHashMap<>();

    private int _activeLeaguesLoadedDate;
    private List<League> _activeLeagues;

    public LeagueService(ServerObjects objects, CachedLeagueMatchDAO leagueMatchDAO,
                         CachedLeagueParticipationDAO participationDAO) {
        _leagueDao = objects.getLeagueDAO();
        _cardLibrary = objects.getCardBlueprintLibrary();
        _formatLibrary = objects.getFormatLibrary();
        _leagueMatchDao = leagueMatchDAO;
        _leagueParticipationDAO = participationDAO;
        _collectionsManager = objects.getCollectionsManager();
        _soloDraftDefinitions = objects.getSoloDraftDefinitions();
    }


    public LeagueService(LeagueDAO leagueDao, LeagueMatchDAO leagueMatchDao,
                         LeagueParticipationDAO leagueParticipationDAO, CollectionsManager collectionsManager,
                         CardBlueprintLibrary library, FormatLibrary formatLibrary, SoloDraftDefinitions soloDraftDefinitions) {
        _leagueDao = leagueDao;
        _cardLibrary = library;
        _formatLibrary = formatLibrary;
        _leagueMatchDao = new CachedLeagueMatchDAO(leagueMatchDao);
        _leagueParticipationDAO = new CachedLeagueParticipationDAO(leagueParticipationDAO);
        _collectionsManager = collectionsManager;
        _soloDraftDefinitions = soloDraftDefinitions;
    }

    public synchronized void clearCache() {
        _seriesStandings.clear();
        _leagueStandings.clear();
        _activeLeaguesLoadedDate = 0;

        _leagueMatchDao.clearCache();
        _leagueParticipationDAO.clearCache();
    }

    private synchronized void ensureLoadedCurrentLeagues() {
        int currentDate = DateUtils.getCurrentDateAsInt();
        if (currentDate != _activeLeaguesLoadedDate) {
            _leagueMatchDao.clearCache();
            _leagueParticipationDAO.clearCache();

            try {
                _activeLeagues = _leagueDao.loadActiveLeagues(currentDate);
                _activeLeaguesLoadedDate = currentDate;
                processLoadedLeagues(currentDate);
            } catch (SQLException e) {
                throw new RuntimeException("Unable to load Leagues", e);
            }
        }
    }

    public synchronized List<League> getActiveLeagues() {
        if (DateUtils.getCurrentDateAsInt() == _activeLeaguesLoadedDate)
            return Collections.unmodifiableList(_activeLeagues);
        else {
            ensureLoadedCurrentLeagues();
            return Collections.unmodifiableList(_activeLeagues);
        }
    }

    private void processLoadedLeagues(int currentDate) {
        for (League activeLeague : _activeLeagues) {
            int oldStatus = activeLeague.getStatus();
            int newStatus = activeLeague.getLeagueData(_cardLibrary, _formatLibrary, _soloDraftDefinitions).process(_collectionsManager, getLeagueStandings(activeLeague), oldStatus, currentDate);
            if (newStatus != oldStatus)
                _leagueDao.setStatus(activeLeague, newStatus);
        }
    }

    public synchronized boolean isPlayerInLeague(League league, User player) {
        return _leagueParticipationDAO.getUsersParticipating(league.getType()).contains(player.getName());
    }

    public synchronized boolean playerJoinsLeague(League league, User player, String remoteAddress)
            throws SQLException, IOException {
        if (isPlayerInLeague(league, player))
            return false;
        int cost = league.getCost();
        if (_collectionsManager.removeCurrencyFromPlayerCollection("Joining "+league.getName()+" league", player, CollectionType.MY_CARDS, cost)) {
            league.getLeagueData(_cardLibrary, _formatLibrary, _soloDraftDefinitions).joinLeague(_collectionsManager, player, DateUtils.getCurrentDateAsInt());
            _leagueParticipationDAO.userJoinsLeague(league.getType(), player, remoteAddress);
            _leagueStandings.remove(LeagueMapKeys.getLeagueMapKey(league));

            return true;
        } else {
            return false;
        }
    }

    public synchronized League getLeagueByType(String type) {
        for (League league : getActiveLeagues()) {
            if (league.getType().equals(type))
                return league;
        }
        return null;
    }

    public synchronized CollectionType getCollectionTypeByCode(String collectionTypeCode) {
        for (League league : getActiveLeagues()) {
            for (LeagueSeriesData LeagueSeriesData : league.getLeagueData(_cardLibrary, _formatLibrary, _soloDraftDefinitions).getSeries()) {
                CollectionType collectionType = LeagueSeriesData.getCollectionType();
                if (collectionType != null && collectionType.getCode().equals(collectionTypeCode))
                    return collectionType;
            }
        }
        return null;
    }

    public synchronized LeagueSeriesData getCurrentLeagueSeries(League league) {
        final int currentDate = DateUtils.getCurrentDateAsInt();

        for (LeagueSeriesData LeagueSeriesData : league.getLeagueData(_cardLibrary, _formatLibrary, _soloDraftDefinitions).getSeries()) {
            if (currentDate >= LeagueSeriesData.getStart() && currentDate <= LeagueSeriesData.getEnd())
                return LeagueSeriesData;
        }

        return null;
    }

    public synchronized void reportLeagueGameResult(League league, LeagueSeriesData series, String winner, String loser) {
        _leagueMatchDao.addPlayedMatch(league.getType(), series.getName(), winner, loser);

        _leagueStandings.remove(LeagueMapKeys.getLeagueMapKey(league));
        _seriesStandings.remove(LeagueMapKeys.getLeagueSeriesMapKey(league, series));

        awardPrizesToPlayer(league, series, winner, true);
        awardPrizesToPlayer(league, series, loser, false);
    }

    private void awardPrizesToPlayer(League league, LeagueSeriesData seriesData, String player, boolean winner) {
        int count = 0;
        Collection<LeagueMatchResult> playerMatchesPlayedOn = getPlayerMatchesInSeries(league, seriesData, player);
        for (LeagueMatchResult leagueMatch : playerMatchesPlayedOn) {
            if (leagueMatch.getWinner().equals(player))
                count++;
        }

        if (winner) {
            _collectionsManager.addItemsToPlayerCollection(
                    true, "Prize for winning league game", player, CollectionType.MY_CARDS,
                    seriesData.getPrizeForLeagueMatchWinner(count).getAll()
            );
        }
    }

    public synchronized Collection<LeagueMatchResult> getPlayerMatchesInSeries(League league,
                                                                               LeagueSeriesData seriesData,
                                                                               String player) {
        final Collection<LeagueMatchResult> allMatches = _leagueMatchDao.getLeagueMatches(league.getType());
        Set<LeagueMatchResult> result = new HashSet<>();
        for (LeagueMatchResult match : allMatches) {
            if (match.getSeriesName().equals(seriesData.getName()) && (match.getWinner().equals(player) || match.getLoser().equals(player)))
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

    public synchronized List<PlayerStanding> getLeagueSeriesStandings(League league, LeagueSeriesData seriesData) {
        List<PlayerStanding> standings = _seriesStandings.get(LeagueMapKeys.getLeagueSeriesMapKey(league, seriesData));
        if (standings == null) {
            synchronized (this) {
                standings = createLeagueSeriesStandings(league, seriesData);
                _seriesStandings.put(LeagueMapKeys.getLeagueSeriesMapKey(league, seriesData), standings);
            }
        }
        return standings;
    }

    private List<PlayerStanding> createLeagueSeriesStandings(League league, LeagueSeriesData seriesData) {
        final Collection<String> playersParticipating = _leagueParticipationDAO.getUsersParticipating(league.getType());
        final Collection<LeagueMatchResult> matches = _leagueMatchDao.getLeagueMatches(league.getType());

        Set<LeagueMatchResult> matchResults = new HashSet<>();
        for (LeagueMatchResult match : matches) {
            if (match.getSeriesName().equals(seriesData.getName()))
                matchResults.add(match);
        }

        return createStandingsForMatchesAndPoints(playersParticipating, matchResults);
    }

    private List<PlayerStanding> createLeagueStandings(League league) {
        final Collection<String> playersParticipating = _leagueParticipationDAO.getUsersParticipating(league.getType());
        final Collection<LeagueMatchResult> matches = _leagueMatchDao.getLeagueMatches(league.getType());

        return createStandingsForMatchesAndPoints(playersParticipating, matches);
    }

    private List<PlayerStanding> createStandingsForMatchesAndPoints(Collection<String> playersParticipating, Collection<LeagueMatchResult> matches) {
        return BestOfOneStandingsProducer.produceStandings(playersParticipating, matches, 2, 1, Collections.emptyMap());
    }

    public synchronized boolean canPlayRankedGame(League league, LeagueSeriesData season, String player) {
        int maxMatches = season.getMaxMatches();
        Collection<LeagueMatchResult> playedInSeason = getPlayerMatchesInSeries(league, season, player);
        return playedInSeason.size() < maxMatches;
    }

    public synchronized boolean canPlayRankedGameAgainst(League league, LeagueSeriesData season, String playerOne, String playerTwo) {
        Collection<LeagueMatchResult> playedInSeason = getPlayerMatchesInSeries(league, season, playerOne);
        int maxGames = league.getLeagueData(_cardLibrary, _formatLibrary, _soloDraftDefinitions)
                .getMaxRepeatMatchesPerSeries();
        int totalGames = 0;
        for (LeagueMatchResult leagueMatch : playedInSeason) {
            if (playerTwo.equals(leagueMatch.getWinner()) || playerTwo.equals(leagueMatch.getLoser()))
                totalGames++;
        }
        return totalGames < maxGames;
    }
}