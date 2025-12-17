package com.gempukku.stccg.async;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.collection.CachedCollectionDAO;
import com.gempukku.stccg.collection.CachedTransferDAO;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.collection.TransferDAO;
import com.gempukku.stccg.database.*;
import com.gempukku.stccg.draft.DraftFormatLibrary;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.game.GameHistoryService;
import com.gempukku.stccg.game.GameRecorder;
import com.gempukku.stccg.game.GameServer;
import com.gempukku.stccg.hall.HallServer;
import com.gempukku.stccg.league.CachedLeagueMatchDAO;
import com.gempukku.stccg.league.CachedLeagueParticipationDAO;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.merchant.MerchantService;
import com.gempukku.stccg.packs.ProductLibrary;
import com.gempukku.stccg.service.AdminService;
import com.gempukku.stccg.service.LoggedUserHolder;
import com.gempukku.stccg.tournament.TournamentDAO;
import com.gempukku.stccg.tournament.TournamentMatchDAO;
import com.gempukku.stccg.tournament.TournamentPlayerDAO;
import com.gempukku.stccg.tournament.TournamentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerObjects {
    private static final Logger LOGGER = LogManager.getLogger(ServerObjects.class);
    private final CardBlueprintLibrary _cardBlueprintLibrary;
    private final ProductLibrary _productLibrary;
    private final LoggedUserHolder _loggedUserHolder;
    private CachedLeagueParticipationDAO _leagueParticipationDAO;
    private CachedLeagueMatchDAO _leagueMatchDAO;
    private TournamentDAO _tournamentDAO;
    private TournamentPlayerDAO _tournamentPlayerDAO;
    private TournamentMatchDAO _tournamentMatchDAO;
    private LeagueDAO _leagueDAO;
    private CacheManager _cacheManager;
    private GameHistoryDAO _gameHistoryDAO;
    private IgnoreDAO _ignoreDAO;
    private CachedDeckDAO _deckDAO;
    private CachedCollectionDAO _collectionDAO;
    private CachedPlayerDAO _playerDAO;
    private CachedTransferDAO _transferDAO;
    private CachedIpBanDAO _ipBanDAO;
    private FormatLibrary _formatLibrary;
    private GameHistoryService _gameHistoryService;
    private GameRecorder _gameRecorder;
    private CollectionsManager _collectionsManager;
    private DraftFormatLibrary _DraftFormatLibrary;
    private LeagueService _leagueService;
    private AdminService _adminService;
    private TournamentService _tournamentService;
    private MerchantService _merchantService;
    private ChatServer _chatServer;
    private GameServer _gameServer;
    private HallServer _hallServer;
    private final LongPollingSystem _longPollingSystem;

    public ServerObjects() {
        //Libraries and other important prerequisite managers that are used by lots of other managers
        LOGGER.info("GempukkuServer loading prerequisites...");
        _cardBlueprintLibrary = new CardBlueprintLibrary();
        _productLibrary = new ProductLibrary();
        _loggedUserHolder = new LoggedUserHolder();
        _loggedUserHolder.start();
        _longPollingSystem = new LongPollingSystem();

        //Now bulk initialize various managers
        LOGGER.info("GempukkuServer loading DAOs...");
        createDatabaseObjects();
        LOGGER.info("GempukkuServer loading services...");
        createServices();
        LOGGER.info("GempukkuServer starting servers...");
        startServers();
        LOGGER.info("GempukkuServer startup complete.");
    }

    private final void createDatabaseObjects() {
        DbAccess dbAccess = new DbAccess();

        _leagueParticipationDAO = new CachedLeagueParticipationDAO(dbAccess);
        _leagueMatchDAO = new CachedLeagueMatchDAO(dbAccess);
        _tournamentDAO =
                LoggingProxy.createLoggingProxy(TournamentDAO.class, new DbTournamentDAO(dbAccess));
        _tournamentPlayerDAO =
                LoggingProxy.createLoggingProxy(TournamentPlayerDAO.class, new DbTournamentPlayerDAO(this, dbAccess));
        _tournamentMatchDAO =
                LoggingProxy.createLoggingProxy(TournamentMatchDAO.class, new DbTournamentMatchDAO(dbAccess));
        _leagueDAO =
                LoggingProxy.createLoggingProxy(LeagueDAO.class, new DbLeagueDAO(dbAccess));
        _gameHistoryDAO =
                LoggingProxy.createLoggingProxy(GameHistoryDAO.class, new DbGameHistoryDAO(dbAccess));
        _ignoreDAO = new CachedIgnoreDAO(dbAccess);
        _deckDAO = new CachedDeckDAO(this, dbAccess);
        _collectionDAO = new CachedCollectionDAO(dbAccess);
        _playerDAO = new CachedPlayerDAO(dbAccess);
        _transferDAO = new CachedTransferDAO(dbAccess);
        _ipBanDAO = new CachedIpBanDAO(dbAccess);
        _cacheManager = new CacheManager(_deckDAO, _collectionDAO, _playerDAO, _transferDAO, _ipBanDAO);
    }

    private void createServices() {
        _formatLibrary = new FormatLibrary(_cardBlueprintLibrary);
        _gameHistoryService = new GameHistoryService(_gameHistoryDAO);
        _gameRecorder = new GameRecorder(_gameHistoryService, _playerDAO);
        _collectionsManager = new CollectionsManager(_playerDAO, _collectionDAO, _transferDAO, _cardBlueprintLibrary);
        _DraftFormatLibrary = new DraftFormatLibrary(_cardBlueprintLibrary, _formatLibrary);
        _leagueService = new LeagueService(this, _leagueMatchDAO, _leagueParticipationDAO);
        _adminService = new AdminService(_playerDAO, _ipBanDAO, _loggedUserHolder);
        _tournamentService = new TournamentService(
                _tournamentDAO, _tournamentPlayerDAO, _tournamentMatchDAO, _cardBlueprintLibrary);
        _merchantService = new MerchantService(_cardBlueprintLibrary, _collectionsManager);
        _chatServer = new ChatServer(this);
        _gameServer = new GameServer(_chatServer, _gameRecorder);
        _hallServer = new HallServer(this, _formatLibrary, _chatServer);
    }

    private void startServers() {
        LOGGER.debug("Function StartServers - starting HallServer");
        _hallServer.startServer();
        LOGGER.debug("Function StartServers - starting GameServer");
        _gameServer.startServer();
        LOGGER.debug("Function StartServers - starting ChatServer");
        _chatServer.startServer();
    }


    public final CardBlueprintLibrary getCardBlueprintLibrary() { return _cardBlueprintLibrary; }
    public final ProductLibrary getProductLibrary() { return _productLibrary; }
    public final LoggedUserHolder getLoggedUserHolder() { return _loggedUserHolder; }
    public final LeagueDAO getLeagueDAO() { return _leagueDAO; }
    public final IgnoreDAO getIgnoreDAO() { return _ignoreDAO; }
    public final DeckDAO getDeckDAO() { return _deckDAO; }
    public final PlayerDAO getPlayerDAO() { return _playerDAO; }
    public final GameHistoryDAO getGameHistoryDAO() { return _gameHistoryDAO; }
    public final TransferDAO getTransferDAO() { return _transferDAO; }
    final IpBanDAO getIpBanDAO() { return _ipBanDAO; }
    public final CacheManager getCacheManager() { return _cacheManager; }
    public final FormatLibrary getFormatLibrary() { return _formatLibrary; }
    public final GameHistoryService getGameHistoryService() { return _gameHistoryService; }
    public final GameRecorder getGameRecorder() { return _gameRecorder; }
    public final CollectionsManager getCollectionsManager() { return _collectionsManager; }
    public final DraftFormatLibrary getSoloDraftDefinitions() { return _DraftFormatLibrary; }
    public final LeagueService getLeagueService() { return _leagueService; }
    public final AdminService getAdminService() { return _adminService; }
    public final TournamentService getTournamentService() { return _tournamentService; }
    public final MerchantService getMerchantService() { return _merchantService; }
    public final ChatServer getChatServer() { return _chatServer; }
    public final GameServer getGameServer() { return _gameServer; }
    public final HallServer getHallServer() { return _hallServer; }

    public LongPollingSystem getLongPollingSystem() {
        return _longPollingSystem;
    }

}