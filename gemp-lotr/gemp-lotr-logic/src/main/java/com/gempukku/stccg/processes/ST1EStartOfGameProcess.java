package com.gempukku.stccg.processes;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.CardType;
import com.gempukku.stccg.common.Zone;
import com.gempukku.stccg.game.ST1EGame;

import java.util.*;

public class ST1EStartOfGameProcess implements GameProcess<ST1EGame> {
    Set<String> _players;
    public ST1EStartOfGameProcess() {
    }

    @Override
    public void process(ST1EGame game) {
        _players = game.getPlayers();
        for (String player : _players) {
            List<PhysicalCard> doorwaySeeds = new LinkedList<>();
            for (PhysicalCard seedCard : game.getGameState().getSeedDeck(player)) {
                if (seedCard.getBlueprint().getCardType() == CardType.DOORWAY)
                    doorwaySeeds.add(seedCard);
            }
            for (PhysicalCard card : doorwaySeeds) {
                game.getGameState().removeCardsFromZone(player, Collections.singleton(card));
                game.getGameState().addCardToZone(game, card, Zone.HAND);
            }
        }
    }

    @Override
    public GameProcess<ST1EGame> getNextProcess() {
        return new ST1EDoorwaySeedPhaseProcess(_players, new HashSet<>());
    }
}
