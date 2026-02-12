package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.EquipmentCard;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Blueprint_101_058_KlingonDisruptor_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private EquipmentCard disruptor;
    private PersonnelCard picard;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        outpost = builder.addOutpost(Affiliation.KLINGON, P1);
        disruptor = builder.addCardInHand("101_058", "Klingon Disruptor", P1, EquipmentCard.class);
        picard = builder.addCardAboardShipOrFacility("101_215", "Jean-Luc Picard", P1, outpost, PersonnelCard.class);
        builder.addCardAboardShipOrFacility("101_270", "Klag", P1, outpost, PersonnelCard.class);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }
    
    @Test
    public void disruptorTest() throws Exception {
        initializeGame();

        assertEquals(6, picard.getStrength(_game));
        playCard(P1, disruptor);
        assertEquals(8, picard.getStrength(_game));
    }
}