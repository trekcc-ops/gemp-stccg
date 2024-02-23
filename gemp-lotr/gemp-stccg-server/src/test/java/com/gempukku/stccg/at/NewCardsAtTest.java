package com.gempukku.stccg.at;

import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalCardGeneric;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.AwaitingDecisionType;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NewCardsAtTest extends AbstractAtTest {

    @Test
    public void reduceArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardGeneric legolas = new PhysicalCardGeneric(_game, 100, P1, _cardLibrary.getCardBlueprint("40_52"));
        PhysicalCardGeneric arrowsOfLight = new PhysicalCardGeneric(_game, 100, P1, _cardLibrary.getCardBlueprint("40_33"));
        PhysicalCardGeneric inquisitor = new PhysicalCardGeneric(_game, 100, P2, _cardLibrary.getCardBlueprint("1_268"));

        _game.getGameState().addCardToZone(inquisitor, Zone.SHADOW_CHARACTERS);

        _game.getGameState().addCardToZone(legolas, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(arrowsOfLight, Zone.HAND);

        skipMulligans();

        // Pass in fellowship
        playerDecided(P1, "");

        // Pass in shadow
        playerDecided(P2, "");

        // Pass in maneuver
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Play ArrowsOfLight
        playerDecided(P1, "0");
        playerDecided(P1, "0");
    }

    @Test
    public void playedTrigger() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardGeneric bruinenUnleashed = new PhysicalCardGeneric(_game, 100, P1, _cardLibrary.getCardBlueprint("40_37"));
        PhysicalCardGeneric legolas = new PhysicalCardGeneric(_game, 100, P1, _cardLibrary.getCardBlueprint("40_52"));
        PhysicalCardGeneric nazgul = new PhysicalCardGeneric(_game, 100, P2, _cardLibrary.getCardBlueprint("40_211"));

        _game.getGameState().addCardToZone(legolas, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(bruinenUnleashed, Zone.SUPPORT);
        _game.getGameState().addCardToZone(nazgul, Zone.HAND);

        skipMulligans();

        _game.getGameState().setTwilight(10);

        // Pass in fellowship
        playerDecided(P1, "");

        // Pass in shadow
        playerDecided(P2, "0");

        assertEquals(1, 0); // Should be some valid logic here instead
    }

    @Test
    public void choiceEffect() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardGeneric getOutOfTheShire = createCard(P1, "40_320");
        PhysicalCardGeneric merry = createCard(P1, "40_256");
        PhysicalCardGeneric nazgul = createCard(P2, "40_211");

        _game.getGameState().addCardToZone(getOutOfTheShire, Zone.HAND);
        _game.getGameState().addCardToZone(merry, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(nazgul, Zone.SHADOW_CHARACTERS);

        skipMulligans();

        // Pass in fellowship
        playerDecided(P1, "");

        // Pass in shadow
        playerDecided(P2, "");

        // Pass in maneuver
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Pass in archery
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Pass in assignment
        playerDecided(P1, "");
        playerDecided(P2, "");

        playerDecided(P1, merry.getCardId() + " " + nazgul.getCardId());

        playerDecided(P1, String.valueOf(merry.getCardId()));

        // Play Get Out of the Shire
        playerDecided(P1, "0");

        // Choose to cancel skirmish
        playerDecided(P1, "1");

        // We're in Fierce skirmishes
        assertEquals(Phase.ASSIGNMENT, _game.getGameState().getCurrentPhase());
        assertEquals(Zone.FREE_CHARACTERS, merry.getZone());
    }

    @Test
    public void conditionalEffect() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardGeneric getOutOfTheShire = createCard(P1, "40_320");
        PhysicalCardGeneric nazgul = createCard(P2, "40_211");

        _game.getGameState().addCardToZone(getOutOfTheShire, Zone.HAND);
        _game.getGameState().addCardToZone(nazgul, Zone.SHADOW_CHARACTERS);

        final PhysicalCard frodo = null;

        skipMulligans();

        // Pass in fellowship
        playerDecided(P1, "");

        // Pass in shadow
        playerDecided(P2, "");

        // Pass in maneuver
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Pass in archery
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Pass in assignment
        playerDecided(P1, "");
        playerDecided(P2, "");

        playerDecided(P1, frodo.getCardId() + " " + nazgul.getCardId());

        playerDecided(P1, String.valueOf(frodo.getCardId()));

        // Play Get Out of the Shire
        playerDecided(P1, "0");

        // No choice given
        assertNull(_userFeedback.getAwaitingDecision(P1));
    }

    @Test
    public void preventableEffect() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardGeneric boromir = createCard(P1, "40_101");
        PhysicalCardGeneric gauntlets = createCard(P1, "40_103");
        PhysicalCardGeneric nazgul = createCard(P2, "40_211");

        _game.getGameState().addCardToZone(boromir, Zone.SUPPORT);
        _game.getGameState().attachCard(gauntlets, boromir);
        _game.getGameState().addCardToZone(nazgul, Zone.SHADOW_CHARACTERS);

        skipMulligans();

        // Pass in fellowship
        playerDecided(P1, "");

        // Pass in shadow
        playerDecided(P2, "");

        // Pass in maneuver
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Pass in archery
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Pass in assignment
        playerDecided(P1, "");
        playerDecided(P2, "");

        playerDecided(P1, boromir.getCardId() + " " + nazgul.getCardId());

        playerDecided(P1, String.valueOf(boromir.getCardId()));

        final int twilightPool = _game.getGameState().getTwilightPool();

        // Use Gauntlets
        playerDecided(P1, "0");

        playerDecided(P2, "0");

        assertEquals(twilightPool - 1, _game.getGameState().getTwilightPool());
    }

    @Test
    public void costToEffect() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardGeneric celeborn = createCard(P1, "40_38");
        PhysicalCardGeneric celebornInDeck = createCard(P1, "40_38");
        PhysicalCardGeneric nazgul = createCard(P2, "40_211");

        _game.getGameState().addCardToZone(celeborn, Zone.SUPPORT);
        _game.getGameState().putCardOnTopOfDeck(celebornInDeck);
        _game.getGameState().addCardToZone(nazgul, Zone.SHADOW_CHARACTERS);

        skipMulligans();

        // Pass in fellowship
        playerDecided(P1, "");

        // Pass in shadow
        playerDecided(P2, "");

        // Use Celeborn
        playerDecided(P1, "0");
        // Pass on reveal
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Choose to discard
        playerDecided(P1, "0");

        assertEquals(Zone.DISCARD, celebornInDeck.getZone());
        assertEquals(1, 0); //Should be something real
    }

    @Test
    public void costToEffectPass() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardGeneric celeborn = createCard(P1, "40_38");
        PhysicalCardGeneric celebornInDeck = createCard(P1, "40_38");
        PhysicalCardGeneric nazgul = createCard(P2, "40_211");

        _game.getGameState().addCardToZone(celeborn, Zone.SUPPORT);
        _game.getGameState().putCardOnTopOfDeck(celebornInDeck);
        _game.getGameState().addCardToZone(nazgul, Zone.SHADOW_CHARACTERS);

        skipMulligans();

        // Pass in fellowship
        playerDecided(P1, "");

        // Pass in shadow
        playerDecided(P2, "");

        // Use Celeborn
        playerDecided(P1, "0");
        // Pass on reveal
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Choose not to discard
        playerDecided(P1, "1");

        assertEquals(Zone.DRAW_DECK, celebornInDeck.getZone());
    }

    @Test
    public void checkingEventCostsAsRequirements() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        final PhysicalCardGeneric battleFever = createCard(P1, "40_5");
        final PhysicalCardGeneric gimli = createCard(P1, "40_18");
        PhysicalCardGeneric nazgul = createCard(P2, "40_211");


        _game.getGameState().addCardToZone(gimli, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(battleFever, Zone.HAND);
        _game.getGameState().addCardToZone(nazgul, Zone.SHADOW_CHARACTERS);

        skipMulligans();

        // Pass in fellowship
        playerDecided(P1, "");

        // Pass in shadow
        playerDecided(P2, "");

//Pass in maneuver
        playerDecided(P1, "");
        playerDecided(P2, "");

//Pass in archery
        playerDecided(P1, "");
        playerDecided(P2, "");

//Pass in assignment
        playerDecided(P1, "");
        playerDecided(P2, "");

        playerDecided(P1, gimli.getCardId() + " " + nazgul.getCardId());
        playerDecided(P1, String.valueOf(gimli.getCardId()));

        assertNull(getCardActionId(P1, "Play Battle Fever"));
    }

    @Test
    public void discardCardEffect() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        final PhysicalCardGeneric blackBreath = createCard(P2, "40_183");
        final PhysicalCardGeneric athelas = createCard(P1, "40_313");
        final PhysicalCardGeneric athelasInHand = createCard(P1, "40_313");
        PhysicalCardGeneric aragorn = createCard(P1, "40_94");

        _game.getGameState().addCardToZone(aragorn, Zone.FREE_CHARACTERS);
        _game.getGameState().attachCard(blackBreath, aragorn);
        _game.getGameState().addCardToZone(athelas, Zone.HAND);
        _game.getGameState().addCardToZone(athelasInHand, Zone.HAND);

        skipMulligans();

        // Play athelas
        playerDecided(P1, "0");
        // Attach to Aragorn
        playerDecided(P1, String.valueOf(aragorn.getCardId()));

        playerDecided(P1, "0");

        playerDecided(P1, "0");

        assertEquals(Zone.DISCARD, blackBreath.getZone());
    }

    @Test
    public void strengthBonusDependingOnCharacterPlayedOn() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        final PhysicalCardGeneric gandalf = createCard(P1, "40_70");
        final PhysicalCardGeneric bolsteredSpirits = createCard(P1, "40_67");
        PhysicalCardGeneric nazgul = createCard(P2, "40_211");

        _game.getGameState().addCardToZone(gandalf, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(bolsteredSpirits, Zone.HAND);
        _game.getGameState().addCardToZone(nazgul, Zone.SHADOW_CHARACTERS);

        skipMulligans();

        // Pass in fellowship
        playerDecided(P1, "");

        // Pass in shadow
        playerDecided(P2, "");

//Pass in maneuver
        playerDecided(P1, "");
        playerDecided(P2, "");

//Pass in archery
        playerDecided(P1, "");
        playerDecided(P2, "");

//Pass in assignment
        playerDecided(P1, "");
        playerDecided(P2, "");

        playerDecided(P1, gandalf.getCardId() + " " + nazgul.getCardId());
        playerDecided(P1, String.valueOf(gandalf.getCardId()));

        playerDecided(P1, "0");
        playerDecided(P1, "");

        assertEquals(7 + 3, _game.getModifiersQuerying().getStrength(gandalf));
    }

    @Test
    public void strengthBonusDependingOnCharacterPlayedOn2() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        final PhysicalCardGeneric gandalf = createCard(P1, "40_70");
        final PhysicalCardGeneric boromir = createCard(P1, "40_101");
        final PhysicalCardGeneric goBackToTheShadows = createCard(P1, "40_312");
        final PhysicalCardGeneric nazgul = createCard(P2, "40_211");

        _game.getGameState().addCardToZone(gandalf, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(boromir, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(goBackToTheShadows, Zone.HAND);
        _game.getGameState().addCardToZone(nazgul, Zone.SHADOW_CHARACTERS);

        skipMulligans();

        // Pass in fellowship
        playerDecided(P1, "");

        // Pass in shadow
        playerDecided(P2, "");

//Pass in maneuver
        playerDecided(P1, "");
        playerDecided(P2, "");

//Pass in archery
        playerDecided(P1, "");
        playerDecided(P2, "");

//Pass in assignment
        playerDecided(P1, "");
        playerDecided(P2, "");

        playerDecided(P1, boromir.getCardId() + " " + nazgul.getCardId());
        playerDecided(P1, String.valueOf(boromir.getCardId()));

        playerDecided(P1, "0");

        assertEquals(14 - 3, _game.getModifiersQuerying().getStrength(nazgul));
    }

    @Test
    public void mathProgrammingDiscardBoth() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        final PhysicalCardGeneric gandalf = createCard(P1, "40_70");
        final PhysicalCardGeneric discerment = createCard(P1, "40_68");
        final PhysicalCardGeneric blackBreath = createCard(P2, "40_183");
        final PhysicalCardGeneric blackBreath2 = createCard(P2, "40_183");

        _game.getGameState().addCardToZone(gandalf, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(discerment, Zone.HAND);
        _game.getGameState().attachCard(blackBreath, gandalf);
        _game.getGameState().attachCard(blackBreath2, gandalf);

        skipMulligans();

        playerDecided(P1, "0");
        playerDecided(P1, "2");
        playerDecided(P1, "");

        playerDecided(P1, String.valueOf(blackBreath.getCardId()));
        playerDecided(P1, String.valueOf(blackBreath2.getCardId()));

        assertEquals(Zone.DISCARD, blackBreath.getZone());
        assertEquals(Zone.DISCARD, blackBreath2.getZone());
    }

    @Test
    public void mathProgrammingDiscardOne() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        final PhysicalCardGeneric gandalf = createCard(P1, "40_70");
        final PhysicalCardGeneric discerment = createCard(P1, "40_68");
        final PhysicalCardGeneric blackBreath = createCard(P2, "40_183");
        final PhysicalCardGeneric blackBreath2 = createCard(P2, "40_183");

        _game.getGameState().addCardToZone(gandalf, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(discerment, Zone.HAND);
        _game.getGameState().attachCard(blackBreath, gandalf);
        _game.getGameState().attachCard(blackBreath2, gandalf);

        skipMulligans();

        playerDecided(P1, "0");
        playerDecided(P1, "1");
        playerDecided(P1, "");

        playerDecided(P1, String.valueOf(blackBreath.getCardId()));
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, _userFeedback.getAwaitingDecision(P1).getDecisionType());

        assertEquals(Zone.DISCARD, blackBreath.getZone());
        assertEquals(Zone.ATTACHED, blackBreath2.getZone());
    }

    @Test
    public void strengthBonus() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        final PhysicalCardGeneric grimbeorn = createCard(P1, "14_6");
        final PhysicalCardGeneric nazgulInHand = createCard(P1, "40_211");
        final PhysicalCardGeneric nazgul = createCard(P2, "40_211");

        _game.getGameState().addCardToZone(grimbeorn, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(nazgul, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(nazgulInHand, Zone.HAND);

        skipMulligans();

        playerDecided(P1, "");
        playerDecided(P2, "");

        playerDecided(P1, "0");
        playerDecided(P1, "");
    }

    @Test
    public void roamingDiscount() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        final PhysicalCardGeneric orcAssassin1 = createCard(P2, "1_262");
        final PhysicalCardGeneric orcAssassin2 = createCard(P2, "1_262");

        _game.getGameState().addCardToZone(orcAssassin1, Zone.HAND);
        _game.getGameState().addCardToZone(orcAssassin2, Zone.HAND);

        skipMulligans();

        _game.getGameState().setTwilight(10);

        playerDecided(P1, "");

        int twilight = _game.getGameState().getTwilightPool();
        playerDecided(P2, getCardActionId(P2, "Play Orc Assassin"));
        assertEquals(twilight - 4, _game.getGameState().getTwilightPool());

        playerDecided(P2, getCardActionId(P2, "Play Orc Assassin"));
        assertEquals(twilight - 4 - 3, _game.getGameState().getTwilightPool());

        assertEquals(Zone.SHADOW_CHARACTERS, orcAssassin1.getZone());
        assertEquals(Zone.SHADOW_CHARACTERS, orcAssassin2.getZone());
    }

    @Test
    public void spotCountChange() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        final PhysicalCardGeneric merry = createCard(P1, "40_257");
        final PhysicalCardGeneric bilbosPipe = createCard(P1, "40_244");
        final PhysicalCardGeneric pipeweed = createCard(P1, "40_255");

        _game.getGameState().addCardToZone(merry, Zone.HAND);
        _game.getGameState().addCardToZone(pipeweed, Zone.SUPPORT);
        _game.getGameState().addCardToZone(bilbosPipe, Zone.HAND);

        skipMulligans();

        playerDecided(P1, getCardActionId(P1, "Play Merry"));
        playerDecided(P1, getCardActionId(P1, "Play Bilbo's Pipe"));

        playerDecided(P1, String.valueOf(merry.getCardId()));
        playerDecided(P1, getCardActionId(P1, "Use Bilbo's Pipe"));
        assertEquals("2", _userFeedback.getAwaitingDecision(P1).getDecisionParameters().get("max")[0]);
    }

}
