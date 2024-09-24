package com.gempukku.stccg.tournament;

import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.db.User;

import java.io.IOException;
import java.sql.SQLException;

public interface TournamentQueue {
    int getCost();

    String getFormat();

    CollectionType getCollectionType();

    String getTournamentQueueName();

    String getPrizesDescription();

    String getPairingDescription();

    String getStartCondition();

    boolean isRequiresDeck();

    boolean process(TournamentQueueCallback tournamentQueueCallback, CollectionsManager collectionsManager) throws SQLException, IOException;

    void joinPlayer(CollectionsManager collectionsManager, User player, CardDeck deck) throws SQLException, IOException;

    void leavePlayer(CollectionsManager collectionsManager, User player) throws SQLException, IOException;

    void leaveAllPlayers(CollectionsManager collectionsManager) throws SQLException, IOException;

    int getPlayerCount();

    boolean isPlayerSignedUp(String player);

    boolean isJoinable();
}
