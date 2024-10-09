package com.gempukku.stccg.tournament;

import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.competitive.BestOfOneStandingsProducer;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.draft.DefaultDraft;
import com.gempukku.stccg.draft.DraftPack;
import com.gempukku.stccg.packs.ProductLibrary;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DefaultTournament implements Tournament {
    private long _waitForPairingsTime = 1000 * 60 * 2;

    private final PairingMechanism _pairingMechanism;
    private final TournamentPrizes _tournamentPrizes;
    private final String _tournamentId;
    private final String _tournamentName;
    private final String _format;
    private final CollectionType _collectionType;
    private Stage _tournamentStage;
    private int _tournamentRound;

    private final Set<String> _players;
    private final Map<String, CardDeck> _playerDecks;
    private final Set<String> _droppedPlayers;
    private final Map<String, Integer> _playerByes;

    private final Set<String> _currentlyPlayingPlayers;
    private final Set<TournamentMatch> _finishedTournamentMatches;

    private final TournamentService _tournamentService;

    private final ReadWriteLock _lock = new ReentrantReadWriteLock();
    private TournamentTask _nextTask;

    private long _deckBuildStartTime;
    private DefaultDraft _draft;

    private List<PlayerStanding> _currentStandings;

    public DefaultTournament(CollectionsManager collectionsManager, TournamentService tournamentService,
                             ProductLibrary productLibrary, DraftPack draftPack, String tournamentId, String tournamentName, String format, CollectionType collectionType,
                             int tournamentRound, Stage tournamentStage, PairingMechanism pairingMechanism, TournamentPrizes tournamentPrizes) {
        _tournamentService = tournamentService;
        _tournamentId = tournamentId;
        _tournamentName = tournamentName;
        _format = format;
        _collectionType = collectionType;
        _tournamentRound = tournamentRound;
        _tournamentStage = tournamentStage;
        _pairingMechanism = pairingMechanism;
        _tournamentPrizes = tournamentPrizes;

        _currentlyPlayingPlayers = new HashSet<>();

        _players = new HashSet<>(_tournamentService.getPlayers(_tournamentId));
        _playerDecks = new HashMap<>(_tournamentService.getPlayerDecks(_tournamentId, _format));
        _droppedPlayers = new HashSet<>(_tournamentService.getDroppedPlayers(_tournamentId));
        _playerByes = new HashMap<>(_tournamentService.getPlayerByes(_tournamentId));
        _finishedTournamentMatches = new HashSet<>();

        if (_tournamentStage == Stage.PLAYING_GAMES) {
            Map<String, String> matchesToCreate = new HashMap<>();
            for (TournamentMatch tournamentMatch : _tournamentService.getMatches(_tournamentId)) {
                if (tournamentMatch.isFinished())
                    _finishedTournamentMatches.add(tournamentMatch);
                else {
                    _currentlyPlayingPlayers.add(tournamentMatch.getPlayerOne());
                    _currentlyPlayingPlayers.add(tournamentMatch.getPlayerTwo());
                    matchesToCreate.put(tournamentMatch.getPlayerOne(), tournamentMatch.getPlayerTwo());
                }
            }

            if (!matchesToCreate.isEmpty())
                _nextTask = new CreateMissingGames(matchesToCreate);
        } else if (_tournamentStage == Stage.DRAFT) {
            _draft = new DefaultDraft(collectionsManager, _collectionType, productLibrary, draftPack,
                    _players);
        } else if (_tournamentStage == Stage.DECK_BUILDING) {
            _deckBuildStartTime = System.currentTimeMillis();
        } else if (_tournamentStage == Stage.FINISHED) {
            _finishedTournamentMatches.addAll(_tournamentService.getMatches(_tournamentId));
        }
    }

    public void setWaitForPairingsTime(long waitForPairingsTime) {
        _waitForPairingsTime = waitForPairingsTime;
    }

    @Override
    public String getPlayOffSystem() {
        return _pairingMechanism.getPlayOffSystem();
    }

    @Override
    public int getPlayersInCompetitionCount() {
        return _players.size() - _droppedPlayers.size();
    }

    @Override
    public String getTournamentId() {
        return _tournamentId;
    }

    @Override
    public String getTournamentName() {
        return _tournamentName;
    }

    @Override
    public Stage getTournamentStage() {
        return _tournamentStage;
    }

    @Override
    public CollectionType getCollectionType() {
        return _collectionType;
    }

    @Override
    public int getCurrentRound() {
        return _tournamentRound;
    }

    @Override
    public String getFormat() {
        return _format;
    }

    @Override
    public boolean isPlayerInCompetition(String player) {
        _lock.readLock().lock();
        try {
            return _tournamentStage != Stage.FINISHED && _players.contains(player) && !_droppedPlayers.contains(player);
        } finally {
            _lock.readLock().unlock();
        }
    }

    @Override
    public void reportGameFinished(String winner, String loser) {
        _lock.writeLock().lock();
        try {
            if (_tournamentStage == Stage.PLAYING_GAMES && _currentlyPlayingPlayers.contains(winner)
                    && _currentlyPlayingPlayers.contains(loser)) {
                _tournamentService.setMatchResult(_tournamentId, winner);
                _currentlyPlayingPlayers.remove(winner);
                _currentlyPlayingPlayers.remove(loser);
                _finishedTournamentMatches.add(
                        new TournamentMatch(winner, loser, winner));
                if (_pairingMechanism.shouldDropLoser()) {
                    _tournamentService.dropPlayer(_tournamentId, loser);
                    _droppedPlayers.add(loser);
                }
                _currentStandings = null;
            }
        } finally {
            _lock.writeLock().unlock();
        }
    }

    @Override
    public void playerSubmittedDeck(String player, CardDeck deck) {
        _lock.writeLock().lock();
        try {
            if (_tournamentStage == Stage.DECK_BUILDING && _players.contains(player)) {
                _tournamentService.setPlayerDeck(_tournamentId, player, deck);
                _playerDecks.put(player, deck);
            }
        } finally {
            _lock.writeLock().unlock();
        }
    }

    public CardDeck getPlayerDeck(String player) {
        _lock.readLock().lock();
        try {
            return _playerDecks.get(player);
        } finally {
            _lock.readLock().unlock();
        }
    }

    @Override
    public void dropPlayer(String player) {
        _lock.writeLock().lock();
        try {
            if (_currentlyPlayingPlayers.contains(player))
                return;
            if (_tournamentStage == Stage.FINISHED)
                return;
            if (_droppedPlayers.contains(player))
                return;
            if (!_players.contains(player))
                return;

            _tournamentService.dropPlayer(_tournamentId, player);
            _droppedPlayers.add(player);
        } finally {
            _lock.writeLock().unlock();
        }
    }

    @Override
    public boolean advanceTournament(TournamentCallback tournamentCallback, CollectionsManager collectionsManager) {
        _lock.writeLock().lock();
        try {
            boolean result = false;
            if (_nextTask == null) {
                if (_tournamentStage == Stage.DRAFT) {
                    _draft.advanceDraft();
                    if (_draft.isFinished()) {
                        tournamentCallback.broadcastMessage("Drafting in tournament " + _tournamentName + " is finished, starting deck building");
                        _tournamentStage = Stage.DECK_BUILDING;
                        _tournamentService.updateTournamentStage(_tournamentId, _tournamentStage);
                        _deckBuildStartTime = System.currentTimeMillis();
                        _draft = null;
                        result = true;
                    }
                }
                if (_tournamentStage == Stage.DECK_BUILDING) {
                    // 10 minutes
                    int _deckBuildTime = 10 * 60 * 1000;
                    if (_deckBuildStartTime + _deckBuildTime < System.currentTimeMillis()
                            || _playerDecks.size() == _players.size()) {
                        _tournamentStage = Stage.PLAYING_GAMES;
                        _tournamentService.updateTournamentStage(_tournamentId, _tournamentStage);
                        result = true;
                    }
                }
                if (_tournamentStage == Stage.PLAYING_GAMES) {
                    if (_currentlyPlayingPlayers.isEmpty()) {
                        if (_pairingMechanism.isFinished(_tournamentRound, _players, _droppedPlayers)) {
                            finishTournament(tournamentCallback, collectionsManager);
                        } else {
                            tournamentCallback.broadcastMessage("Tournament " + _tournamentName + " will start round "+(_tournamentRound+1)+" in 2 minutes");
                            _nextTask = new PairPlayers();
                        }
                        result = true;
                    }
                }
            }
            if (_nextTask != null && _nextTask.getExecuteAfter() <= System.currentTimeMillis()) {
                TournamentTask task = _nextTask;
                _nextTask = null;
                task.executeTask(tournamentCallback, collectionsManager);
                result = true;
            }
            return result;
        } finally {
            _lock.writeLock().unlock();
        }
    }

    @Override
    public List<PlayerStanding> getCurrentStandings() {
        List<PlayerStanding> result = _currentStandings;
        if (result != null)
            return result;

        _lock.readLock().lock();
        try {
            _currentStandings = BestOfOneStandingsProducer.produceStandings(_players, _finishedTournamentMatches, 2, 1, _playerByes);
            return _currentStandings;
        } finally {
            _lock.readLock().unlock();
        }
    }

    private void finishTournament(TournamentCallback tournamentCallback, CollectionsManager collectionsManager) {
        _tournamentStage = Stage.FINISHED;
        _tournamentService.updateTournamentStage(_tournamentId, _tournamentStage);
        tournamentCallback.broadcastMessage("Tournament " + _tournamentName + " is finished");
        awardPrizes(collectionsManager);
    }

    private void awardPrizes(CollectionsManager collectionsManager) {
        List<PlayerStanding> list = getCurrentStandings();
        for (PlayerStanding playerStanding : list) {
            CardCollection prizes = _tournamentPrizes.getPrizeForTournament(playerStanding, list.size());
            if (prizes != null)
                collectionsManager.addItemsToPlayerCollection(
                        true, "Tournament " + getTournamentName() + " prize",
                        playerStanding.getPlayerName(), CollectionType.MY_CARDS, prizes.getAll()
                );
        }
    }

    private void createNewGame(TournamentCallback tournamentCallback, String playerOne, String playerTwo) {
        tournamentCallback.createGame(playerOne, _playerDecks.get(playerOne),
                playerTwo, _playerDecks.get(playerTwo));
    }

    private void doPairing(TournamentCallback tournamentCallback, CollectionsManager collectionsManager) {
        _tournamentRound++;
        _tournamentService.updateTournamentRound(_tournamentId, _tournamentRound);
        Map<String, String> pairingResults = new HashMap<>();
        Set<String> byeResults = new HashSet<>();

        Map<String, Set<String>> previouslyPaired = getPreviouslyPairedPlayersMap();

        boolean finished = _pairingMechanism.pairPlayers(_tournamentRound, _players, _droppedPlayers, _playerByes, getCurrentStandings(), previouslyPaired, pairingResults, byeResults);
        if (finished) {
            finishTournament(tournamentCallback, collectionsManager);
        } else {
            for (Map.Entry<String, String> pairing : pairingResults.entrySet()) {
                String playerOne = pairing.getKey();
                String playerTwo = pairing.getValue();
                _tournamentService.addMatch(_tournamentId, _tournamentRound, playerOne, playerTwo);
                _currentlyPlayingPlayers.add(playerOne);
                _currentlyPlayingPlayers.add(playerTwo);
                createNewGame(tournamentCallback, playerOne, playerTwo);
            }

            if (!byeResults.isEmpty())
                tournamentCallback.broadcastMessage("Bye awarded to: "+ StringUtils.join(byeResults, ", "));
            for (String bye : byeResults) {
                _tournamentService.addRoundBye(_tournamentId, bye, _tournamentRound);
                addPlayerBye(bye);
            }
        }
    }

    private Map<String, Set<String>> getPreviouslyPairedPlayersMap() {
        Map<String, Set<String>> previouslyPaired = new HashMap<>();
        for (String player : _players)
            previouslyPaired.put(player, new HashSet<>());

        for (TournamentMatch finishedTournamentMatch : _finishedTournamentMatches) {
            previouslyPaired.get(finishedTournamentMatch.getWinner()).add(finishedTournamentMatch.getLoser());
            previouslyPaired.get(finishedTournamentMatch.getLoser()).add(finishedTournamentMatch.getWinner());
        }
        return previouslyPaired;
    }

    private void addPlayerBye(String player) {
        Integer byes = _playerByes.get(player);
        if (byes == null)
            byes = 0;
        _playerByes.put(player, byes + 1);
    }

    private class PairPlayers implements TournamentTask {
        private final long _taskStart = System.currentTimeMillis() + _waitForPairingsTime;

        @Override
        public void executeTask(TournamentCallback tournamentCallback, CollectionsManager collectionsManager) {
            doPairing(tournamentCallback, collectionsManager);
        }

        @Override
        public long getExecuteAfter() {
            return _taskStart;
        }
    }

    private class CreateMissingGames implements TournamentTask {
        private final Map<String, String> _gamesToCreate;

        public CreateMissingGames(Map<String, String> gamesToCreate) {
            _gamesToCreate = gamesToCreate;
        }

        @Override
        public void executeTask(TournamentCallback tournamentCallback, CollectionsManager collectionsManager) {
            for (Map.Entry<String, String> pairings : _gamesToCreate.entrySet()) {
                String playerOne = pairings.getKey();
                String playerTwo = pairings.getValue();
                createNewGame(tournamentCallback, playerOne, playerTwo);
            }
        }

        @Override
        public long getExecuteAfter() {
            return 0;
        }
    }
}