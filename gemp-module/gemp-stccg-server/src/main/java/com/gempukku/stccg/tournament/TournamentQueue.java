package com.gempukku.stccg.tournament;

import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;

import java.io.IOException;
import java.sql.SQLException;

public interface TournamentQueue {
    int getCost();

    GameFormat getGameFormat(FormatLibrary formatLibrary);

    CollectionType getCollectionType();

    String getTournamentQueueName();

    String getPrizesDescription();

    String getPairingDescription();

    String getStartCondition();

    boolean isRequiresDeck();

    boolean process(TournamentQueueCallback tournamentQueueCallback, CollectionsManager collectionsManager)
            throws SQLException, IOException;

    void joinPlayer(CollectionsManager collectionsManager, User player, CardDeck deck) throws SQLException, IOException;

    void leavePlayer(CollectionsManager collectionsManager, User player) throws SQLException, IOException;

    void leaveAllPlayers(CollectionsManager collectionsManager) throws SQLException, IOException;

    int getPlayerCount();

    boolean isPlayerSignedUp(String player);

    boolean isJoinable();
}