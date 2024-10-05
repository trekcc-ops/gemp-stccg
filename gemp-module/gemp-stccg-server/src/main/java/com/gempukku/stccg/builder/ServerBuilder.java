package com.gempukku.stccg.builder;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.collection.TransferDAO;
import com.gempukku.stccg.db.*;
import com.gempukku.stccg.draft.SoloDraftDefinitions;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.game.GameHistoryService;
import com.gempukku.stccg.game.GameRecorder;
import com.gempukku.stccg.game.GameServer;
import com.gempukku.stccg.hall.HallServer;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.merchant.MerchantService;
import com.gempukku.stccg.packs.DraftPackStorage;
import com.gempukku.stccg.packs.ProductLibrary;
import com.gempukku.stccg.service.AdminService;
import com.gempukku.stccg.service.LoggedUserHolder;
import com.gempukku.stccg.tournament.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Map;


public class ServerBuilder {
    private static final Logger LOGGER = LogManager.getLogger(ServerBuilder.class);
    public static void CreatePrerequisites(Map<Type, Object> objectMap) {
        LOGGER.debug("Calling CreatePrerequisites function");
        final CardBlueprintLibrary library = new CardBlueprintLibrary();
        objectMap.put(CardBlueprintLibrary.class, library);
        objectMap.put(ProductLibrary.class, new ProductLibrary(library));

        LoggedUserHolder loggedUserHolder = new LoggedUserHolder();
        loggedUserHolder.start();
        objectMap.put(LoggedUserHolder.class, loggedUserHolder);
        LOGGER.debug("Ending CreatePrerequisites function");
    }

    public static void CreateServices(Map<Type, Object> objects) {
        LOGGER.debug("Calling CreateServices function");
        objects.put(FormatLibrary.class,
                new FormatLibrary(extract(objects, CardBlueprintLibrary.class)));

        objects.put(GameHistoryService.class,
                new GameHistoryService(
                        extract(objects, GameHistoryDAO.class)));
        objects.put(GameRecorder.class,
                new GameRecorder(
                        extract(objects, GameHistoryService.class),
                        extract(objects, PlayerDAO.class)));

        objects.put(CollectionsManager.class,
                new CollectionsManager(
                        extract(objects, PlayerDAO.class),
                        extract(objects, CollectionDAO.class),
                        extract(objects, TransferDAO.class),
                        extract(objects, CardBlueprintLibrary.class)));

        objects.put(SoloDraftDefinitions.class,
                new SoloDraftDefinitions(
                    extract(objects, CollectionsManager.class),
                    extract(objects, CardBlueprintLibrary.class),
                    extract(objects, FormatLibrary.class)
                ));

        objects.put(LeagueService.class,
                new LeagueService(
                        extract(objects, LeagueDAO.class),
                        extract(objects, LeagueMatchDAO.class),
                        extract(objects, LeagueParticipationDAO.class),
                        extract(objects, CollectionsManager.class),
                        extract(objects, CardBlueprintLibrary.class),
                        extract(objects, FormatLibrary.class),
                        extract(objects, SoloDraftDefinitions.class)));

        objects.put(AdminService.class,
                new AdminService(
                        extract(objects, PlayerDAO.class),
                        extract(objects, IpBanDAO.class),
                        extract(objects, LoggedUserHolder.class)
                ));

        TournamentPrizeSchemeRegistry tournamentPrizeSchemeRegistry = new TournamentPrizeSchemeRegistry();
        PairingMechanismRegistry pairingMechanismRegistry = new PairingMechanismRegistry();

        objects.put(TournamentService.class,
                new TournamentService(
                        extract(objects, CollectionsManager.class),
                        extract(objects, ProductLibrary.class),
                        new DraftPackStorage(),
                        pairingMechanismRegistry,
                        tournamentPrizeSchemeRegistry,
                        extract(objects, TournamentDAO.class),
                        extract(objects, TournamentPlayerDAO.class),
                        extract(objects, TournamentMatchDAO.class),
                        extract(objects, CardBlueprintLibrary.class)));

        objects.put(MerchantService.class,
                new MerchantService(
                        extract(objects, CardBlueprintLibrary.class),
                        extract(objects, CollectionsManager.class)));

        objects.put(ChatServer.class, new ChatServer(
                extract(objects, IgnoreDAO.class),
                extract(objects, PlayerDAO.class)));

        objects.put(GameServer.class,
                new GameServer(
                        extract(objects, DeckDAO.class),
                        extract(objects, CardBlueprintLibrary.class),
                        extract(objects, ChatServer.class),
                        extract(objects, GameRecorder.class)));

        objects.put(HallServer.class,
                new HallServer(
                        extract(objects, IgnoreDAO.class),
                        extract(objects, GameServer.class),
                        extract(objects, ChatServer.class),
                        extract(objects, LeagueService.class),
                        extract(objects, TournamentService.class),
                        extract(objects, CardBlueprintLibrary.class),
                        extract(objects, FormatLibrary.class),
                        extract(objects, CollectionsManager.class),
                        extract(objects, AdminService.class),
                        tournamentPrizeSchemeRegistry,
                        pairingMechanismRegistry
                ));
        LOGGER.debug("Ending CreateServices function");
    }

    private static <T> T extract(Map<Type, Object> objectMap, Class<T> clazz) {
        T result = (T) objectMap.get(clazz);
        if (result == null)
            throw new RuntimeException("Unable to find class " + clazz.getName());
        return result;
    }

    public static void StartServers(Map<Type, Object> objectMap) {
        LOGGER.debug("Function StartServers - starting HallServer");
        extract(objectMap, HallServer.class).startServer();
        LOGGER.debug("Function StartServers - starting GameServer");
        extract(objectMap, GameServer.class).startServer();
        LOGGER.debug("Function StartServers - starting ChatServer");
        extract(objectMap, ChatServer.class).startServer();
    }

    public static void StopServers(Map<Type, Object> objectMap) {
        extract(objectMap, HallServer.class).stopServer();
        extract(objectMap, GameServer.class).stopServer();
        extract(objectMap, ChatServer.class).stopServer();
    }
}
