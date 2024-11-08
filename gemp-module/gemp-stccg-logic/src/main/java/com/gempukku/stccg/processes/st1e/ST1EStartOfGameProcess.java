package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;

import java.util.*;

public class ST1EStartOfGameProcess extends ST1EGameProcess {
    public ST1EStartOfGameProcess(ST1EGame game) {
        super(new HashSet<>(), game);
    }

    @Override
    public void process() {
        Set<String> players = _game.getPlayerIds();
        _game.takeSnapshot("Start of game");
        for (String player : players) {
            List<PhysicalCard> doorwaySeeds = new LinkedList<>();
            for (PhysicalCard seedCard : _game.getGameState().getSeedDeck(player)) {
                if (seedCard.getCardType() == CardType.DOORWAY)
                    doorwaySeeds.add(seedCard);
            }
            for (PhysicalCard card : doorwaySeeds) {
                _game.getGameState().removeCardsFromZone(player, Collections.singleton(card));
                _game.getGameState().addCardToZone(card, Zone.HAND);
            }
        }
    }

    @Override
    public GameProcess getNextProcess() {
        return new ST1EDoorwaySeedPhaseProcess(_game);
    }
}