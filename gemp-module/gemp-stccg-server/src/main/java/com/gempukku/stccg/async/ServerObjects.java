package com.gempukku.stccg.async;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.collection.CachedCollectionDAO;
import com.gempukku.stccg.collection.CachedTransferDAO;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.database.*;
import com.gempukku.stccg.draft.DraftFormatLibrary;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.game.GameHistoryService;
import com.gempukku.stccg.game.GameRecorder;
import com.gempukku.stccg.game.GameServer;
import com.gempukku.stccg.hall.HallServer;
import com.gempukku.stccg.league.LeagueMapper;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.packs.ProductLibrary;
import com.gempukku.stccg.service.AdminService;
import com.gempukku.stccg.tournament.TournamentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerObjects {
    private static final Logger LOGGER = LogManager.getLogger(ServerObjects.class);
    private final CardBlueprintLibrary _cardBlueprintLibrary;
    private final CachedCollectionDAO _collectionDAO;
    private final CachedPlayerDAO _playerDAO;
    private final CachedTransferDAO _transferDAO;
    private final CachedIpBanDAO _ipBanDAO;
    private final FormatLibrary _formatLibrary;
    private final DraftFormatLibrary _draftFormatLibrary;
    private AdminService _adminService;
    private final InjectableValues.Std _injectables = new InjectableValues.Std();

    public ServerObjects() {
        //Libraries and other important prerequisite managers that are used by lots of other managers
        LOGGER.info("GempukkuServer loading prerequisites...");
        _cardBlueprintLibrary = new CardBlueprintLibrary();
        _formatLibrary = new FormatLibrary(_cardBlueprintLibrary);
        _draftFormatLibrary = new DraftFormatLibrary(_cardBlueprintLibrary, _formatLibrary);

        _injectables.addValue(LongPollingSystem.class, new LongPollingSystem());
        _injectables.addValue(CardBlueprintLibrary.class, _cardBlueprintLibrary);
        _injectables.addValue(ProductLibrary.class, new ProductLibrary());
        _injectables.addValue(FormatLibrary.class, _formatLibrary);
        _injectables.addValue(DraftFormatLibrary.class, _draftFormatLibrary);

        // Now bulk initialize various managers
        // DAO objects should only have dbAccess as a constructor parameter
        LOGGER.info("GempukkuServer loading DAOs...");
        DbAccess dbAccess = new DbAccess();

        CachedDeckDAO _deckDAO = new CachedDeckDAO(dbAccess);
        _collectionDAO = new CachedCollectionDAO(dbAccess);
        _playerDAO = new CachedPlayerDAO(dbAccess);
        _transferDAO = new CachedTransferDAO(dbAccess);
        _ipBanDAO = new CachedIpBanDAO(dbAccess);

        _injectables.addValue(DeckDAO.class, _deckDAO);
        _injectables.addValue(CacheManager.class,
                new CacheManager(_deckDAO, _collectionDAO, _playerDAO, _transferDAO, _ipBanDAO));

        LOGGER.info("GempukkuServer loading services...");
        createServices(dbAccess);

        LOGGER.info("GempukkuServer startup complete.");
    }

    private void createServices(DbAccess dbAccess) {
        // Services for multiple database access
        // Constructors should only take DAO objects and dbAccess as parameters
        _adminService = new AdminService(_playerDAO, _ipBanDAO, dbAccess);
        GameHistoryService gameHistoryService = new GameHistoryService(dbAccess);
        CollectionsManager collectionsManager =
                new CollectionsManager(_playerDAO, _collectionDAO, _transferDAO);

        // Multiple database access; these have some library properties but shouldn't
        LeagueMapper leagueMapper = new LeagueMapper(_cardBlueprintLibrary, _formatLibrary, _draftFormatLibrary);
        LeagueService leagueService = new LeagueService(collectionsManager, leagueMapper, dbAccess);
        TournamentService tournamentService = new TournamentService(_cardBlueprintLibrary, dbAccess);

        // GameRecorder is just kind of weird
        GameRecorder gameRecorder = new GameRecorder(gameHistoryService, _playerDAO);

        // Server objects; these should have as few properties as possible. Ideally they would not have other servers as properties.
        ChatServer chatServer = new ChatServer();
        GameServer gameServer = new GameServer(chatServer, gameRecorder, _cardBlueprintLibrary);
        HallServer hallServer =
                new HallServer(_adminService, _formatLibrary, chatServer, leagueService,
                        collectionsManager, tournamentService, gameServer, _cardBlueprintLibrary);

        _injectables.addValue(AdminService.class, _adminService);
        _injectables.addValue(GameHistoryService.class, gameHistoryService);
        _injectables.addValue(GameRecorder.class, gameRecorder);
        _injectables.addValue(CollectionsManager.class, collectionsManager);
        _injectables.addValue(LeagueService.class, leagueService);
        _injectables.addValue(TournamentService.class, tournamentService);
        _injectables.addValue(ChatServer.class, chatServer);
        _injectables.addValue(GameServer.class, gameServer);
        _injectables.addValue(HallServer.class, hallServer);

        hallServer.startServer();
        gameServer.startServer();
        chatServer.startServer();
    }


    ServerChannelInitializer getChannelInitializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setInjectableValues(_injectables);
        return new ServerChannelInitializer(mapper, _adminService);
    }
}