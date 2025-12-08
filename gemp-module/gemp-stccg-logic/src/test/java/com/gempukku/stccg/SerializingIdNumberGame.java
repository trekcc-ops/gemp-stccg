package com.gempukku.stccg;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.cardgroup.PhysicalCardGroup;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.ArrayList;
import java.util.List;

public class SerializingIdNumberGame {

    @JsonProperty("allCards")
    private final List<PhysicalCard> _cards = new ArrayList<>();

    @JsonProperty("selectedCards")
    @JsonIdentityReference(alwaysAsId=true)
    private final List<PhysicalCard> _selectedCards = new ArrayList<>();

    @JsonProperty("cardGroup")
    private final PhysicalCardGroup<PhysicalCard> _cardGroup;

    @JsonProperty("players")
    private final List<Player> _players = new ArrayList<>();

    public SerializingIdNumberGame(DefaultGame cardGame) {
        _cards.addAll(cardGame.getGameState().getAllCardsInGame());
        _selectedCards.add(_cards.getFirst());
        _cardGroup = new PhysicalCardGroup<>(_selectedCards);
        _players.addAll(cardGame.getPlayers());
    }

    public SerializingIdNumberGame(@JsonProperty("allCards")
                                   List<PhysicalCard> allCards,
                                   @JsonIdentityReference(alwaysAsId=true)
                                   @JsonProperty("selectedCards")
                                   List<PhysicalCard> selectedCards,
                                   @JsonProperty("cardGroup")
                                   PhysicalCardGroup<PhysicalCard> cardGroup,
                                   @JsonProperty("players")
                                   List<Player> players) {
        _cards.addAll(allCards);
        _selectedCards.addAll(selectedCards);
        _cardGroup = cardGroup;
        _players.addAll(players);
    }



}