package com.gempukku.stccg.player;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.cardgroup.PhysicalCardGroup;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;

import java.util.*;

@JsonIncludeProperties({ "playerId", "score", "turnNumber", "decked", "cardGroups" })
@JsonPropertyOrder({ "playerId", "score", "turnNumber", "decked", "cardGroups" })
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

    @JsonProperty("decked")
    private boolean getDecked() {
        return _playerRequested.isDecked();
    }

    @JsonProperty("cardGroups")
    private Map<Zone, CardGroupView> getCardGroups() {
        Map<Zone, CardGroupView> result = new HashMap<>();
        for (Zone zone : _playerRequested.getCardGroupZones()) {
            PhysicalCardGroup<? extends PhysicalCard> cardGroup = _playerRequested.getCardGroup(zone);
            if (zone.isPublic() ||
                    (_playerRequested.getPlayerId().equals(_requestingPlayerId) && zone.isVisibleByOwner())) {
                result.put(zone, new PublicCardGroupView(cardGroup));
            } else {
                result.put(zone, new PrivateCardGroupView(cardGroup));
            }
        }
        return result;
    }


    @JsonIncludeProperties({ "cardCount", "cardIds" })
    private interface CardGroupView {

    }

    private class PrivateCardGroupView implements CardGroupView {

        private static final int ANONYMOUS_CARD_ID = -99;
        @JsonProperty("cardCount")
        int _cardCount;

        @JsonProperty("cardIds")
        final Collection<Integer> _cardIds;

        PrivateCardGroupView(PhysicalCardGroup<? extends PhysicalCard> group) {
            _cardCount = group.size();
            _cardIds = Collections.nCopies(_cardCount, ANONYMOUS_CARD_ID);
        }
    }

    private class PublicCardGroupView implements CardGroupView {
        @JsonProperty("cardCount")
        int _cardCount;
        @JsonProperty("cardIds")
        @JsonIdentityReference(alwaysAsId=true)
        List<? extends PhysicalCard> _cards;

        PublicCardGroupView(PhysicalCardGroup<? extends PhysicalCard> group) {
            _cardCount = group.size();
            _cards = group.getCards();
        }
    }

}