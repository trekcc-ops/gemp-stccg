package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.packs.ProductLibrary;

public class ReloadCardLibraryRequestHandler implements UriRequestHandler, AdminRequestHandler {
    private final static long CARD_LOAD_SLEEP_TIME = 6000;
    private final CardBlueprintLibrary _cardBlueprintLibrary;
    private final FormatLibrary _formatLibrary;
    private final ProductLibrary _productLibrary;
    private final ChatServer _chatServer;


    ReloadCardLibraryRequestHandler(@JacksonInject CardBlueprintLibrary cardBlueprintLibrary,
                                    @JacksonInject FormatLibrary formatLibrary,
                                    @JacksonInject ProductLibrary productLibrary,
                                    @JacksonInject ChatServer chatServer) {
        _formatLibrary = formatLibrary;
        _cardBlueprintLibrary = cardBlueprintLibrary;
        _productLibrary = productLibrary;
        _chatServer = chatServer;
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        validateAdmin(request);

        _chatServer.sendSystemMessageToAllUsers(
                "Server is reloading card definitions.  This will impact game speed until it is complete.");
        Thread.sleep(CARD_LOAD_SLEEP_TIME);
        _cardBlueprintLibrary.reloadAllDefinitions();
        _productLibrary.ReloadPacks();
        _formatLibrary.reloadFormats(_cardBlueprintLibrary);
        _formatLibrary.reloadSealedTemplates();
        _chatServer.sendSystemMessageToAllUsers(
                "Card definition reload complete.  If you are mid-game and you notice any oddities, reload the page " +
                        "and please let the mod team know in the game hall ASAP if the problem doesn't go away.");
        responseWriter.writeJsonOkResponse();
    }
}