package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Signet;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_03_038_ErrataTests
{
    protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("aragorn", "53_38");
                    put("gimli", "1_13");

                    put("minion", "1_268");
                    put("tracker", "1_270");
                }}
        );
    }

    @Test
    public void AragornHasRanger() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");


    }

    @Test
    public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 1E
         * Title: *Aragorn
         * Subtitle: Heir to the White City
         * Side: Free Peoples
         * Culture: Gondor
         * Twilight Cost: 4
         * Type: Companion
         * Subtype: Man
         * Strength: 8
         * Vitality: 4
         * Signet: Frodo
         * Errata Game Text: Ranger.  Each time the fellowship moves during the fellowship phase, remove (1).
         */

        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");

        assertTrue(aragorn.getBlueprint().isUnique());
        assertEquals(4, aragorn.getBlueprint().getTwilightCost());

        assertEquals(8, aragorn.getBlueprint().getStrength());
        assertEquals(4, aragorn.getBlueprint().getVitality());
        assertEquals(Signet.FRODO, aragorn.getBlueprint().getSignet());

        assertTrue(scn.HasKeyword(aragorn, Keyword.RANGER));
    }


    @Test
    public void AragornAbilityOnlyWorksDuringFellowship() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl frodo = scn.GetRingBearer();
        PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
        scn.FreepsMoveCharToTable(aragorn);

        scn.StartGame();
        scn.SetTwilight(2);

        scn.FreepsPassCurrentPhaseAction();

        // 2 for Frodo/Aragorn, 1 for the site, 2 in the pool, -1 for Aragorn's text
        assertEquals(4, scn.GetTwilight());


        scn.SkipToPhase(Phase.REGROUP);

        scn.PassCurrentPhaseActions();
        scn.ShadowPassCurrentPhaseAction();
        assertEquals(4, scn.GetTwilight());
        scn.FreepsChooseToMove();

        // 2 for Frodo/Aragorn, 1 for the site, 4 in the pool, no removal from Aragorn
        assertEquals(7, scn.GetTwilight());
    }

    @Test
    public void AragornAbilityOnlyWorksIfTwilightInPool() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl frodo = scn.GetRingBearer();
        PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
        scn.FreepsMoveCharToTable(aragorn);

        scn.StartGame();

        scn.FreepsPassCurrentPhaseAction();

        // 2 for Frodo/Aragorn, 1 for the site, no removal since there was nothing in the pool before moving
        assertEquals(3, scn.GetTwilight());

    }



}
