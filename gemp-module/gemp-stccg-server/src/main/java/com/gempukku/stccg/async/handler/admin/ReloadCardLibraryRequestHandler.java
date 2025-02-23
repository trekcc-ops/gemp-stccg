package com.gempukku.stccg.async.handler.admin;

import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.chat.ChatServer;

public class ReloadCardLibraryRequestHandler extends AdminRequestHandlerNew implements UriRequestHandler {
    private final static long CARD_LOAD_SLEEP_TIME = 6000;

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        validateAdmin(request);
        ChatServer chatServer = serverObjects.getChatServer();
        CardBlueprintLibrary cardLibrary = serverObjects.getCardBlueprintLibrary();

        chatServer.sendSystemMessageToAllUsers(
                "Server is reloading card definitions.  This will impact game speed until it is complete.");
        Thread.sleep(CARD_LOAD_SLEEP_TIME);
        serverObjects.getCardBlueprintLibrary().reloadAllDefinitions();
        serverObjects.getProductLibrary().ReloadPacks();
        serverObjects.getFormatLibrary().reloadFormats(cardLibrary);
        serverObjects.getFormatLibrary().reloadSealedTemplates();
        chatServer.sendSystemMessageToAllUsers(
                "Card definition reload complete.  If you are mid-game and you notice any oddities, reload the page " +
                        "and please let the mod team know in the game hall ASAP if the problem doesn't go away.");
        responseWriter.writeJsonOkResponse();
    }
}