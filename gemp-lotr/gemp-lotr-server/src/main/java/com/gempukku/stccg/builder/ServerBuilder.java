package com.gempukku.stccg.builder;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.collection.CollectionSerializer;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.collection.TransferDAO;
import com.gempukku.stccg.db.*;
import com.gempukku.stccg.draft.SoloDraftDefinitions;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.adventure.AdventureLibrary;
import com.gempukku.stccg.adventure.DefaultAdventureLibrary;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.hall.HallServer;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.merchant.MerchantService;
import com.gempukku.stccg.packs.DraftPackStorage;
import com.gempukku.stccg.packs.ProductLibrary;
import com.gempukku.stccg.service.AdminService;
import com.gempukku.stccg.service.LoggedUserHolder;
import com.gempukku.stccg.tournament.*;

import java.lang.reflect.Type;
import java.util.Map;
import org.apache.log4j.Logger;


public class ServerBuilder {
    private static final Logger logger = Logger.getLogger(ServerBuilder.class);
    public static void CreatePrerequisites(Map<Type, Object> objectMap) {
        logger.debug("Calling CreatePrerequisites function");
        final CardBlueprintLibrary library = new CardBlueprintLibrary();
        objectMap.put(CardBlueprintLibrary.class, library);
        objectMap.put(ProductLibrary.class, new ProductLibrary(library));

        LoggedUserHolder loggedUserHolder = new LoggedUserHolder();
        loggedUserHolder.start();
        objectMap.put(LoggedUserHolder.class, loggedUserHolder);

        CollectionSerializer collectionSerializer = new CollectionSerializer();
        objectMap.put(CollectionSerializer.class, collectionSerializer);
        logger.debug("Ending CreatePrerequisites function");
    }

    public static void CreateServices(Map<Type, Object> objectMap) {
        logger.debug("Calling CreateServices function");
        objectMap.put(AdventureLibrary.class,
                new DefaultAdventureLibrary());

        objectMap.put(FormatLibrary.class,
                new FormatLibrary(
                        extract(objectMap, AdventureLibrary.class),
                        extract(objectMap, CardBlueprintLibrary.class)));

        objectMap.put(GameHistoryService.class,
                new GameHistoryService(
                        extract(objectMap, GameHistoryDAO.class)));
        objectMap.put(GameRecorder.class,
                new GameRecorder(
                        extract(objectMap, GameHistoryService.class),
                        extract(objectMap, PlayerDAO.class)));

        objectMap.put(CollectionsManager.class,
                new CollectionsManager(
                        extract(objectMap, PlayerDAO.class),
                        extract(objectMap, CollectionDAO.class),
                        extract(objectMap, TransferDAO.class),
                        extract(objectMap, CardBlueprintLibrary.class)));

        objectMap.put(SoloDraftDefinitions.class,
                new SoloDraftDefinitions(
                    extract(objectMap, CollectionsManager.class),
                    extract(objectMap, CardBlueprintLibrary.class),
                    extract(objectMap, FormatLibrary.class)
                ));

        objectMap.put(LeagueService.class,
                new LeagueService(
                        extract(objectMap, LeagueDAO.class),
                        extract(objectMap, LeagueMatchDAO.class),
                        extract(objectMap, LeagueParticipationDAO.class),
                        extract(objectMap, CollectionsManager.class),
                        extract(objectMap, CardBlueprintLibrary.class),
                        extract(objectMap, FormatLibrary.class),
                        extract(objectMap, SoloDraftDefinitions.class)));

        objectMap.put(AdminService.class,
                new AdminService(
                        extract(objectMap, PlayerDAO.class),
                        extract(objectMap, IpBanDAO.class),
                        extract(objectMap, LoggedUserHolder.class)
                ));

        TournamentPrizeSchemeRegistry tournamentPrizeSchemeRegistry = new TournamentPrizeSchemeRegistry();
        PairingMechanismRegistry pairingMechanismRegistry = new PairingMechanismRegistry();

        objectMap.put(TournamentService.class,
                new TournamentService(
                        extract(objectMap, CollectionsManager.class),
                        extract(objectMap, ProductLibrary.class),
                        new DraftPackStorage(),
                        pairingMechanismRegistry,
                        tournamentPrizeSchemeRegistry,
                        extract(objectMap, TournamentDAO.class),
                        extract(objectMap, TournamentPlayerDAO.class),
                        extract(objectMap, TournamentMatchDAO.class),
                        extract(objectMap, CardBlueprintLibrary.class)));

        objectMap.put(MerchantService.class,
                new MerchantService(
                        extract(objectMap, CardBlueprintLibrary.class),
                        extract(objectMap, CollectionsManager.class)));

        objectMap.put(ChatServer.class, new ChatServer(
                extract(objectMap, IgnoreDAO.class),
                extract(objectMap, PlayerDAO.class)));

        objectMap.put(LotroServer.class,
                new LotroServer(
                        extract(objectMap, DeckDAO.class),
                        extract(objectMap, CardBlueprintLibrary.class),
                        extract(objectMap, ChatServer.class),
                        extract(objectMap, GameRecorder.class)));

        objectMap.put(HallServer.class,
                new HallServer(
                        extract(objectMap, IgnoreDAO.class),
                        extract(objectMap, LotroServer.class),
                        extract(objectMap, ChatServer.class),
                        extract(objectMap, LeagueService.class),
                        extract(objectMap, TournamentService.class),
                        extract(objectMap, CardBlueprintLibrary.class),
                        extract(objectMap, FormatLibrary.class),
                        extract(objectMap, CollectionsManager.class),
                        extract(objectMap, AdminService.class),
                        tournamentPrizeSchemeRegistry,
                        pairingMechanismRegistry
                ));
        logger.debug("Ending CreateServices function");
    }

    private static <T> T extract(Map<Type, Object> objectMap, Class<T> clazz) {
        T result = (T) objectMap.get(clazz);
        if (result == null)
            throw new RuntimeException("Unable to find class " + clazz.getName());
        return result;
    }

    public static void StartServers(Map<Type, Object> objectMap) {
        logger.debug("Function StartServers - starting HallServer");
        extract(objectMap, HallServer.class).startServer();
        logger.debug("Function StartServers - starting LotroServer");
        extract(objectMap, LotroServer.class).startServer();
        logger.debug("Function StartServers - starting ChatServer");
        extract(objectMap, ChatServer.class).startServer();
    }

    public static void StopServers(Map<Type, Object> objectMap) {
        extract(objectMap, HallServer.class).stopServer();
        extract(objectMap, LotroServer.class).stopServer();
        extract(objectMap, ChatServer.class).stopServer();
    }
}
