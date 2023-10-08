package com.gempukku.stccg.league;

import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.competitive.BestOfOneStandingsProducer;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.db.LeagueDAO;
import com.gempukku.stccg.db.LeagueMatchDAO;
import com.gempukku.stccg.db.LeagueParticipationDAO;
import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.db.vo.League;
import com.gempukku.stccg.db.vo.LeagueMatchResult;
import com.gempukku.stccg.draft2.SoloDraftDefinitions;
import com.gempukku.stccg.game.CardCollection;
import com.gempukku.stccg.game.User;
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
    private final Map<String, List<PlayerStanding>> _leagueSerieStandings = new ConcurrentHashMap<>();

    private int _activeLeaguesLoadedDate;
    private List<League> _activeLeagues;

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
        _leagueSerieStandings.clear();
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

    public synchronized boolean playerJoinsLeague(League league, User player, String remoteAddr) throws SQLException, IOException {
        if (isPlayerInLeague(league, player))
            return false;
        int cost = league.getCost();
        if (_collectionsManager.removeCurrencyFromPlayerCollection("Joining "+league.getName()+" league", player, CollectionType.MY_CARDS, cost)) {
            league.getLeagueData(_cardLibrary, _formatLibrary, _soloDraftDefinitions).joinLeague(_collectionsManager, player, DateUtils.getCurrentDateAsInt());
            _leagueParticipationDAO.userJoinsLeague(league.getType(), player, remoteAddr);
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

    public synchronized LeagueSeriesData getCurrentLeagueSerie(League league) {
        final int currentDate = DateUtils.getCurrentDateAsInt();

        for (LeagueSeriesData LeagueSeriesData : league.getLeagueData(_cardLibrary, _formatLibrary, _soloDraftDefinitions).getSeries()) {
            if (currentDate >= LeagueSeriesData.getStart() && currentDate <= LeagueSeriesData.getEnd())
                return LeagueSeriesData;
        }

        return null;
    }

    public synchronized void reportLeagueGameResult(League league, LeagueSeriesData serie, String winner, String loser) {
        _leagueMatchDao.addPlayedMatch(league.getType(), serie.getName(), winner, loser);

        _leagueStandings.remove(LeagueMapKeys.getLeagueMapKey(league));
        _leagueSerieStandings.remove(LeagueMapKeys.getLeagueSerieMapKey(league, serie));

        awardPrizesToPlayer(league, serie, winner, true);
        awardPrizesToPlayer(league, serie, loser, false);
    }

    private void awardPrizesToPlayer(League league, LeagueSeriesData serie, String player, boolean winner) {
        int count = 0;
        Collection<LeagueMatchResult> playerMatchesPlayedOn = getPlayerMatchesInSerie(league, serie, player);
        for (LeagueMatchResult leagueMatch : playerMatchesPlayedOn) {
            if (leagueMatch.getWinner().equals(player))
                count++;
        }

        CardCollection prize;
        if (winner)
            prize = serie.getPrizeForLeagueMatchWinner(count, playerMatchesPlayedOn.size());
        else
            prize = serie.getPrizeForLeagueMatchLoser(count, playerMatchesPlayedOn.size());
        if (prize != null)
             _collectionsManager.addItemsToPlayerCollection(true, "Prize for winning league game", player, CollectionType.MY_CARDS, prize.getAll());

    }

    public synchronized Collection<LeagueMatchResult> getPlayerMatchesInSerie(League league, LeagueSeriesData serie, String player) {
        final Collection<LeagueMatchResult> allMatches = _leagueMatchDao.getLeagueMatches(league.getType());
        Set<LeagueMatchResult> result = new HashSet<>();
        for (LeagueMatchResult match : allMatches) {
            if (match.getSerieName().equals(serie.getName()) && (match.getWinner().equals(player) || match.getLoser().equals(player)))
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

    public synchronized List<PlayerStanding> getLeagueSerieStandings(League league, LeagueSeriesData leagueSerie) {
        List<PlayerStanding> serieStandings = _leagueSerieStandings.get(LeagueMapKeys.getLeagueSerieMapKey(league, leagueSerie));
        if (serieStandings == null) {
            synchronized (this) {
                serieStandings = createLeagueSerieStandings(league, leagueSerie);
                _leagueSerieStandings.put(LeagueMapKeys.getLeagueSerieMapKey(league, leagueSerie), serieStandings);
            }
        }
        return serieStandings;
    }

    private List<PlayerStanding> createLeagueSerieStandings(League league, LeagueSeriesData leagueSerie) {
        final Collection<String> playersParticipating = _leagueParticipationDAO.getUsersParticipating(league.getType());
        final Collection<LeagueMatchResult> matches = _leagueMatchDao.getLeagueMatches(league.getType());

        Set<LeagueMatchResult> matchesInSerie = new HashSet<>();
        for (LeagueMatchResult match : matches) {
            if (match.getSerieName().equals(leagueSerie.getName()))
                matchesInSerie.add(match);
        }

        return createStandingsForMatchesAndPoints(playersParticipating, matchesInSerie);
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
        Collection<LeagueMatchResult> playedInSeason = getPlayerMatchesInSerie(league, season, player);
        return playedInSeason.size() < maxMatches;
    }

    public synchronized boolean canPlayRankedGameAgainst(League league, LeagueSeriesData season, String playerOne, String playerTwo) {
        Collection<LeagueMatchResult> playedInSeason = getPlayerMatchesInSerie(league, season, playerOne);
        int maxGames = league.getLeagueData(_cardLibrary, _formatLibrary, _soloDraftDefinitions)
                .getMaxRepeatMatchesPerSerie();
        int totalGames = 0;
        for (LeagueMatchResult leagueMatch : playedInSeason) {
            if (playerTwo.equals(leagueMatch.getWinner()) || playerTwo.equals(leagueMatch.getLoser()))
                totalGames++;
        }
        return totalGames < maxGames;
    }
}
