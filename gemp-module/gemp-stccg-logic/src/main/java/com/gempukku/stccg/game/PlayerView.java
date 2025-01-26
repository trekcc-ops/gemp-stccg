package com.gempukku.stccg.game;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.cardgroup.PhysicalCardGroup;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.JsonViews;
import com.gempukku.stccg.common.filterable.Zone;

import java.util.*;

@JsonIncludeProperties({ "playerId", "score", "turnNumber", "decked", "cardGroups" })
@JsonPropertyOrder({ "playerId", "score", "turnNumber", "decked", "cardGroups" })
@JsonView(JsonViews.Public.class)
public class PlayerView {

    private final String _requestingPlayerId;
    private final Player _playerRequested;

    public PlayerView(Player player, String requestingPlayerId) {
        _requestingPlayerId = requestingPlayerId;
        _playerRequested = player;
    }

    @JsonProperty("playerId")
    private String getPlayerId() {
        return _playerRequested.getPlayerId();
    }

    @JsonProperty("score")
    private int getScore() {
        return _playerRequested.getScore();
    }

    @JsonProperty("turnNumber")
    private int getTurnNumber() {
        return _playerRequested.getTurnNumber();
    }

    @JsonProperty("decked")
    private boolean getDecked() {
        return _playerRequested.isDecked();
    }

    @JsonProperty("cardGroups")
    private Map<Zone, CardGroupView> getCardGroups() {
        Map<Zone, CardGroupView> result = new HashMap<>();
        for (Zone zone : _playerRequested.getCardGroupZones()) {
            PhysicalCardGroup cardGroup = _playerRequested.getCardGroup(zone);
            if (zone.isPublic() || _playerRequested.getPlayerId().equals(_requestingPlayerId)) {
                result.put(zone, new PublicCardGroupView(cardGroup));
            } else {
                result.put(zone, new PrivateCardGroupView(cardGroup));
            }
        }
        return result;
    }


    @JsonIncludeProperties({ "cardCount", "cardIds" })
    @JsonView(JsonViews.Public.class)
    private interface CardGroupView {

    }

    private class PrivateCardGroupView implements CardGroupView {

        private static final int ANONYMOUS_CARD_ID = -99;
        @JsonProperty("cardCount")
        int _cardCount;

        @JsonProperty("cardIds")
        final Collection<Integer> _cardIds;

        PrivateCardGroupView(PhysicalCardGroup group) {
            _cardCount = group.size();
            _cardIds = Collections.nCopies(_cardCount, ANONYMOUS_CARD_ID);
        }
    }

    private class PublicCardGroupView implements CardGroupView {
        @JsonProperty("cardCount")
        int _cardCount;
        @JsonProperty("cardIds")
        @JsonIdentityReference(alwaysAsId=true)
        List<PhysicalCard> _cards;

        PublicCardGroupView(PhysicalCardGroup group) {
            _cardCount = group.size();
            _cards = group.getCards();
        }
    }

}